/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.math.BigInteger;
import java.nio.file.attribute.UserPrincipal;
import java.security.AuthProvider;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.X509Util;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = BootstrapCryptoRunnerImpl.class )
public class BootstrapCryptoRunnerImpl {

    private static final String LOCALHOST_IP = "127.0.0.1"; //$NON-NLS-1$
    private static final String LOCALHOST = "localhost"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BootstrapCryptoRunnerImpl.class);

    private KeystoresManager keyStoresManager;
    private TruststoresManager trustStoresManager;
    private X509Util x509util;


    @Reference
    protected synchronized void setKeyStoresManager ( KeystoresManager ksm ) {
        this.keyStoresManager = ksm;
    }


    protected synchronized void unsetKeyStoresManager ( KeystoresManager ksm ) {
        if ( this.keyStoresManager == ksm ) {
            this.keyStoresManager = null;
        }
    }


    @Reference
    protected synchronized void setTrustStoresManager ( TruststoresManager tsm ) {
        this.trustStoresManager = tsm;
    }


    protected synchronized void unsetTrustStoresManager ( TruststoresManager tsm ) {
        if ( this.trustStoresManager == tsm ) {
            this.trustStoresManager = null;
        }
    }


    @Reference
    protected synchronized void setX509Util ( X509Util x509 ) {
        this.x509util = x509;
    }


    protected synchronized void unsetX509Util ( X509Util x509 ) {
        if ( this.x509util == x509 ) {
            this.x509util = null;
        }
    }


    /**
     * @param agentId
     * @param fqdn
     * @param serverId
     * @param serverUser
     * @param agentConfigWriter
     * @return whether the bootstrap process was successful
     */
    public CryptoBootstrapResult setupLocalServerCrypto ( UUID agentId, String fqdn, UUID serverId, UserPrincipal[] serverUser,
            DirectoryWriter agentConfigWriter ) {
        log.debug("Creating CA"); //$NON-NLS-1$
        try ( BootstrapCA bootstrapCA = createBootstrapCA(serverUser, agentId, fqdn) ) {

            if ( bootstrapCA == null ) {
                log.error("Failed to initialize CA"); //$NON-NLS-1$
                return null;
            }

            CryptoBootstrapResult cryptoBootstrapResult = new CryptoBootstrapResult(bootstrapCA.getCert());
            log.debug("Initialize internal trust store"); //$NON-NLS-1$
            if ( !initializeInternalTrustStore(bootstrapCA.getCert(), bootstrapCA.getCrl()) ) {
                return null;
            }

            log.debug("Generating certificates"); //$NON-NLS-1$
            if ( !generateEndEntityCertificates(agentId, fqdn, serverId, serverUser, agentConfigWriter, bootstrapCA, cryptoBootstrapResult) ) {
                return null;
            }

            return cryptoBootstrapResult;
        }
        catch ( KeystoreManagerException e ) {
            log.error("Failed to close CA", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param serverUser
     * @return
     */
    @SuppressWarnings ( "resource" )
    protected BootstrapCA createBootstrapCA ( UserPrincipal[] serverUser, UUID agentId, String fqdn ) {
        // generate internal CA
        KeystoreManager caKeystore = setupKeyStore(BootstrapConstants.INTERNAL_CA_KEYSTORE, null, true, serverUser);
        if ( caKeystore == null ) {
            return null;
        }

        KeyStore caKeys = createInternalCA(caKeystore, agentId, fqdn);
        if ( caKeys == null ) {
            try {
                caKeystore.close();
            }
            catch ( KeystoreManagerException e1 ) {
                log.error("Failed to close CA", e1); //$NON-NLS-1$
            }
            return null;
        }

        X509Certificate caCert;
        X509CRL caCrl;
        KeyPair caKeyPair;

        try {
            caCert = (X509Certificate) caKeys.getCertificate(BootstrapConstants.CA_KEY_ALIAS);

            if ( caCert == null ) {
                throw new CryptoException("Failed to get CA certificate"); //$NON-NLS-1$
            }

            caKeyPair = new KeyPair(caCert.getPublicKey(), (PrivateKey) caKeys.getKey(BootstrapConstants.CA_KEY_ALIAS, null));
            caCrl = this.makeInitialCRL(caKeystore.getProvider(), caKeyPair, caCert);
            return new BootstrapCA(caKeystore, caKeyPair, caCert, caCrl);
        }
        catch (
            CryptoException |
            KeyStoreException |
            UnrecoverableKeyException |
            NoSuchAlgorithmException |
            CertificateEncodingException e ) {
            log.error("Failed to get CA key", e); //$NON-NLS-1$
            try {
                caKeystore.close();
            }
            catch ( KeystoreManagerException e1 ) {
                log.error("Failed to close CA", e1); //$NON-NLS-1$
            }
            return null;
        }

    }


    /**
     * @param provider
     * @param caKeyPair
     * @param caCert
     * @return
     * @throws CertificateEncodingException
     * @throws CryptoException
     */
    private X509CRL makeInitialCRL ( AuthProvider provider, KeyPair caKeyPair, X509Certificate caCert )
            throws CertificateEncodingException, CryptoException {
        X500Name issuer = new JcaX509CertificateHolder(caCert).getSubject();
        return this.x509util.generateCRL(
            provider,
            caKeyPair,
            issuer,
            BigInteger.ONE,
            DateTime.now(),
            DateTime.now().plusDays(1),
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

    }


    /**
     * @param caKeystore
     * @return
     * 
     */
    protected KeyStore createInternalCA ( KeystoreManager caKeystore, UUID agentId, String fqdn ) {
        try {
            if ( caKeystore.getKeyStore().containsAlias(BootstrapConstants.CA_KEY_ALIAS) ) {
                log.info("Internal CA does already exist, skip"); //$NON-NLS-1$
                return caKeystore.getKeyStore();
            }
        }
        catch (
            KeyStoreException |
            KeystoreManagerException e ) {
            log.warn("Failed to access CA key store", e); //$NON-NLS-1$
        }

        try {

            log.info("Creating internal CA"); //$NON-NLS-1$

            // TOOD: 10 year lifetime, if rollover is supported shorten this
            int lifetimeDays = 365 * 10;
            X500NameBuilder dnBuilder = new X500NameBuilder(BCStyle.INSTANCE);
            dnBuilder.addRDN(BCStyle.CN, "AgNO3 Orchestrator Internal CA"); //$NON-NLS-1$
            dnBuilder.addRDN(BCStyle.O, getInstallOrganization(agentId));
            Set<CertExtension> exts = new HashSet<>(this.x509util.getDefaultCAExtensions(1));
            caKeystore.createSelfSigned(
                BootstrapConstants.CA_KEY_ALIAS,
                KeyType.RSA4096,
                dnBuilder.build(),
                this.x509util.makeRandomSerial(),
                lifetimeDays,
                exts);
            return caKeystore.getKeyStore();
        }
        catch ( KeystoreManagerException e ) {
            log.error("Failed to create internal CA", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * 
     * @param serverId
     * @param serverUser
     * @param agentConfigWriter
     * @param serverConfigWriter
     * @param bootstrapCA
     * @param cryptoBootstrapResult
     * @return
     */
    protected boolean generateEndEntityCertificates ( UUID agentId, String fqdn, UUID serverId, UserPrincipal[] serverUser,
            DirectoryWriter agentConfigWriter, BootstrapCA bootstrapCA, CryptoBootstrapResult cryptoBootstrapResult ) {
        // setup keystores
        try ( KeystoreManager agentKeystore = setupKeyStore(BootstrapConstants.AGENT_KEYSTORE, BootstrapConstants.INTERNAL_TRUSTSTORE, true);
              KeystoreManager serverKeystore = setupKeyStore(
                  BootstrapConstants.SERVER_KEYSTORE,
                  BootstrapConstants.INTERNAL_TRUSTSTORE,
                  true,
                  serverUser);
              KeystoreManager webKeyStore = setupKeyStore(BootstrapConstants.WEB_KEYSTORE, null, false, serverUser) ) {

            if ( agentKeystore == null || serverKeystore == null || webKeyStore == null ) {
                return false;
            }

            return generateEndEntityCertsInternal(
                agentId,
                fqdn,
                serverId,
                bootstrapCA,
                agentKeystore,
                serverKeystore,
                webKeyStore,
                cryptoBootstrapResult);
        }
        catch ( KeystoreManagerException e ) {
            log.error("Keystore failure", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param agentId
     * @param fqdn
     * @param serverId
     * @param bootstrapCA
     * @param agentKeystore
     * @param serverKeystore
     * @param webKeyStore
     * @param cryptoBootstrapResult
     * @return
     */
    private boolean generateEndEntityCertsInternal ( UUID agentId, String fqdn, UUID serverId, BootstrapCA bootstrapCA, KeystoreManager agentKeystore,
            KeystoreManager serverKeystore, KeystoreManager webKeyStore, CryptoBootstrapResult cryptoBootstrapResult ) {
        if ( !this.createAgentCert(agentKeystore, agentId, bootstrapCA) ) {
            return false;
        }

        if ( !this.createServerCert(serverKeystore, serverId, bootstrapCA) ) {
            return false;
        }

        if ( !this.createWebCert(webKeyStore, agentId, fqdn, bootstrapCA, cryptoBootstrapResult) ) {
            return false;
        }

        return true;
    }


    /**
     * @param caCert
     * @return
     */
    protected boolean initializeInternalTrustStore ( X509Certificate caCert, X509CRL caCrl ) {
        // -> setup trust store for agent and server
        TruststoreManager internalTruststore = setupInternalTrustStore();

        if ( internalTruststore == null ) {
            log.error("Failed to get internal truststore"); //$NON-NLS-1$
            return false;
        }

        try {
            for ( X509Certificate crt : internalTruststore.listCertificates() ) {
                internalTruststore.removeCertificate(crt);
            }
        }
        catch ( TruststoreManagerException e ) {
            log.warn("Failed to clean certificates from trust store", e); //$NON-NLS-1$
            internalTruststore.deleteAllCertificates();
        }

        try {
            if ( !internalTruststore.hasCertificate(caCert) ) {
                internalTruststore.addCertificate(caCert);
            }

            internalTruststore.updateCRL(caCrl);
        }
        catch ( TruststoreManagerException e ) {
            log.error("Failed to setup trust with internal ca", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param internalTruststore
     * @return
     */
    private TruststoreManager setupInternalTrustStore () {
        if ( !this.trustStoresManager.hasTrustStore(BootstrapConstants.INTERNAL_TRUSTSTORE) ) {
            try {
                this.trustStoresManager.createTrustStore(BootstrapConstants.INTERNAL_TRUSTSTORE);
            }
            catch ( TruststoreManagerException e ) {
                log.error("Failed to create internal truststore", e); //$NON-NLS-1$
                return null;
            }
        }
        else {
            log.info("Internal truststore does already exist"); //$NON-NLS-1$
        }

        try {
            TruststoreManager m = this.trustStoresManager.getTrustStoreManager(BootstrapConstants.INTERNAL_TRUSTSTORE);
            m.setRevocationConfig(makeRevocationConfig());
            return m;
        }
        catch ( TruststoreManagerException e ) {
            log.error("Failed to get internal truststore", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @return
     */
    private static RevocationConfig makeRevocationConfig () {
        return new BootstrapRevocationConfig();
    }


    /**
     * @param webKeyStore
     * @param cryptoBootstrapResult
     * @param provider
     * @param caCert
     * @param caKeyPair
     */
    private boolean createWebCert ( KeystoreManager webKeyStore, UUID agentId, String bootstrapHostname, BootstrapCA bootstrapCA,
            CryptoBootstrapResult cryptoBootstrapResult ) {

        X500NameBuilder nb = new X500NameBuilder(BCStyle.INSTANCE);
        nb.addRDN(BCStyle.CN, bootstrapHostname);
        nb.addRDN(BCStyle.OU, "Orchestrator WebGUI"); //$NON-NLS-1$
        nb.addRDN(BCStyle.O, getInstallOrganization(agentId));
        X500Name dn = nb.build();

        String hostAddress = LocalHostUtil.guessPrimaryAddress().getHostAddress();
        GeneralName[] sans = new GeneralName[] {
            new GeneralName(GeneralName.dNSName, bootstrapHostname), new GeneralName(GeneralName.dNSName, LOCALHOST),
            new GeneralName(GeneralName.iPAddress, hostAddress), new GeneralName(GeneralName.iPAddress, LOCALHOST_IP)
        };

        try {
            KeyStore keyStore = webKeyStore.getKeyStore();
            if ( keyStore.containsAlias(BootstrapConstants.WEB_KEY_ALIAS) ) {
                if ( !checkComponentCertificate(keyStore, BootstrapConstants.WEB_KEY_ALIAS, null, sans, bootstrapCA.getCert().getPublicKey()) ) {
                    log.info("Web certificate does already exist and is valid, skip"); //$NON-NLS-1$
                    cryptoBootstrapResult.setWebCert((X509Certificate) keyStore.getCertificate(BootstrapConstants.WEB_KEY_ALIAS));
                    return true;
                }
            }
        }
        catch (
            KeyStoreException |
            KeystoreManagerException |
            CertificateException e ) {
            log.warn("Failed to access web key store", e); //$NON-NLS-1$
            return false;
        }

        log.info("Generating web certificate"); //$NON-NLS-1$
        try {
            int lifetimeDays = 365;
            int keyUsage = KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyAgreement | KeyUsage.keyEncipherment;
            KeyPurposeId[] eku = new KeyPurposeId[] {
                KeyPurposeId.id_kp_serverAuth
            };

            Collection<CertExtension> exts = this.x509util.getDefaultClientExtensions(keyUsage, eku, sans);
            cryptoBootstrapResult.setWebCert(
                createSignedCertificate(
                    BootstrapConstants.WEB_KEY_ALIAS,
                    webKeyStore,
                    bootstrapCA.getProvider(),
                    bootstrapCA.getCert(),
                    bootstrapCA.getKeyPair(),
                    dn,
                    lifetimeDays,
                    exts,
                    true));
        }
        catch (
            KeystoreManagerException |
            CryptoException |
            CertificateException e ) {
            log.error("Failed to generate web certificate", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    private static String getInstallOrganization ( UUID agentId ) {
        return "AgNO3 Orchestrator Installation on " + agentId; //$NON-NLS-1$
    }


    /**
     * @param serverKeystore
     * @param serverId
     * @param caKeystore
     */
    private boolean createServerCert ( KeystoreManager serverKeystore, UUID serverId, BootstrapCA bootstrapCA ) {
        X500NameBuilder nb = new X500NameBuilder();
        nb.addRDN(AuthConstants.SERVER_ID_OID, serverId.toString());
        X500Name dn = nb.build();

        GeneralName[] sans = new GeneralName[] {
            new GeneralName(GeneralName.dNSName, LOCALHOST), new GeneralName(GeneralName.iPAddress, LOCALHOST_IP)
        };

        try {
            KeyStore keyStore = serverKeystore.getKeyStore();
            if ( keyStore.containsAlias(BootstrapConstants.SERVER_KEY_ALIAS) ) {
                if ( !checkComponentCertificate(keyStore, BootstrapConstants.SERVER_KEY_ALIAS, dn, sans, bootstrapCA.getCert().getPublicKey()) ) {
                    log.info("Server certificate does already exist and is valid, skip"); //$NON-NLS-1$
                    return true;
                }
            }
        }
        catch (
            KeyStoreException |
            KeystoreManagerException |
            CertificateException e ) {
            log.warn("Failed to access server key store", e); //$NON-NLS-1$
            return false;
        }

        log.info("Generating server certificate"); //$NON-NLS-1$
        try {
            int lifetimeDays = 2 * 365;
            int keyUsage = KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyAgreement | KeyUsage.keyEncipherment;
            KeyPurposeId[] eku = new KeyPurposeId[] {
                KeyPurposeId.id_kp_serverAuth
            };

            Collection<CertExtension> exts = this.x509util.getDefaultClientExtensions(keyUsage, eku, sans);
            createSignedCertificate(
                BootstrapConstants.SERVER_KEY_ALIAS,
                serverKeystore,
                bootstrapCA.getProvider(),
                bootstrapCA.getCert(),
                bootstrapCA.getKeyPair(),
                dn,
                lifetimeDays,
                exts,
                false);
        }
        catch (
            KeystoreManagerException |
            CryptoException |
            CertificateException e ) {
            log.error("Failed to generate server certificate", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param agentKeystore
     * @param caKeystore
     */
    private boolean createAgentCert ( KeystoreManager agentKeystore, UUID agentId, BootstrapCA bootstrapCA ) {

        X500NameBuilder nb = new X500NameBuilder();
        nb.addRDN(AuthConstants.AGENT_ID_OID, agentId.toString());
        X500Name dn = nb.build();
        GeneralName[] sans = new GeneralName[] {

        };

        try {
            KeyStore keyStore = agentKeystore.getKeyStore();
            if ( keyStore.containsAlias(BootstrapConstants.AGENT_KEY_ALIAS) ) {
                if ( !checkComponentCertificate(keyStore, BootstrapConstants.AGENT_KEY_ALIAS, dn, sans, bootstrapCA.getCert().getPublicKey()) ) {
                    log.info("Agent certificate does already exist and is valid, skip"); //$NON-NLS-1$
                    return true;
                }
            }
        }
        catch (
            KeyStoreException |
            KeystoreManagerException |
            CertificateException e ) {
            log.warn("Failed to access agent key store", e); //$NON-NLS-1$
            return false;
        }

        log.info("Generating agent certificate"); //$NON-NLS-1$
        try {
            int lifetimeDays = 2 * 365;
            int keyUsage = KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyAgreement | KeyUsage.keyEncipherment;
            KeyPurposeId[] eku = new KeyPurposeId[] {
                KeyPurposeId.id_kp_clientAuth
            };

            Collection<CertExtension> exts = this.x509util.getDefaultClientExtensions(keyUsage, eku, sans);

            createSignedCertificate(
                BootstrapConstants.AGENT_KEY_ALIAS,
                agentKeystore,
                bootstrapCA.getProvider(),
                bootstrapCA.getCert(),
                bootstrapCA.getKeyPair(),
                dn,
                lifetimeDays,
                exts,
                false);
        }
        catch (
            KeystoreManagerException |
            CryptoException |
            CertificateException e ) {
            log.error("Failed to generate agent certificate", e); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    /**
     * @param keyStore
     * @return
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    protected boolean checkComponentCertificate ( KeyStore keyStore, String alias, X500Name expectDn, GeneralName[] expectSans, PublicKey publicKey )
            throws KeyStoreException, CertificateEncodingException {
        X509Certificate componentCert = (X509Certificate) keyStore.getCertificate(alias);

        JcaX509CertificateHolder holder = new JcaX509CertificateHolder(componentCert);
        X500Name dn = holder.getSubject();
        RDN[] cns = dn.getRDNs(BCStyle.CN);

        if ( expectDn != null ) {
            RDN[] expectCns = expectDn.getRDNs(BCStyle.CN);
            if ( cns != null && cns.length > 0 && expectCns != null && expectCns.length > 0 ) {
                if ( !Arrays.equals(cns, expectCns) ) {
                    log.warn("Certificate does not contain expected CN " + dn); //$NON-NLS-1$
                    return true;
                }
            }
            else if ( !dn.equals(expectDn) ) {
                log.warn("Certificate does not contain expected DN " + dn); //$NON-NLS-1$
                return true;
            }
        }

        // TODO: should also check SANs

        try {
            componentCert.checkValidity();
            componentCert.verify(publicKey);
            return false;
        }
        catch (
            CertificateException |
            InvalidKeyException |
            NoSuchAlgorithmException |
            NoSuchProviderException |
            SignatureException e ) {
            log.warn("Certificate expired or invalid: ", e.getMessage()); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Certificate check failed", e); //$NON-NLS-1$
                log.debug("Certificate " + componentCert); //$NON-NLS-1$
                log.debug("Public key " + publicKey); //$NON-NLS-1$
            }
        }
        return true;
    }


    /**
     * @param alias
     * @param ks
     * @param caProv
     * @param caCert
     * @param caKeyPair
     * @param dn
     * @param serial
     * @param lifetimeDays
     * @param exts
     * @return
     * @throws KeystoreManagerException
     * @throws CryptoException
     * @throws CertificateEncodingException
     */
    private X509Certificate createSignedCertificate ( String alias, KeystoreManager ks, Provider caProv, X509Certificate caCert, KeyPair caKeyPair,
            X500Name dn, int lifetimeDays, Collection<CertExtension> exts, boolean includeCAInChain )
                    throws KeystoreManagerException, CryptoException, CertificateEncodingException {

        PKCS10CertificationRequest csr;
        if ( ks.getEntry(alias) != null ) {
            csr = ks.getCSR(alias, dn, null, null);
        }
        else {
            csr = ks.createWithCSR(alias, KeyType.RSA2048, dn, null, null);
        }

        Date notBefore = new Date();
        Date notAfter = DateTime.now().plusDays(lifetimeDays).toDate();

        BigInteger serial = this.x509util.makeRandomSerial();

        X500Name caDn = new JcaX509CertificateHolder(caCert).getSubject();
        X509Certificate cert = this.x509util.signCertificate(caProv, csr, caDn, caKeyPair, serial, notBefore, notAfter, exts);

        if ( includeCAInChain ) {
            ks.updateCertificateChain(alias, new X509Certificate[] {
                cert, caCert
            });
        }
        else {
            ks.updateCertificateChain(alias, new X509Certificate[] {
                cert
            });
        }

        return cert;
    }


    /**
     * @return
     */
    protected KeystoreManager setupKeyStore ( String keyStoreName, String validationTrustStore, boolean internal, UserPrincipal... grantAccess ) {
        KeystoreManager keystore;
        if ( !this.keyStoresManager.hasKeyStore(keyStoreName) ) {
            try {
                this.keyStoresManager.createKeyStore(keyStoreName, internal);
                keystore = this.keyStoresManager.getKeyStoreManager(keyStoreName);
            }
            catch ( KeystoreManagerException e ) {
                log.error("Failed to create keystore " + keyStoreName, e); //$NON-NLS-1$
                return null;
            }

            for ( UserPrincipal up : grantAccess ) {
                try {
                    keystore.allowUser(up);
                }
                catch (
                    UnixAccountException |
                    KeystoreManagerException e ) {
                    log.warn("Failed to grant access to keystore", e); //$NON-NLS-1$
                }
            }

            try {
                keystore.setValidationTruststoreName(validationTrustStore);
            }
            catch ( KeystoreManagerException e ) {
                log.warn("Failed to set validation truststore", e); //$NON-NLS-1$
            }

            return keystore;
        }

        log.info("Keystore does already exist " + keyStoreName); //$NON-NLS-1$
        KeystoreManager km;
        try {
            km = this.keyStoresManager.getKeyStoreManager(keyStoreName);
        }
        catch ( KeystoreManagerException e ) {
            log.error("Failed to get keystore " + keyStoreName, e); //$NON-NLS-1$
            return null;
        }

        try {
            km.setValidationTruststoreName(validationTrustStore);
        }
        catch ( KeystoreManagerException e ) {
            log.warn("Failed to set validation truststore on existing", e); //$NON-NLS-1$
        }
        return km;
    }

}

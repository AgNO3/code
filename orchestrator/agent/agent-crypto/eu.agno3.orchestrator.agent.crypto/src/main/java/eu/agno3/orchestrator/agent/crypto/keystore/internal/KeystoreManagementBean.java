/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorException.Reason;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.agent.realms.AsymmetricKeyStoreEntry;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.crypto.keystore.CertRequestData;
import eu.agno3.orchestrator.crypto.keystore.CertificateInfo;
import eu.agno3.orchestrator.crypto.keystore.ExtensionData;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagementMXBean;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.crypto.keystore.KeystoreNotFoundException;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.CertExtensionImpl;
import eu.agno3.runtime.crypto.x509.X509Util;
import eu.agno3.runtime.jmx.MBean;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    MBean.class, KeystoreManagementBean.class
}, property = {
    "objectName=eu.agno3.agent.crypto:type=KeystoreManagementBean"
} )
public class KeystoreManagementBean implements KeystoreManagementMXBean, MBean {

    private static final Logger log = Logger.getLogger(KeystoreManagementBean.class);

    private KeystoresManager ksManager;
    private X509Util x509util;
    private TruststoresManager tsManager;

    private TrustChecker trustChecker;


    @Reference
    protected synchronized void setKeystoresManager ( KeystoresManager ksm ) {
        this.ksManager = ksm;
    }


    protected synchronized void unsetKeystoresManager ( KeystoresManager ksm ) {
        if ( this.ksManager == ksm ) {
            this.ksManager = null;
        }
    }


    @Reference
    protected synchronized void setTruststoresManager ( TruststoresManager tsm ) {
        this.tsManager = tsm;
    }


    protected synchronized void unsetTruststoresManager ( TruststoresManager tsm ) {
        if ( this.tsManager == tsm ) {
            this.tsManager = null;
        }
    }


    @Reference
    protected synchronized void setTrustChecker ( TrustChecker tc ) {
        this.trustChecker = tc;
    }


    protected synchronized void unsetTrustChecker ( TrustChecker tc ) {
        if ( this.trustChecker == tc ) {
            this.trustChecker = null;
        }
    }


    @Reference
    protected synchronized void setX509Util ( X509Util xu ) {
        this.x509util = xu;
    }


    protected synchronized void unsetX509Util ( X509Util xu ) {
        if ( this.x509util == xu ) {
            this.x509util = null;
        }
    }


    @Override
    public List<KeyStoreInfo> getKeystores ( boolean includeInternal ) throws KeystoreManagerException {
        List<KeyStoreInfo> res = new LinkedList<>();
        for ( String ksAlias : this.ksManager.getKeyStores() ) {
            try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(ksAlias) ) {
                KeyStoreInfo ksi = makeKeystoreInfo(ksAlias, keyStoreManager);
                if ( !includeInternal && ksi.isInternal() ) {
                    continue;
                }
                res.add(ksi);
            }
            catch ( KeystoreManagerException e ) {
                log.error("Failed to load keystore " + ksAlias, e); //$NON-NLS-1$
            }
        }
        return res;
    }


    /**
     * @param ksAlias
     * @param keyStoreManager
     * @return
     * @throws KeystoreManagerException
     */
    private KeyStoreInfo makeKeystoreInfo ( String ksAlias, KeystoreManager keyStoreManager ) throws KeystoreManagerException {
        KeyStoreInfo ksi = new KeyStoreInfo();
        ksi.setAlias(ksAlias);
        ksi.setValidationTrustStore(this.ksManager.getValidationTruststoreName(ksAlias));
        ksi.setInternal(this.ksManager.isInternalKeyStore(ksAlias));
        ksi.setKeyEntries(wrapKeys(keyStoreManager.listKeys()));
        return ksi;
    }


    @Override
    public List<String> getKeystoreAliases () throws KeystoreManagerException {
        return this.ksManager.getKeyStores();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.keystore.KeystoreManagementMXBean#getKeyStoreInfo(java.lang.String)
     */
    @Override
    public KeyStoreInfo getKeyStoreInfo ( String keystore ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            return makeKeystoreInfo(keystore, keyStoreManager);
        }
        catch ( KeystoreNotFoundException e ) {
            log.debug("Keystore not found", e); //$NON-NLS-1$
            return null;
        }
    }


    @Override
    public KeyInfo getKeyInfo ( String keystore, String keyAlias ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            return wrapKey(keyStoreManager.getEntry(keyAlias));
        }
    }


    @Override
    public String validateChain ( String keystore, boolean checkRevocation, List<String> chain ) throws KeystoreManagerException {
        String validationTruststoreName = this.ksManager.getValidationTruststoreName(keystore);

        try {
            X509Certificate[] certs = unwrapCerts(chain);

            if ( certs == null || certs.length == 0 ) {
                return "EMPTY"; //$NON-NLS-1$
            }

            try {
                PKIXCertPathBuilderResult val = this.trustChecker.validateChain(
                    this.tsManager.getTrustConfig(validationTruststoreName, checkRevocation),
                    Arrays.asList(certs),
                    null,
                    null,
                    null);

                if ( val == null ) {
                    return "NOTRUST"; //$NON-NLS-1$
                }
            }
            catch ( CryptoException e ) {
                String valError = unwrapValidationError(e);
                log.debug("Certificate validation failed " + valError, e); //$NON-NLS-1$
                return valError;
            }
            return null;
        }
        catch (
            CertificateException |
            TruststoreManagerException e ) {
            log.warn("Failed to validate cert chain", e); //$NON-NLS-1$
            throw new KeystoreManagerException("Failed to validate"); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return
     */
    private static String unwrapValidationError ( Exception e ) {

        Throwable ex = e;

        if ( ex instanceof CryptoException && ex.getCause() != null ) {
            ex = ex.getCause();
        }

        if ( ex instanceof CertPathBuilderException && ex.getCause() != null ) {
            ex = ex.getCause();
        }

        if ( ! ( ex.getCause() instanceof CertPathValidatorException ) ) {
            return "UNKNOWN"; //$NON-NLS-1$
        }

        CertPathValidatorException cpve = (CertPathValidatorException) ex.getCause();

        Reason r = cpve.getReason();

        if ( r instanceof BasicReason ) {
            BasicReason br = (BasicReason) r;
            return br.name();
        }
        else if ( r instanceof PKIXReason ) {
            PKIXReason pr = (PKIXReason) r;
            return pr.name();
        }

        return "UNKNOWN"; //$NON-NLS-1$
    }


    /**
     * 
     * @param keystore
     * @param keyAlias
     * @param kt
     * @throws KeystoreManagerException
     */
    @Override
    public void generateKey ( String keystore, String keyAlias, String kt ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            KeyType keyType;
            try {
                keyType = KeyType.valueOf(kt);
            }
            catch ( IllegalArgumentException e ) {
                throw new KeystoreManagerException("Unknown keyType " + kt); //$NON-NLS-1$
            }
            keyStoreManager.generateKey(keyAlias, keyType);
        }
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param kt
     * @param keyData
     * @param importChain
     * @throws KeystoreManagerException
     */
    @Override
    public void importKey ( String keystore, String keyAlias, String kt, String keyData, List<String> importChain ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            KeyType keyType;
            try {
                keyType = KeyType.valueOf(kt);
            }
            catch ( IllegalArgumentException e ) {
                throw new KeystoreManagerException("Unknown keyType " + kt); //$NON-NLS-1$
            }

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyData));
            PrivateKey pkey = KeyFactory.getInstance(keyType.getAlgo()).generatePrivate(keySpec);
            if ( ! ( pkey instanceof RSAPrivateCrtKey ) ) {
                throw new KeystoreManagerException("Cannot handle non RSA key"); //$NON-NLS-1$
            }

            RSAPrivateCrtKey key = (RSAPrivateCrtKey) pkey;
            PublicKey pubkey = KeyFactory.getInstance(keyType.getAlgo())
                    .generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent()));

            X509Certificate[] certs = unwrapCerts(importChain);
            keyStoreManager.importKey(keyAlias, new KeyPair(pubkey, key), certs);
        }
        catch (
            InvalidKeySpecException |
            NoSuchAlgorithmException |
            CertificateException e ) {
            throw new KeystoreManagerException("Failed to parse key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param chain
     * @return
     * @throws CertificateException
     */
    private static X509Certificate[] unwrapCerts ( List<String> chain ) throws CertificateException {
        if ( chain == null ) {
            return new X509Certificate[] {};
        }
        X509Certificate[] certs = new X509Certificate[chain.size()];

        int i = 0;
        for ( String certData : chain ) {
            certs[ i++ ] = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(Base64.decodeBase64(certData)));
        }
        return certs;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.keystore.KeystoreManagementMXBean#updateChain(java.lang.String,
     *      java.lang.String, java.util.List)
     */
    @Override
    public void updateChain ( String keystore, String keyAlias, List<String> certs ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            keyStoreManager.updateCertificateChain(keyAlias, unwrapCerts(certs));
        }
        catch ( CertificateException e ) {
            throw new KeystoreManagerException("Failed to parse certificates", e); //$NON-NLS-1$
        }
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param req
     * @return the generated CSR
     * @throws KeystoreManagerException
     */
    @Override
    public String generateCSR ( String keystore, String keyAlias, CertRequestData req ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            Set<CertExtension> exts = new HashSet<>();
            for ( ExtensionData extData : req.getExtensions() ) {
                ASN1StreamParser p = new ASN1StreamParser(Base64.decodeBase64(extData.getData()));
                exts.add(new CertExtensionImpl(new ASN1ObjectIdentifier(extData.getOid()), extData.getCritical(), p.readObject()));
            }
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs = new HashMap<>();
            if ( req.getRequestPassword() != null ) {
                attrs.put(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, new DERUTF8String(req.getRequestPassword()));
            }

            X500Name subjectName = new X500Name(req.getSubject());
            PKCS10CertificationRequest csr = keyStoreManager.getCSR(keyAlias, subjectName, exts, attrs);
            return this.x509util.toPEM(csr);
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to parse request", e); //$NON-NLS-1$
        }
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param requestPassword
     * @return the generate CSR
     * @throws KeystoreManagerException
     */
    @Override
    public String generateRenewalCSR ( String keystore, String keyAlias, String requestPassword ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            KeyStoreEntry entry = keyStoreManager.getEntry(keyAlias);
            Certificate[] certs = entry.getCertificateChain();

            if ( certs == null || certs.length < 1 ) {
                throw new KeystoreManagerException("No current certificate available"); //$NON-NLS-1$
            }

            X509Certificate curCert = (X509Certificate) certs[ 0 ];
            JcaX509CertificateHolder holder = new JcaX509CertificateHolder(curCert);
            Set<CertExtension> exts = extsFromCurrentCert(holder);

            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs = new HashMap<>();
            if ( requestPassword != null ) {
                attrs.put(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, new DERUTF8String(requestPassword));
            }

            PKCS10CertificationRequest csr = keyStoreManager.getCSR(keyAlias, holder.getSubject(), exts, attrs);
            return this.x509util.toPEM(csr);
        }
        catch (
            CertificateEncodingException |
            IOException e ) {
            throw new KeystoreManagerException("Failed to parse certificate", e); //$NON-NLS-1$
        }
    }


    @Override
    public void makeSelfSignedCert ( String keystore, String keyAlias, CertRequestData req ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            Set<CertExtension> exts = new HashSet<>();
            for ( ExtensionData extData : req.getExtensions() ) {
                ASN1Primitive data = ASN1Primitive.fromByteArray(Base64.decodeBase64(extData.getData()));
                exts.add(new CertExtensionImpl(new ASN1ObjectIdentifier(extData.getOid()), extData.getCritical(), data));
            }

            X500Name subjectName = new X500Name(req.getSubject());
            PKCS10CertificationRequest csr = keyStoreManager.getCSR(keyAlias, subjectName, exts, null);
            BigInteger serial = this.x509util.makeRandomSerial();
            DateTime notBefore = DateTime.now();
            DateTime notAfter = notBefore.plusDays(req.getLifetimeDays());
            X509Certificate cert = keyStoreManager.generateSelfSigned(keyAlias, csr, serial, notBefore, notAfter, exts);
            keyStoreManager.updateCertificateChain(keyAlias, new X509Certificate[] {
                cert
            });
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to parse request", e); //$NON-NLS-1$
        }
    }


    /**
     * @param holder
     * @return
     */
    private static Set<CertExtension> extsFromCurrentCert ( JcaX509CertificateHolder holder ) {
        Set<CertExtension> exts = new HashSet<>();
        Extensions extensions = holder.getExtensions();
        for ( ASN1ObjectIdentifier extOid : extensions.getCriticalExtensionOIDs() ) {
            exts.add(new CertExtensionImpl(extOid, true, extensions.getExtensionParsedValue(extOid)));
        }

        for ( ASN1ObjectIdentifier extOid : extensions.getNonCriticalExtensionOIDs() ) {
            exts.add(new CertExtensionImpl(extOid, false, extensions.getExtensionParsedValue(extOid)));
        }
        return exts;
    }


    /**
     * @param keystore
     * @param keyAlias
     * @throws KeystoreManagerException
     */
    @Override
    public void deleteKey ( String keystore, String keyAlias ) throws KeystoreManagerException {
        try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(keystore) ) {
            keyStoreManager.deleteKey(keyAlias);
        }
    }


    /**
     * @param keys
     * @return
     * @throws KeystoreManagerException
     */
    private static List<KeyInfo> wrapKeys ( List<KeyStoreEntry> keys ) throws KeystoreManagerException {
        List<KeyInfo> res = new LinkedList<>();

        for ( KeyStoreEntry kse : keys ) {
            KeyInfo wrapKey = wrapKey(kse);
            if ( wrapKey != null ) {
                res.add(wrapKey);
            }
        }
        return res;
    }


    /**
     * @param kse
     * @return
     * @throws KeystoreManagerException
     */
    private static KeyInfo wrapKey ( KeyStoreEntry kse ) throws KeystoreManagerException {
        if ( kse == null ) {
            return null;
        }

        try {
            KeyInfo ki = new KeyInfo();

            ki.setKeyAlias(kse.getAlias());
            ki.setKeyType(kse.getType());

            if ( kse instanceof AsymmetricKeyStoreEntry ) {
                ki.setEncodedPublicKey(Base64.encodeBase64String( ( (AsymmetricKeyStoreEntry) kse ).getPublicKey().getEncoded()));
            }

            ki.setCertificateChain(wrapCertificateChain(kse.getCertificateChain()));
            return ki;
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Failed to handle key " + kse.getAlias(), e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param certificateChain
     * @return
     * @throws KeystoreManagerException
     */
    private static List<CertificateInfo> wrapCertificateChain ( Certificate[] certificateChain ) throws KeystoreManagerException {
        if ( certificateChain == null || certificateChain.length == 0 ) {
            return Collections.EMPTY_LIST;
        }

        List<CertificateInfo> chain = new LinkedList<>();

        for ( Certificate cert : certificateChain ) {
            CertificateInfo ci = new CertificateInfo();
            try {
                ci.setCertificateData(Base64.encodeBase64String(cert.getEncoded()));
            }
            catch ( CertificateEncodingException e ) {
                throw new KeystoreManagerException("Failed to encode certificate", e); //$NON-NLS-1$
            }
            chain.add(ci);
        }
        return chain;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.AuthProvider;
import java.security.InvalidKeyException;
import java.security.Key;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.realms.AbstractAsymmetricKeyEntry;
import eu.agno3.orchestrator.agent.realms.ECKeyEntry;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.agent.realms.RSAKeyEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.acl.util.ACLGroupSyncUtil;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.X509Util;


/**
 * @author mbechler
 *
 */
public class PKCS11Keystore implements KeystoreManager {

    /**
     * 
     */
    private static final BouncyCastleProvider BCPROV = new BouncyCastleProvider();

    private static final Logger log = Logger.getLogger(PKCS11Keystore.class);

    private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private AuthProvider provider;
    private PKCS11Util pkcs11util;
    private X509Util x509util;
    private File keyStoreDir;

    private String pin;


    /**
     * @param keyStoreDir
     * @param x509util
     * @param pkcs11util
     * @param provider
     * @param pin
     */
    public PKCS11Keystore ( File keyStoreDir, AuthProvider provider, String pin, PKCS11Util pkcs11util, X509Util x509util ) {
        this.keyStoreDir = keyStoreDir;
        this.provider = provider;
        this.pin = pin;
        this.pkcs11util = pkcs11util;
        this.x509util = x509util;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#getProvider()
     */
    @Override
    public AuthProvider getProvider () {
        return this.provider;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#getKeyStore()
     */
    @Override
    public KeyStore getKeyStore () throws KeystoreManagerException {
        try {
            return this.pkcs11util.getKeyStore(this.provider, this.pin);
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to get keystore", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#close()
     */
    @Override
    public void close () throws KeystoreManagerException {
        try {
            this.pkcs11util.close(this.provider);
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to logout", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#setValidationTruststoreName(java.lang.String)
     */
    @Override
    public void setValidationTruststoreName ( String validationTrustStore ) throws KeystoreManagerException {
        File validationTruststoreFile = new File(this.keyStoreDir, KeystoresManager.VALIDATION_TRUSTSTORE_FILE);
        if ( ( !validationTruststoreFile.exists() && !validationTruststoreFile.getParentFile().canWrite() )
                || ( validationTruststoreFile.exists() && !validationTruststoreFile.canWrite() ) ) {
            throw new KeystoreManagerException("Cannot write validation truststore file"); //$NON-NLS-1$
        }

        if ( validationTrustStore == null ) {
            validationTruststoreFile.delete();
            return;
        }

        try {
            Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(validationTruststoreFile.toPath());
            try {
                Files.write(tmpFile, validationTrustStore.getBytes(UTF8), StandardOpenOption.TRUNCATE_EXISTING);
                PosixFileAttributeView fileAttributeView = Files
                        .getFileAttributeView(tmpFile, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

                fileAttributeView.setOwner(this.getKeystoreUser());
                fileAttributeView.setGroup(this.getKeystoreGroup());
                fileAttributeView.setPermissions(PosixFilePermissions.fromString("rw-r-----")); //$NON-NLS-1$
                FileUtil.safeMove(tmpFile, validationTruststoreFile.toPath(), true);
            }
            finally {
                Files.deleteIfExists(tmpFile);
            }
            syncACL();
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to read validation truststore file", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws KeystoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#getKeystoreGroup()
     */
    @Override
    public GroupPrincipal getKeystoreGroup () throws KeystoreManagerException {
        try {
            return Files.getFileAttributeView(this.keyStoreDir.toPath(), PosixFileAttributeView.class).readAttributes().group();
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to get keystore group", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws KeystoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#getKeystoreUser()
     */
    @Override
    public UserPrincipal getKeystoreUser () throws KeystoreManagerException {
        try {
            return Files.getOwner(this.keyStoreDir.toPath());
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to get keystore user", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the users that are allowed to access this keystore
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    @Override
    public Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KeystoreManagerException {
        return UnixAccountUtil.getMembers(this.getKeystoreGroup());
    }


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    @Override
    public void allowUser ( UserPrincipal user ) throws UnixAccountException, KeystoreManagerException {
        UnixAccountUtil.addToGroup(this.getKeystoreGroup(), user);
        syncACL();
    }


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    @Override
    public void revokeUser ( UserPrincipal user ) throws UnixAccountException, KeystoreManagerException {
        UnixAccountUtil.removeFromGroup(this.getKeystoreGroup(), user);
        syncACL();
    }


    private void syncACL () {
        try {
            ACLGroupSyncUtil.syncACLRecursive(this.keyStoreDir.toPath());
        }
        catch ( IOException e ) {
            log.warn("Failed to sync ACL entries", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the entries in this key store
     * @throws KeystoreManagerException
     */
    @Override
    public List<KeyStoreEntry> listKeys () throws KeystoreManagerException {
        List<KeyStoreEntry> res = new LinkedList<>();
        try {
            KeyStore keyStore = this.pkcs11util.getKeyStore(this.provider, this.pin);

            Enumeration<String> aliases = keyStore.aliases();

            while ( aliases.hasMoreElements() ) {
                String alias = aliases.nextElement();

                if ( !keyStore.isKeyEntry(alias) ) {
                    continue;
                }

                AbstractAsymmetricKeyEntry entry = handleKey(keyStore, alias);
                if ( entry != null ) {
                    res.add(entry);
                }
            }

            return res;
        }
        catch (
            CryptoException |
            KeyStoreException e ) {
            throw new KeystoreManagerException("Failed to enumerate keys", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#getEntry(java.lang.String)
     */
    @Override
    public KeyStoreEntry getEntry ( String alias ) throws KeystoreManagerException {
        try {
            KeyStore keyStore = this.pkcs11util.getKeyStore(this.provider, this.pin);

            if ( !keyStore.isKeyEntry(alias) ) {
                return null;
            }

            if ( !this.pkcs11util.keyAliasExists(this.provider, alias) ) {
                log.warn("Keystore contains stale objects but not related private key " + alias); //$NON-NLS-1$
                this.pkcs11util.cleanupAlias(this.provider, keyStore, alias);
                return null;
            }

            return this.handleKey(keyStore, alias);
        }
        catch (
            CryptoException |
            KeyStoreException e ) {
            throw new KeystoreManagerException("Failed to get key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param keyStore
     * @param alias
     * @param res
     * @return
     * @throws KeyStoreException
     */
    private AbstractAsymmetricKeyEntry handleKey ( KeyStore keyStore, String alias ) throws KeyStoreException {
        Certificate[] chain = keyStore.getCertificateChain(alias);

        if ( chain == null || chain.length == 0 ) {
            log.warn("Chain is empty for " + alias); //$NON-NLS-1$
            return null;
        }

        if ( ! ( chain[ 0 ] instanceof X509Certificate ) ) {
            log.warn("Not a X509 certificate in " + alias); //$NON-NLS-1$
            return null;
        }

        X509Certificate primary = (X509Certificate) chain[ 0 ];
        PublicKey publicKey = primary.getPublicKey();

        if ( publicKey instanceof RSAPublicKey ) {
            if ( isFakeCertificate(primary) ) {
                chain = null;
            }

            return new RSAKeyEntry(alias, (RSAPublicKey) publicKey, chain);
        }
        else if ( publicKey instanceof ECPublicKey ) {
            if ( isFakeCertificate(primary) ) {
                chain = null;
            }

            return new ECKeyEntry(alias, (ECPublicKey) publicKey, chain);
        }
        else {
            log.warn("Unsupported key in " + alias); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#createSelfSigned(java.lang.String,
     *      eu.agno3.runtime.crypto.keystore.KeyType, org.bouncycastle.asn1.x500.X500Name, java.math.BigInteger, int,
     *      java.util.Set)
     */
    @Override
    public void createSelfSigned ( String alias, KeyType keyType, X500Name dn, BigInteger serial, int lifetimeDays, Set<CertExtension> exts )
            throws KeystoreManagerException {
        try {
            KeyStore ks = this.pkcs11util.getKeyStore(this.provider, this.pin);
            KeyPair kp = this.pkcs11util.prepareKeyPair(this.provider, ks, alias, keyType);

            storeKeyPair(alias, dn, serial, lifetimeDays, exts, ks, kp);
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to generate key with self-signed cert", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#generateKey(java.lang.String,
     *      eu.agno3.runtime.crypto.keystore.KeyType)
     */
    @Override
    public void generateKey ( String alias, KeyType kt ) throws KeystoreManagerException {
        try {
            KeyStore ks = this.pkcs11util.getKeyStore(this.provider, this.pin);
            KeyPair kp = this.pkcs11util.prepareKeyPair(this.provider, ks, alias, kt);

            this.pkcs11util.storeKeyPair(this.provider, ks, alias, kp, new Certificate[] {
                this.makeFakeCertificate(this.provider, kp)
            });
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to generate key with self-signed cert", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @param dn
     * @param serial
     * @param lifetimeDays
     * @param exts
     * @param ks
     * @param kp
     * @throws CryptoException
     * @throws Exception
     */
    private void storeKeyPair ( String alias, X500Name dn, BigInteger serial, int lifetimeDays, Set<CertExtension> exts, KeyStore ks, KeyPair kp )
            throws CryptoException {
        try {
            DateTime now = DateTime.now();
            DateTime expire = DateTime.now().plusDays(lifetimeDays);

            X509Certificate selfSignedCert = this.x509util.selfSignCertificate(this.provider, kp, dn, serial, now.toDate(), expire.toDate(), exts);

            this.pkcs11util.storeKeyPair(this.provider, ks, alias, kp, new Certificate[] {
                selfSignedCert
            });
        }
        catch ( Exception e ) {
            this.pkcs11util.cleanupAlias(this.provider, ks, alias);
            throw e;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#createWithCSR(java.lang.String,
     *      eu.agno3.runtime.crypto.keystore.KeyType, org.bouncycastle.asn1.x500.X500Name, java.util.Set, java.util.Map)
     */
    @Override
    public PKCS10CertificationRequest createWithCSR ( String alias, KeyType kt, X500Name dn, Set<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs ) throws KeystoreManagerException {
        try {
            KeyStore ks = this.pkcs11util.getKeyStore(this.provider, this.pin);
            KeyPair kp = this.pkcs11util.prepareKeyPair(this.provider, ks, alias, kt);
            return generateCSR(alias, dn, exts, attrs, ks, kp);
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to generate key with csr", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @param dn
     * @param exts
     * @param attrs
     * @param ks
     * @param kp
     * @return
     * @throws CryptoException
     * @throws Exception
     */
    private PKCS10CertificationRequest generateCSR ( String alias, X500Name dn, Set<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs, KeyStore ks, KeyPair kp ) throws CryptoException {
        try {
            this.pkcs11util.storeKeyPair(this.provider, ks, alias, kp, new Certificate[] {
                this.makeFakeCertificate(this.provider, kp)
            });

            return this.x509util.generateCSR(this.provider, kp, dn, exts, attrs);
        }
        catch ( Exception e ) {
            this.pkcs11util.cleanupAlias(this.provider, ks, alias);
            throw e;
        }
    }


    /**
     * @param alias
     * @param dn
     * @param exts
     * @param attrs
     * @return a certificate request for the given key
     * @throws KeystoreManagerException
     */
    @Override
    public PKCS10CertificationRequest getCSR ( String alias, X500Name dn, Set<CertExtension> exts, Map<ASN1ObjectIdentifier, ASN1Encodable> attrs )
            throws KeystoreManagerException {
        try {
            KeyPair kp = getKeyPair(alias);
            return this.x509util.generateCSR(this.provider, kp, dn, exts, attrs);
        }
        catch (
            CryptoException |
            KeyStoreException |
            UnrecoverableKeyException |
            NoSuchAlgorithmException e ) {
            throw new KeystoreManagerException("Failed to generate CSR", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @return
     * @throws CryptoException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    private KeyPair getKeyPair ( String alias ) throws CryptoException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore ks = this.pkcs11util.getKeyStore(this.provider, this.pin);
        Key k = ks.getKey(alias, null);
        Certificate cert = ks.getCertificate(alias);

        if ( k == null || cert == null ) {
            if ( this.pkcs11util.keyAliasExists(this.provider, alias) ) {
                throw new KeyStoreException("Keystore contains broken key entry (probably without cert) " + alias); //$NON-NLS-1$
            }
            throw new KeyStoreException("Key not found " + alias); //$NON-NLS-1$
        }

        PublicKey pk = cert.getPublicKey();

        if ( ! ( k instanceof PrivateKey ) ) {
            throw new CryptoException("Key is not a private key"); //$NON-NLS-1$
        }
        return new KeyPair(pk, (PrivateKey) k);
    }


    /**
     * @param alias
     * @param kp
     * @param chain
     * @throws KeystoreManagerException
     */
    @Override
    public void importKey ( String alias, KeyPair kp, X509Certificate[] chain ) throws KeystoreManagerException {

        try {
            KeyStore keyStore = this.pkcs11util.getKeyStore(this.provider, this.pin);
            this.pkcs11util.importKeyPair(this.provider, keyStore, alias, kp, realOrFakeCertificateChain(alias, BCPROV, kp, chain));
        }
        catch (
            CryptoException |
            UnrecoverableKeyException |
            KeyStoreException |
            NoSuchAlgorithmException e ) {
            throw new KeystoreManagerException("Failed to import key", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager#signUsingKey(java.lang.String,
     *      org.bouncycastle.pkcs.PKCS10CertificationRequest, java.math.BigInteger, org.joda.time.DateTime,
     *      org.joda.time.DateTime, java.util.Set)
     */
    @Override
    public X509Certificate signUsingKey ( String signingAlias, PKCS10CertificationRequest csr, BigInteger serial, DateTime notBefore,
            DateTime notAfter, Set<CertExtension> extensions ) throws KeystoreManagerException {

        KeyStoreEntry entry = getEntry(signingAlias);
        Certificate[] certificateChain = entry.getCertificateChain();
        if ( certificateChain == null || certificateChain.length == 0 || isFakeCertificate((X509Certificate) certificateChain[ 0 ]) ) {
            throw new KeystoreManagerException("No certificiate available for signing"); //$NON-NLS-1$
        }

        X509Certificate signCert = (X509Certificate) certificateChain[ 0 ];
        KeyPair signkeyPair;
        try {
            signkeyPair = this.getKeyPair(signingAlias);
        }
        catch (
            UnrecoverableKeyException |
            KeyStoreException |
            NoSuchAlgorithmException |
            CryptoException e ) {
            throw new KeystoreManagerException("Failed to get signing key", e); //$NON-NLS-1$
        }

        JcaX509CertificateHolder holder;
        try {
            holder = new JcaX509CertificateHolder(signCert);
        }
        catch ( CertificateEncodingException e ) {
            throw new KeystoreManagerException("Failed to parse signing certificate", e); //$NON-NLS-1$
        }

        return doSignInternal(csr, serial, notBefore, notAfter, extensions, signkeyPair, holder.getIssuer());
    }


    @Override
    public X509Certificate generateSelfSigned ( String signingAlias, PKCS10CertificationRequest csr, BigInteger serial, DateTime notBefore,
            DateTime notAfter, Set<CertExtension> extensions ) throws KeystoreManagerException {
        KeyPair signkeyPair;
        try {
            signkeyPair = this.getKeyPair(signingAlias);
        }
        catch (
            UnrecoverableKeyException |
            KeyStoreException |
            NoSuchAlgorithmException |
            CryptoException e ) {
            throw new KeystoreManagerException("Failed to get signing key", e); //$NON-NLS-1$
        }

        return doSignInternal(csr, serial, notBefore, notAfter, extensions, signkeyPair, csr.getSubject());
    }


    /**
     * @param csr
     * @param serial
     * @param notBefore
     * @param notAfter
     * @param extensions
     * @param signkeyPair
     * @param holder
     * @return
     * @throws KeystoreManagerException
     */
    private X509Certificate doSignInternal ( PKCS10CertificationRequest csr, BigInteger serial, DateTime notBefore, DateTime notAfter,
            Set<CertExtension> extensions, KeyPair signkeyPair, X500Name signSubject ) throws KeystoreManagerException {
        try {
            X509Certificate signed = this.x509util
                    .signCertificate(this.getProvider(), csr, signSubject, signkeyPair, serial, notBefore.toDate(), notAfter.toDate(), extensions);

            if ( log.isDebugEnabled() ) {
                log.debug("Signed certificate is " + this.x509util.toPEM(signed)); //$NON-NLS-1$
            }

            signed.verify(signkeyPair.getPublic());
            return signed;
        }
        catch (
            CryptoException |
            InvalidKeyException |
            CertificateException |
            NoSuchAlgorithmException |
            NoSuchProviderException |
            SignatureException e ) {
            throw new KeystoreManagerException("Failed to sign certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @throws KeystoreManagerException
     */
    @Override
    public void deleteKey ( String alias ) throws KeystoreManagerException {
        try {
            this.pkcs11util.removeKey(this.provider, this.pkcs11util.getKeyStore(this.provider, this.pin), alias);
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to remove key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @param chain
     * @throws KeystoreManagerException
     */
    @Override
    public void updateCertificateChain ( String alias, X509Certificate[] chain ) throws KeystoreManagerException {
        try {
            KeyStore keyStore = this.pkcs11util.getKeyStore(this.provider, this.pin);
            this.pkcs11util
                    .updateCertificate(this.provider, keyStore, alias, realOrFakeCertificateChain(alias, this.provider, getKeyPair(alias), chain));
        }
        catch (
            CryptoException |
            UnrecoverableKeyException |
            KeyStoreException |
            NoSuchAlgorithmException e ) {
            throw new KeystoreManagerException("Failed to update certificate chain", e); //$NON-NLS-1$
        }
    }


    /**
     * @param alias
     * @param chain
     * @return
     * @throws CryptoException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    private X509Certificate[] realOrFakeCertificateChain ( String alias, Provider p, KeyPair kp, X509Certificate[] chain )
            throws CryptoException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        X509Certificate[] actualChain = chain;
        if ( chain == null || chain.length == 0 ) {
            actualChain = new X509Certificate[] {
                this.makeFakeCertificate(p, kp)
            };
        }
        return actualChain;
    }


    protected X509Certificate makeFakeCertificate ( Provider p, KeyPair kp ) throws CryptoException {
        X500Name dn = ( new X500NameBuilder(BCStyle.INSTANCE) ).addRDN(BCStyle.CN, "fake").build(); //$NON-NLS-1$
        Date alreadyExpired = DateTime.now().minusYears(10).toDate();
        Collection<CertExtension> exts = this.x509util.getDefaultClientExtensions(0, new KeyPurposeId[] {}, null);
        return this.x509util.selfSignCertificate(p, kp, dn, BigInteger.ZERO, alreadyExpired, alreadyExpired, exts);
    }


    /**
     * @param primary
     * @return
     */
    protected boolean isFakeCertificate ( X509Certificate primary ) {
        return Arrays.equals(primary.getKeyUsage(), new boolean[] {
            false, false, false, false, false, false, false, false, false
        });
    }

}

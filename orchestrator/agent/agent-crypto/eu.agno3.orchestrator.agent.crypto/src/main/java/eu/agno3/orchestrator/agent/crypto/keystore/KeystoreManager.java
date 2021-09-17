/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore;


import java.math.BigInteger;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.security.AuthProvider;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.x509.CertExtension;


/**
 * @author mbechler
 *
 */
public interface KeystoreManager extends AutoCloseable {

    /**
     * 
     * @return the user that owns this keystore
     * @throws KeystoreManagerException
     */
    UserPrincipal getKeystoreUser () throws KeystoreManagerException;


    /**
     * 
     * @return the group that has access to this keystore
     * @throws KeystoreManagerException
     */
    GroupPrincipal getKeystoreGroup () throws KeystoreManagerException;


    /**
     * Set a new certificate chain for the key
     * 
     * @param alias
     * @param chain
     * @throws KeystoreManagerException
     */
    void updateCertificateChain ( String alias, X509Certificate[] chain ) throws KeystoreManagerException;


    /**
     * Remove a key
     * 
     * @param alias
     * @throws KeystoreManagerException
     */
    void deleteKey ( String alias ) throws KeystoreManagerException;


    /**
     * Import a key
     * 
     * @param alias
     * @param kp
     * @param chain
     * @throws KeystoreManagerException
     */
    void importKey ( String alias, KeyPair kp, X509Certificate[] chain ) throws KeystoreManagerException;


    /**
     * Create a new CSR for the key
     * 
     * @param alias
     * @param dn
     * @param exts
     * @param attrs
     * @return a CSR for the specified key and params
     * @throws KeystoreManagerException
     */
    PKCS10CertificationRequest getCSR ( String alias, X500Name dn, Set<CertExtension> exts, Map<ASN1ObjectIdentifier, ASN1Encodable> attrs )
            throws KeystoreManagerException;


    /**
     * Create a new key a generate a certificate request for it
     * 
     * @param alias
     * @param t
     * @param dn
     * @param exts
     * @param attrs
     * @return a CSR for the new key and params
     * @throws KeystoreManagerException
     */
    PKCS10CertificationRequest createWithCSR ( String alias, KeyType t, X500Name dn, Set<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs ) throws KeystoreManagerException;


    /**
     * Create a new self signed certificate
     * 
     * @param alias
     * @param t
     * @param dn
     * @param serial
     * @param lifetimeDays
     * @param exts
     * @throws KeystoreManagerException
     */
    void createSelfSigned ( String alias, KeyType t, X500Name dn, BigInteger serial, int lifetimeDays, Set<CertExtension> exts )
            throws KeystoreManagerException;


    /**
     * List keystore entries
     * 
     * @return a entries in this key store
     * @throws KeystoreManagerException
     */
    List<KeyStoreEntry> listKeys () throws KeystoreManagerException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    void revokeUser ( UserPrincipal user ) throws UnixAccountException, KeystoreManagerException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    void allowUser ( UserPrincipal user ) throws UnixAccountException, KeystoreManagerException;


    /**
     * @return the set of users that can access this keystore (root always implicit)
     * @throws UnixAccountException
     * @throws KeystoreManagerException
     */
    Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KeystoreManagerException;


    /**
     * @return the security provider backing this keystore
     */
    AuthProvider getProvider ();


    /**
     * @return an instance of the managed key store
     * @throws KeystoreManagerException
     */
    KeyStore getKeyStore () throws KeystoreManagerException;


    /**
     * @param alias
     * @return a key entry for the alias, null if none exists
     * @throws KeystoreManagerException
     */
    KeyStoreEntry getEntry ( String alias ) throws KeystoreManagerException;


    /**
     * @param signingAlias
     * @param csr
     * @param serial
     * @param notBefore
     * @param notAfter
     * @param extensions
     * @return the signed certificate
     * @throws KeystoreManagerException
     */
    X509Certificate signUsingKey ( String signingAlias, PKCS10CertificationRequest csr, BigInteger serial, DateTime notBefore, DateTime notAfter,
            Set<CertExtension> extensions ) throws KeystoreManagerException;


    /**
     * @param keyAlias
     * @param kt
     * @param algo
     * @param bitSize
     * @throws KeystoreManagerException
     */
    void generateKey ( String keyAlias, KeyType kt ) throws KeystoreManagerException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws KeystoreManagerException;


    /**
     * @param signingAlias
     * @param csr
     * @param serial
     * @param notBefore
     * @param notAfter
     * @param extensions
     * @return a self signed certificate
     * @throws KeystoreManagerException
     */
    X509Certificate generateSelfSigned ( String signingAlias, PKCS10CertificationRequest csr, BigInteger serial, DateTime notBefore,
            DateTime notAfter, Set<CertExtension> extensions ) throws KeystoreManagerException;


    /**
     * @param keyStoreName
     * @param validationTrustStore
     * @throws KeystoreManagerException
     */
    void setValidationTruststoreName ( String validationTrustStore ) throws KeystoreManagerException;

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.x509;


import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
public interface X509Util {

    /**
     * @param prov
     * @param keyType
     * @return a new key pair
     * @throws CryptoException
     */
    KeyPair generateKey ( Provider prov, KeyType keyType ) throws CryptoException;


    /**
     * @param p
     * @param algo
     * @param params
     * @return a new key pair
     * @throws CryptoException
     */
    KeyPair generateKey ( Provider p, String algo, AlgorithmParameterSpec params ) throws CryptoException;


    /**
     * @param prov
     * @param toSign
     *            public key to sign
     * @param signerDn
     *            issuer DN
     * @param signKey
     *            issuer key used for signature
     * @param dn
     *            subject DN
     * @param serial
     *            serial number
     * @param notBefore
     * @param notAfter
     * @param exts
     * @return the certificate
     * @throws CryptoException
     */
    X509Certificate signCertificate ( Provider prov, PublicKey toSign, X500Name signerDn, KeyPair signKey, X500Name dn, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException;


    /**
     * @param maxPathLength
     * @return basicConstraints and keyUsage extensions for CA operation
     */
    Collection<CertExtension> getDefaultCAExtensions ( int maxPathLength );


    /**
     * @param keyUsage
     * @param eku
     * @param sans
     * @return basicConstraints, keyUsage and optionally EKU and SAN extensions
     */
    Collection<CertExtension> getDefaultClientExtensions ( int keyUsage, KeyPurposeId[] eku, GeneralName[] sans );


    /**
     * @param prov
     * @param kp
     *            key pair
     * @param dn
     *            subject DN
     * @param serial
     *            serial number
     * @param notBefore
     * @param notAfter
     * @param exts
     * @return the certificate
     * @throws CryptoException
     */
    X509Certificate selfSignCertificate ( Provider prov, KeyPair kp, X500Name dn, BigInteger serial, Date notBefore, Date notAfter,
            Collection<CertExtension> exts ) throws CryptoException;


    /**
     * @param cert
     * @return a PEM encoded certificate
     * @throws CertificateEncodingException
     */
    String toPEM ( X509Certificate cert ) throws CertificateEncodingException;


    /**
     * @param csr
     * @return a PEM encoded CSR
     * @throws IOException
     */
    String toPEM ( PKCS10CertificationRequest csr ) throws IOException;


    /**
     * @param crl
     * @return a PEM encoded CRL
     * @throws CRLException
     */
    String toPEM ( X509CRL crl ) throws CRLException;


    /**
     * @param prov
     * @param kp
     * @param requestDn
     * @param exts
     * @param attrs
     * @return a certificate request
     * @throws CryptoException
     */
    PKCS10CertificationRequest generateCSR ( Provider prov, KeyPair kp, X500Name requestDn, Collection<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs ) throws CryptoException;


    /**
     * Sign a certificate from CSR
     * 
     * Copies only the subject DN
     * 
     * @param prov
     * @param csr
     * @param signerDn
     * @param signKey
     * @param serial
     * @param notBefore
     * @param notAfter
     * @param exts
     * @return the signed certificate
     * @throws CryptoException
     */
    X509Certificate signCertificate ( Provider prov, PKCS10CertificationRequest csr, X500Name signerDn, KeyPair signKey, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException;


    /**
     * @param provider
     * @param issuerKeyPair
     * @param issuerName
     * @param one
     * @param thisUpdate
     * @param nextUpdate
     * @param entries
     * @param exts
     * @return the generated CRL
     * @throws CryptoException
     */
    X509CRL generateCRL ( Provider provider, KeyPair issuerKeyPair, X500Name issuerName, BigInteger one, DateTime thisUpdate, DateTime nextUpdate,
            List<CRLEntry> entries, Collection<CertExtension> exts ) throws CryptoException;


    /**
     * @param crl
     * @return the crl entries from the given crl
     * @throws CryptoException
     */
    List<CRLEntry> getCRLEntries ( X509CRL crl ) throws CryptoException;


    /**
     * @param serial
     * @param revocationDate
     * @param reason
     * @return a crl entry
     * @throws CryptoException
     */
    CRLEntry makeCRLEntry ( BigInteger serial, DateTime revocationDate, int reason ) throws CryptoException;


    /**
     * @param cert
     * @param reason
     * @return a crl entry
     * @throws CryptoException
     */
    CRLEntry makeCRLEntry ( X509Certificate cert, int reason ) throws CryptoException;


    /**
     * @return a random serial number
     */
    BigInteger makeRandomSerial ();

}
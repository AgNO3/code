/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.internal;


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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.X509UtilSystemService;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.x509.CRLEntry;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.X509Util;


/**
 * @author mbechler
 *
 */
@Component ( service = SystemService.class )
@SystemServiceType ( X509UtilSystemService.class )
public class X509UtilSystemServiceImpl implements X509UtilSystemService {

    private X509Util delegate;


    @Reference
    protected synchronized void setX509Util ( X509Util util ) {
        this.delegate = util;
    }


    protected synchronized void unsetX509Util ( X509Util util ) {
        if ( this.delegate == util ) {
            this.delegate = null;
        }
    }


    @Override
    public KeyPair generateKey ( Provider prov, KeyType kt ) throws CryptoException {
        return this.delegate.generateKey(prov, kt);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#generateKey(java.security.Provider, java.lang.String,
     *      java.security.spec.AlgorithmParameterSpec)
     */
    @Override
    public KeyPair generateKey ( Provider p, String algo, AlgorithmParameterSpec params ) throws CryptoException {
        return this.delegate.generateKey(p, algo, params);
    }


    @Override
    public X509Certificate signCertificate ( Provider prov, PublicKey toSign, X500Name signerDn, KeyPair signKey, X500Name dn, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException {
        return this.delegate.signCertificate(prov, toSign, signerDn, signKey, dn, serial, notBefore, notAfter, exts);
    }


    @Override
    public Collection<CertExtension> getDefaultCAExtensions ( int maxPathLength ) {
        return this.delegate.getDefaultCAExtensions(maxPathLength);
    }


    @Override
    public Collection<CertExtension> getDefaultClientExtensions ( int keyUsage, KeyPurposeId[] eku, GeneralName[] sans ) {
        return this.delegate.getDefaultClientExtensions(keyUsage, eku, sans);
    }


    @Override
    public X509Certificate selfSignCertificate ( Provider prov, KeyPair kp, X500Name dn, BigInteger serial, Date notBefore, Date notAfter,
            Collection<CertExtension> exts ) throws CryptoException {
        return this.delegate.selfSignCertificate(prov, kp, dn, serial, notBefore, notAfter, exts);
    }


    @Override
    public String toPEM ( X509Certificate cert ) throws CertificateEncodingException {
        return this.delegate.toPEM(cert);
    }


    @Override
    public String toPEM ( PKCS10CertificationRequest csr ) throws IOException {
        return this.delegate.toPEM(csr);
    }


    @Override
    public String toPEM ( X509CRL crl ) throws CRLException {
        return this.delegate.toPEM(crl);
    }


    @Override
    public PKCS10CertificationRequest generateCSR ( Provider prov, KeyPair kp, X500Name requestDn, Collection<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs ) throws CryptoException {
        return this.delegate.generateCSR(prov, kp, requestDn, exts, attrs);
    }


    @Override
    public X509Certificate signCertificate ( Provider prov, PKCS10CertificationRequest csr, X500Name signerDn, KeyPair signKey, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException {
        return this.delegate.signCertificate(prov, csr, signerDn, signKey, serial, notBefore, notAfter, exts);
    }


    @Override
    public X509CRL generateCRL ( Provider provider, KeyPair issuerKeyPair, X500Name issuerName, BigInteger one, DateTime thisUpdate,
            DateTime nextUpdate, List<CRLEntry> entries, Collection<CertExtension> exts ) throws CryptoException {
        return this.delegate.generateCRL(provider, issuerKeyPair, issuerName, one, thisUpdate, nextUpdate, entries, exts);
    }


    @Override
    public List<CRLEntry> getCRLEntries ( X509CRL crl ) throws CryptoException {
        return this.delegate.getCRLEntries(crl);
    }


    @Override
    public CRLEntry makeCRLEntry ( BigInteger serial, DateTime revocationDate, int reason ) throws CryptoException {
        return this.delegate.makeCRLEntry(serial, revocationDate, reason);
    }


    @Override
    public CRLEntry makeCRLEntry ( X509Certificate cert, int reason ) throws CryptoException {
        return this.delegate.makeCRLEntry(cert, reason);
    }


    @Override
    public BigInteger makeRandomSerial () {
        return this.delegate.makeRandomSerial();
    }

}

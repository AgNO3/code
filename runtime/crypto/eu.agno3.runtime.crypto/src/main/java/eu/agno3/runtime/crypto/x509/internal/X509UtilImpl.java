/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.x509.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.x509.CRLEntry;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.CertExtensionImpl;
import eu.agno3.runtime.crypto.x509.X509Util;


/**
 * @author mbechler
 *
 */
@Component ( service = X509Util.class )
public class X509UtilImpl implements X509Util {

    private static final Provider BC = new BouncyCastleProvider();

    private SecureRandom random;
    private SecureRandomProvider randProv;


    /**
     * 
     */
    public X509UtilImpl () {}


    /**
     * @param random
     * 
     */
    public X509UtilImpl ( SecureRandom random ) {
        this.random = random;
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randProv = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randProv == srp ) {
            this.randProv = null;
        }
    }


    /**
     * @return the random
     */
    public SecureRandom getRandom () {
        if ( this.random == null ) {
            this.random = this.randProv.getSecureRandom();
        }
        return this.random;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#generateKey(java.security.Provider,
     *      eu.agno3.runtime.crypto.keystore.KeyType)
     */
    @Override
    public KeyPair generateKey ( Provider prov, KeyType kt ) throws CryptoException {
        return generateKey(prov, kt.getAlgo(), kt.getParams()); // $NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#generateKey(java.security.Provider, java.lang.String,
     *      java.security.spec.AlgorithmParameterSpec)
     */
    @Override
    public KeyPair generateKey ( Provider p, String algo, AlgorithmParameterSpec params ) throws CryptoException {
        try {
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance(algo, p);
            kpGen.initialize(params, getRandom());
            return kpGen.generateKeyPair();
        }
        catch (
            NoSuchAlgorithmException |
            ProviderException |
            InvalidAlgorithmParameterException e ) {
            throw new CryptoException("Could not generate key", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#signCertificate(java.security.Provider, java.security.PublicKey,
     *      org.bouncycastle.asn1.x500.X500Name, java.security.KeyPair, org.bouncycastle.asn1.x500.X500Name,
     *      java.math.BigInteger, java.util.Date, java.util.Date, java.util.Collection)
     */
    @Override
    public X509Certificate signCertificate ( Provider prov, PublicKey toSign, X500Name signerDn, KeyPair signKey, X500Name dn, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException {
        try {
            ContentSigner signer = new JcaContentSignerBuilder(getSignatureAlg(signKey)).setProvider(prov).build(signKey.getPrivate());
            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(signerDn, serial, notBefore, notAfter, dn, toSign);
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(BC);

            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(signer.getAlgorithmIdentifier());

            // extensions
            exts.addAll(getKeyIdentifierExtensions(signKey.getPublic(), toSign, digAlgId));
            for ( CertExtension ext : exts ) {
                builder.addExtension(ext.getObjectIdentifier(), ext.isCritical(), ext.getExtension());
            }

            return converter.getCertificate(builder.build(signer));
        }
        catch (
            OperatorCreationException |
            CertificateException |
            ProviderException |
            IOException e ) {
            throw new CryptoException("Failed to sign certificate", e); //$NON-NLS-1$
        }
    }


    private static String getSignatureAlg ( KeyPair signKey ) throws CryptoException {
        PublicKey pub = signKey.getPublic();
        if ( pub instanceof RSAPublicKey ) {
            return "SHA512withRSA"; //$NON-NLS-1$
        }
        else if ( pub instanceof ECPublicKey ) {
            return "SHA512withECDSA"; //$NON-NLS-1$
        }
        throw new CryptoException("Failed to determine signature algorithm for " + pub.getAlgorithm()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#signCertificate(java.security.Provider,
     *      org.bouncycastle.pkcs.PKCS10CertificationRequest, org.bouncycastle.asn1.x500.X500Name,
     *      java.security.KeyPair, java.math.BigInteger, java.util.Date, java.util.Date, java.util.Collection)
     */
    @Override
    public X509Certificate signCertificate ( Provider prov, PKCS10CertificationRequest csr, X500Name signerDn, KeyPair signKey, BigInteger serial,
            Date notBefore, Date notAfter, Collection<CertExtension> exts ) throws CryptoException {

        PublicKey pk;
        try {
            JcaPKCS10CertificationRequest jcaCSR = new JcaPKCS10CertificationRequest(csr);
            pk = jcaCSR.getPublicKey();
        }
        catch (
            InvalidKeyException |
            NoSuchAlgorithmException e ) {
            throw new CryptoException("Failed to get public key from certificate request", e); //$NON-NLS-1$
        }

        return signCertificate(prov, pk, signerDn, signKey, csr.getSubject(), serial, notBefore, notAfter, exts);
    }


    /**
     * @param prov
     * @param kp
     * @param requestDn
     * @param exts
     * @param attrs
     * @return a certificate request object
     * @throws CryptoException
     */
    @Override
    public PKCS10CertificationRequest generateCSR ( Provider prov, KeyPair kp, X500Name requestDn, Collection<CertExtension> exts,
            Map<ASN1ObjectIdentifier, ASN1Encodable> attrs ) throws CryptoException {
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(requestDn, kp.getPublic());
        try {
            ContentSigner signer = new JcaContentSignerBuilder(getSignatureAlg(kp)).setProvider(prov).build(kp.getPrivate());
            ExtensionsGenerator extGenerator = new ExtensionsGenerator();

            if ( exts != null && !exts.isEmpty() ) {
                for ( CertExtension ext : exts ) {
                    extGenerator.addExtension(ext.getObjectIdentifier(), ext.isCritical(), ext.getExtension());
                }
                p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGenerator.generate());
            }

            if ( attrs != null ) {
                for ( Entry<ASN1ObjectIdentifier, ASN1Encodable> attr : attrs.entrySet() ) {
                    p10Builder.addAttribute(attr.getKey(), attr.getValue());
                }
            }
            return p10Builder.build(signer);
        }
        catch (
            OperatorCreationException |
            ProviderException |
            IOException e ) {
            throw new CryptoException("Failed to generate CSR", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#generateCRL(java.security.Provider, java.security.KeyPair,
     *      org.bouncycastle.asn1.x500.X500Name, java.math.BigInteger, org.joda.time.DateTime, org.joda.time.DateTime,
     *      java.util.List, java.util.Collection)
     */
    @Override
    public X509CRL generateCRL ( Provider provider, KeyPair issuerKeyPair, X500Name issuerName, BigInteger one, DateTime thisUpdate,
            DateTime nextUpdate, List<CRLEntry> entries, Collection<CertExtension> exts ) throws CryptoException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
            X509v2CRLBuilder builder = new X509v2CRLBuilder(issuerName, thisUpdate.toDate());
            ContentSigner signer = new JcaContentSignerBuilder(getSignatureAlg(issuerKeyPair)).setProvider(provider)
                    .build(issuerKeyPair.getPrivate());

            if ( nextUpdate != null ) {
                builder.setNextUpdate(nextUpdate.toDate());
            }

            for ( CertExtension ext : exts ) {
                builder.addExtension(ext.getObjectIdentifier(), ext.isCritical(), ext.getExtension());
            }

            for ( CRLEntry e : entries ) {
                builder.addCRLEntry(e.getSerial(), e.getRevocationDate().toDate(), e.getExtensions());
            }

            X509CRLHolder crl = builder.build(signer);
            return (X509CRL) cf.generateCRL(new ByteArrayInputStream(crl.getEncoded()));
        }
        catch (
            IOException |
            CRLException |
            ProviderException |
            CertificateException |
            OperatorCreationException e ) {
            throw new CryptoException("Failed to generate CRL", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#getCRLEntries(java.security.cert.X509CRL)
     */
    @Override
    public List<CRLEntry> getCRLEntries ( X509CRL crl ) throws CryptoException {
        try {
            List<CRLEntry> res = new ArrayList<>();
            for ( X509CRLEntry e : crl.getRevokedCertificates() ) {
                org.bouncycastle.asn1.x509.TBSCertList.CRLEntry bcEntry;

                bcEntry = org.bouncycastle.asn1.x509.TBSCertList.CRLEntry.getInstance(e.getEncoded());

                res.add(new CRLEntryImpl(e.getSerialNumber(), new DateTime(e.getRevocationDate()), bcEntry.getExtensions()));
            }
            return res;
        }
        catch ( CRLException e ) {
            throw new CryptoException("Failed to parse CRL", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#makeCRLEntry(java.math.BigInteger, org.joda.time.DateTime, int)
     */
    @Override
    public CRLEntry makeCRLEntry ( BigInteger serial, DateTime revocationDate, int reason ) {
        return new CRLEntryImpl(serial, revocationDate, reason);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#makeCRLEntry(java.security.cert.X509Certificate, int)
     */
    @Override
    public CRLEntry makeCRLEntry ( X509Certificate cert, int reason ) {
        return new CRLEntryImpl(cert.getSerialNumber(), DateTime.now(), reason);
    }


    private static Collection<? extends CertExtension> getKeyIdentifierExtensions ( PublicKey issuerPublic, PublicKey subjectPublic,
            AlgorithmIdentifier digAlgId ) throws IOException, OperatorCreationException {
        Set<CertExtension> exts = new HashSet<>();
        SubjectPublicKeyInfo subjectPubInfo = SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(subjectPublic.getEncoded()));
        SubjectPublicKeyInfo issuerPubInfo = SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(issuerPublic.getEncoded()));
        DigestCalculatorProvider digestBuilder = new JcaDigestCalculatorProviderBuilder().setProvider(BC).build();
        X509ExtensionUtils extUtil = new X509ExtensionUtils(digestBuilder.get(digAlgId));

        exts.add(new CertExtensionImpl(Extension.authorityKeyIdentifier, false, extUtil.createAuthorityKeyIdentifier(issuerPubInfo)));
        exts.add(new CertExtensionImpl(Extension.subjectKeyIdentifier, false, extUtil.createSubjectKeyIdentifier(subjectPubInfo)));

        return exts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#getDefaultCAExtensions(int)
     */
    @Override
    public Collection<CertExtension> getDefaultCAExtensions ( int maxPathLength ) {
        Set<CertExtension> exts = new HashSet<>();
        exts.add(new CertExtensionImpl(Extension.basicConstraints, true, new BasicConstraints(maxPathLength)));
        exts.add(new CertExtensionImpl(Extension.keyUsage, true, new KeyUsage(KeyUsage.cRLSign | KeyUsage.digitalSignature | KeyUsage.keyCertSign)));
        return exts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#getDefaultClientExtensions(int,
     *      org.bouncycastle.asn1.x509.KeyPurposeId[], org.bouncycastle.asn1.x509.GeneralName[])
     */
    @Override
    public Collection<CertExtension> getDefaultClientExtensions ( int keyUsage, KeyPurposeId[] eku, GeneralName[] sans ) {
        Set<CertExtension> exts = new HashSet<>();

        exts.add(new CertExtensionImpl(Extension.basicConstraints, true, new BasicConstraints(false)));
        exts.add(new CertExtensionImpl(Extension.keyUsage, true, new KeyUsage(keyUsage)));

        if ( eku != null && eku.length > 0 ) {
            exts.add(new CertExtensionImpl(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(eku)));
        }
        if ( sans != null && sans.length > 0 ) {
            exts.add(new CertExtensionImpl(Extension.subjectAlternativeName, true, new GeneralNames(sans)));
        }

        return exts;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#selfSignCertificate(java.security.Provider, java.security.KeyPair,
     *      org.bouncycastle.asn1.x500.X500Name, java.math.BigInteger, java.util.Date, java.util.Date,
     *      java.util.Collection)
     */
    @Override
    public X509Certificate selfSignCertificate ( Provider prov, KeyPair kp, X500Name dn, BigInteger serial, Date notBefore, Date notAfter,
            Collection<CertExtension> exts ) throws CryptoException {
        return signCertificate(prov, kp.getPublic(), dn, kp, dn, serial, notBefore, notAfter, exts);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#makeRandomSerial()
     */
    @Override
    public BigInteger makeRandomSerial () {
        byte[] r = new byte[16];
        this.getRandom().nextBytes(r);
        return new BigInteger(r);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#toPEM(java.security.cert.X509Certificate)
     */
    @Override
    public String toPEM ( X509Certificate cert ) throws CertificateEncodingException {
        StringBuilder sb = new StringBuilder();
        String encoded = Base64.encodeBase64String(cert.getEncoded());
        sb.append("-----BEGIN CERTIFICATE-----").append('\n'); //$NON-NLS-1$
        final int chunkSize = 60;
        for ( int pos = 0; pos < encoded.length(); pos += chunkSize ) {
            sb.append(encoded.substring(pos, Math.min(encoded.length(), pos + chunkSize))).append('\n');
        }
        sb.append("-----END CERTIFICATE-----").append('\n'); //$NON-NLS-1$
        return sb.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#toPEM(org.bouncycastle.pkcs.PKCS10CertificationRequest)
     */
    @Override
    public String toPEM ( PKCS10CertificationRequest csr ) throws IOException {
        StringBuilder sb = new StringBuilder();
        String encoded = Base64.encodeBase64String(csr.getEncoded());
        sb.append("-----BEGIN CERTIFICATE REQUEST-----").append('\n'); //$NON-NLS-1$
        final int chunkSize = 60;
        for ( int pos = 0; pos < encoded.length(); pos += chunkSize ) {
            sb.append(encoded.substring(pos, Math.min(encoded.length(), pos + chunkSize))).append('\n');
        }
        sb.append("-----END CERTIFICATE REQUEST-----").append('\n'); //$NON-NLS-1$
        return sb.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CRLException
     *
     * @see eu.agno3.runtime.crypto.x509.X509Util#toPEM(java.security.cert.X509CRL)
     */
    @Override
    public String toPEM ( X509CRL crl ) throws CRLException {
        StringBuilder sb = new StringBuilder();
        String encoded = Base64.encodeBase64String(crl.getEncoded());
        sb.append("-----BEGIN X509 CRL-----").append('\n'); //$NON-NLS-1$
        final int chunkSize = 60;
        for ( int pos = 0; pos < encoded.length(); pos += chunkSize ) {
            sb.append(encoded.substring(pos, Math.min(encoded.length(), pos + chunkSize))).append('\n');
        }
        sb.append("-----END X509 CRL-----").append('\n'); //$NON-NLS-1$
        return sb.toString();
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs7.internal;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.SignerInformationVerifierProvider;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
final class TrustCheckingVerifierProvider implements SignerInformationVerifierProvider {

    private static final Logger log = Logger.getLogger(TrustCheckingVerifierProvider.class);

    private Collection<Certificate> certs;
    private Store<X509CertificateHolder> store;

    private TrustChecker checker;
    private TrustConfiguration tc;
    private boolean[] keyUsage;
    private Set<String> ekus;
    private JcaX509CertificateConverter jcaX509CertificateConverter = new JcaX509CertificateConverter();

    private Date date;


    /**
     * @param tc
     * @param checker
     * @param certs
     * @param date
     * @param keyUsage
     * @param ekus
     * 
     */
    public TrustCheckingVerifierProvider ( TrustConfiguration tc, TrustChecker checker, Store<X509CertificateHolder> certs, DateTime date,
            boolean[] keyUsage, Set<String> ekus ) {
        this.tc = tc;
        this.checker = checker;
        this.keyUsage = Arrays.copyOf(keyUsage, keyUsage.length);
        this.ekus = new HashSet<>(ekus);
        this.store = certs;
        this.certs = extractCertificates(certs);
        this.date = date != null ? date.toDate() : null;
    }


    /**
     * @param certificates
     * @return
     */
    private static final Set<Certificate> extractCertificates ( Store<X509CertificateHolder> certificates ) {
        Collection<X509CertificateHolder> allCerts = certificates.getMatches(new AnyMatchSelector());
        JcaX509CertificateConverter jcaX509CertificateConverter = new JcaX509CertificateConverter();
        Set<Certificate> certs = new HashSet<>();

        for ( X509CertificateHolder h : allCerts ) {
            try {
                certs.add(jcaX509CertificateConverter.getCertificate(h));
            }
            catch ( CertificateException e ) {
                log.warn("Failed to read certificate", e); //$NON-NLS-1$
            }
        }

        return certs;
    }


    @Override
    public SignerInformationVerifier get ( SignerId signerId ) throws OperatorCreationException {
        @SuppressWarnings ( "unchecked" )
        Collection<X509CertificateHolder> matches = this.store.getMatches(signerId);
        for ( X509CertificateHolder h : matches ) {
            try {
                X509Certificate cert = this.jcaX509CertificateConverter.getCertificate(h);
                PKIXCertPathBuilderResult validate = this.checker.validate(this.tc, cert, this.certs, this.date, this.keyUsage, this.ekus);
                if ( log.isDebugEnabled() ) {
                    log.debug("Validation result is " + validate); //$NON-NLS-1$
                }
                return checkSignature(cert);
            }
            catch (
                CertificateException |
                CryptoException e ) {
                log.warn("Invalid signer certificate for " + h.getSubject(), e); //$NON-NLS-1$
                continue;
            }
        }

        try {
            return invalidSignature();
        }
        catch ( CryptoException e ) {
            throw new OperatorCreationException("Failed to create default verifier", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param signer
     * @return signer information verifier
     * @throws CryptoException
     */
    private static SignerInformationVerifier checkSignature ( X509Certificate signer ) throws CryptoException {
        try {
            CMSSignatureAlgorithmNameGenerator sigNameGenerator = new DefaultCMSSignatureAlgorithmNameGenerator();
            SignatureAlgorithmIdentifierFinder sigAlgorithmFinder = new DefaultSignatureAlgorithmIdentifierFinder();
            ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder().build(signer);
            DigestCalculatorProvider digestProvider = new JcaDigestCalculatorProviderBuilder().build();
            return new SignerInformationVerifier(sigNameGenerator, sigAlgorithmFinder, verifierProvider, digestProvider);
        }
        catch ( OperatorCreationException e ) {
            throw new CryptoException(e);
        }
    }


    /**
     * 
     * @return signer information verifier, always returning false
     * @throws CryptoException
     */
    public static SignerInformationVerifier invalidSignature () throws CryptoException {
        try {
            CMSSignatureAlgorithmNameGenerator sigNameGenerator = new DefaultCMSSignatureAlgorithmNameGenerator();
            SignatureAlgorithmIdentifierFinder sigAlgorithmFinder = new DefaultSignatureAlgorithmIdentifierFinder();
            InvalidContentVerifierProvider verifierProvider = new InvalidContentVerifierProvider();
            DigestCalculatorProvider digestProvider = new JcaDigestCalculatorProviderBuilder().build();
            return new SignerInformationVerifier(sigNameGenerator, sigAlgorithmFinder, verifierProvider, digestProvider);
        }
        catch ( OperatorCreationException e ) {
            throw new CryptoException(e);
        }
    }

    private static final class InvalidContentVerifierProvider implements ContentVerifierProvider {

        /**
         * 
         */
        public InvalidContentVerifierProvider () {}


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifierProvider#get(org.bouncycastle.asn1.x509.AlgorithmIdentifier)
         */
        @Override
        public ContentVerifier get ( AlgorithmIdentifier algId ) throws OperatorCreationException {
            return new InvalidContentVerifier(algId);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifierProvider#getAssociatedCertificate()
         */
        @Override
        public X509CertificateHolder getAssociatedCertificate () {
            return null;
        }


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifierProvider#hasAssociatedCertificate()
         */
        @Override
        public boolean hasAssociatedCertificate () {
            return false;
        }

    }

    private static final class InvalidContentVerifier implements ContentVerifier {

        private AlgorithmIdentifier algId;


        /**
         * @param algId
         * 
         */
        public InvalidContentVerifier ( AlgorithmIdentifier algId ) {
            this.algId = algId;
        }


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifier#getAlgorithmIdentifier()
         */
        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier () {
            return this.algId;
        }


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifier#getOutputStream()
         */
        @Override
        public OutputStream getOutputStream () {
            return new ByteArrayOutputStream();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.bouncycastle.operator.ContentVerifier#verify(byte[])
         */
        @Override
        public boolean verify ( byte[] arg0 ) {
            return false;
        }

    }

    /**
     * @author mbechler
     *
     */
    private static final class AnyMatchSelector implements Selector<X509CertificateHolder> {

        /**
         * 
         */
        public AnyMatchSelector () {}


        @Override
        public boolean match ( X509CertificateHolder o ) {
            return true;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone () {
            return new AnyMatchSelector();
        }
    }
}
/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.AnnotatedException;

import eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class RevocationPathChecker extends PKIXCertPathChecker {

    private static final Logger log = Logger.getLogger(RevocationPathChecker.class);

    private final RevocationConfig revocationConfig;
    private final PKIXParameters params;
    private final CRLPathChecker crlChecker;
    private final OCSPPathChecker ocspChecker;

    private List<X509Certificate> chain = null;


    /**
     * @param revocationConfig
     * @param params
     * @param crlChecker
     * @param ocspChecker
     */
    public RevocationPathChecker ( RevocationConfig revocationConfig, PKIXParameters params, CRLPathChecker crlChecker, OCSPPathChecker ocspChecker ) {
        this.revocationConfig = revocationConfig;
        this.params = params;
        this.crlChecker = crlChecker;
        this.ocspChecker = ocspChecker;
    }


    /**
     * @param revocationConfig
     * @param params
     * @param certChain
     * 
     */
    protected RevocationPathChecker ( RevocationConfig revocationConfig, PKIXParameters params, CRLPathChecker crlChecker,
            OCSPPathChecker ocspChecker, List<X509Certificate> certChain ) {
        this(revocationConfig, params, crlChecker, ocspChecker);
        if ( certChain != null ) {
            this.chain = new ArrayList<>(certChain);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#init(boolean)
     */
    @Override
    public void init ( boolean forward ) throws CertPathValidatorException {
        if ( forward ) {
            throw new CertPathValidatorException("Forward checking not supported"); //$NON-NLS-1$
        }

        this.chain = new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#isForwardCheckingSupported()
     */
    @Override
    public boolean isForwardCheckingSupported () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#getSupportedExtensions()
     */
    @Override
    public Set<String> getSupportedExtensions () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#check(java.security.cert.Certificate, java.util.Collection)
     */
    @Override
    public void check ( Certificate cert, Collection<String> unresolvedCritExts ) throws CertPathValidatorException {

        if ( ! ( cert instanceof X509Certificate ) ) {
            throw new CertPathValidatorException("Not a X509 Certificate"); //$NON-NLS-1$
        }

        if ( this.chain == null ) {
            throw new CertPathValidatorException("Not initialized"); //$NON-NLS-1$
        }

        X509Certificate x509cert = (X509Certificate) cert;

        if ( x509cert.getBasicConstraints() != -1 && this.revocationConfig.isCheckOnlyEndEntityCerts() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not checking as this is not an end entity certificiate " + x509cert.getSubjectDN().getName()); //$NON-NLS-1$
            }
            this.chain.add(x509cert);
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Found " + x509cert.getSubjectDN().getName()); //$NON-NLS-1$
        }

        Set<TrustAnchor> tas = new HashSet<>(this.params.getTrustAnchors());
        for ( X509Certificate alreadyChecked : this.chain ) {
            tas.add(new TrustAnchor(alreadyChecked, null));
        }

        try {
            TrustAnchor ta = CRLUtil.findTrustAnchor(x509cert, tas);
            if ( ta == null ) {
                throw new CertPathValidatorException("Path does not chain with any of the trust anchors", //$NON-NLS-1$
                    null,
                    null,
                    -1,
                    PKIXReason.NO_TRUST_ANCHOR);
            }

            PKIXParameters paramsWithChain = makeNewParams(this.chain, ta);
            this.check(ta, x509cert, paramsWithChain);
            this.chain.add(x509cert);

        }
        catch ( AnnotatedException e ) {
            throw new CertPathValidatorException(e);
        }

    }


    /**
     * @param tas
     * @return
     * @throws CertPathValidatorException
     */
    protected PKIXParameters makeNewParams ( Collection<X509Certificate> localChain, TrustAnchor anchor ) throws CertPathValidatorException {
        PKIXParameters paramsWithChain = (PKIXParameters) this.params.clone();
        Set<X509Certificate> certs = new HashSet<>(localChain);
        certs.add(anchor.getTrustedCert());

        try {
            paramsWithChain.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs))); //$NON-NLS-1$
        }
        catch (
            InvalidAlgorithmParameterException |
            NoSuchAlgorithmException e ) {
            throw new CertPathValidatorException("Failed to construct cert store", e, null, -1, BasicReason.UNSPECIFIED); //$NON-NLS-1$
        }
        return paramsWithChain;
    }


    /**
     * @param anchor
     * @param paramsWithChain
     * @param x509cert
     */
    private void check ( TrustAnchor anchor, X509Certificate cert, PKIXParameters paramsWithChain ) throws CertPathValidatorException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Checking %s", //$NON-NLS-1$ 
                cert.getSubjectDN().getName()));
            log.debug(String.format("trust anchor %s", //$NON-NLS-1$ 
                anchor.getTrustedCert().getSubjectDN().getName()));
        }

        JcaX509CertificateHolder holder;
        try {
            holder = new JcaX509CertificateHolder(cert);
        }
        catch ( CertificateEncodingException e ) {
            throw new CertPathValidatorException("Could not read certificate", e); //$NON-NLS-1$
        }

        X509CRLSelector crlSelector = new X509CRLSelector();
        Set<URI> ocspUris = new HashSet<>();
        Set<URI> extraDps = new HashSet<>();
        boolean hasCRLInfo = this.crlChecker.checkCRLInfo(holder, cert.getIssuerX500Principal(), crlSelector, extraDps, paramsWithChain);
        boolean hasOCSPInfo = this.ocspChecker.hasOCSPResponders(holder, ocspUris, paramsWithChain);
        boolean haveSystemOCSP = this.revocationConfig.getSystemOCSPUri() != null;

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Have in cert crl:%s ocsp:%s sysocsp=%s", hasCRLInfo, hasOCSPInfo, haveSystemOCSP)); //$NON-NLS-1$
        }
        if ( this.revocationConfig.isCheckOCSP() && ( hasOCSPInfo || haveSystemOCSP ) ) {
            boolean checkSucceeded = this.ocspChecker.doCheckOCSP(cert, getIssuerCert(anchor), ocspUris, paramsWithChain);
            if ( checkSucceeded ) {
                return;
            }
            else if ( this.revocationConfig.isRequireOCSP() ) {
                throw new CertPathValidatorException("OCSP check failed ", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS); //$NON-NLS-1$
            }
        }
        else if ( this.revocationConfig.isRequireOCSP() && ! ( hasOCSPInfo || haveSystemOCSP ) ) {
            throw new CertPathValidatorException("OCSP required ", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS); //$NON-NLS-1$
        }

        if ( this.revocationConfig.isCheckCRL() && ( hasCRLInfo || this.revocationConfig.isRequireCRL() ) ) {
            this.crlChecker.doCheckCRLs(cert, crlSelector, extraDps, paramsWithChain);
        }
        else if ( this.revocationConfig.isRequireCRL() && !hasCRLInfo ) {
            throw new CertPathValidatorException("CRL required ", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS); //$NON-NLS-1$
        }

    }


    /**
     * @param anchor
     * @return
     */
    protected X509Certificate getIssuerCert ( TrustAnchor anchor ) {
        X509Certificate issuerCert;
        if ( !this.chain.isEmpty() ) {
            issuerCert = this.chain.get(this.chain.size() - 1);
        }
        else {
            issuerCert = anchor.getTrustedCert();
        }
        return issuerCert;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#clone()
     */
    @Override
    public RevocationPathChecker clone () {
        return new RevocationPathChecker(this.revocationConfig, this.params, this.crlChecker, this.ocspChecker, this.chain);
    }
}

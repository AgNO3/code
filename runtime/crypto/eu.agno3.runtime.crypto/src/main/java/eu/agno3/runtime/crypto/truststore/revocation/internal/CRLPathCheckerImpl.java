/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.x509.X509CRLStoreSelector;

import eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.DistributionPointCache;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class CRLPathCheckerImpl implements CRLPathChecker {

    private static final Logger log = Logger.getLogger(CRLPathCheckerImpl.class);

    private RevocationConfig revocationConfig;
    private DistributionPointCache dpCache;

    private static final String HTTP = "http"; //$NON-NLS-1$
    private static final String HTTPS = "https"; //$NON-NLS-1$


    /**
     * @param revocationConfig
     * @param dpCache
     * 
     */
    public CRLPathCheckerImpl ( RevocationConfig revocationConfig, DistributionPointCache dpCache ) {
        this.revocationConfig = revocationConfig;
        this.dpCache = dpCache;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker#checkCRLInfo(org.bouncycastle.cert.jcajce.JcaX509CertificateHolder,
     *      javax.security.auth.x500.X500Principal, java.security.cert.X509CRLSelector, java.util.Set,
     *      java.security.cert.PKIXParameters)
     */
    @Override
    public boolean checkCRLInfo ( JcaX509CertificateHolder holder, X500Principal issuer, X509CRLSelector crlSelector, Set<URI> extraDps,
            PKIXParameters paramsWithChain ) throws CertPathValidatorException {
        Extension extension = holder.getExtension(Extension.cRLDistributionPoints);
        if ( extension != null ) {
            CRLDistPoint dps = CRLDistPoint.getInstance(extension.getParsedValue());
            try {
                for ( DistributionPoint dp : dps.getDistributionPoints() ) {
                    CRLUtil.getCRLIssuersFromDistributionPoint(dp, issuer, crlSelector, paramsWithChain);
                    handleDistributionPoint(dp, extraDps);
                }
                return true;
            }
            catch ( AnnotatedException e ) {
                throw new CertPathValidatorException("Invalid distribution point information", e); //$NON-NLS-1$
            }
        }

        crlSelector.setIssuers(Arrays.asList(issuer));
        return false;
    }


    /**
     * @param dp
     * @param extraDps
     */
    private static void handleDistributionPoint ( DistributionPoint dp, Set<URI> extraDps ) {
        DistributionPointName distributionPoint = dp.getDistributionPoint();
        if ( distributionPoint == null ) {
            return;
        }

        GeneralNames names = GeneralNames.getInstance(distributionPoint.getName());
        if ( names == null ) {
            return;
        }

        for ( GeneralName name : names.getNames() ) {
            if ( name.getTagNo() != GeneralName.uniformResourceIdentifier ) {
                continue;
            }

            try {
                URI uri = new URI(name.getName().toString());

                if ( !HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme()) ) {
                    continue;
                }

                extraDps.add(uri);
            }
            catch ( URISyntaxException e ) {
                log.warn("Failed to parse distribution point URI " + name.getName(), e); //$NON-NLS-1$
                continue;
            }

        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker#doCheckCRLs(java.security.cert.X509Certificate,
     *      java.security.cert.X509CRLSelector, java.util.Set, java.security.cert.PKIXParameters)
     */
    @Override
    public void doCheckCRLs ( X509Certificate cert, X509CRLSelector crlSelector, Set<URI> extraDps, PKIXParameters paramsWithChain )
            throws CertPathValidatorException {

        Set<X509CRL> crls = findCRLCandidates(crlSelector, paramsWithChain);
        if ( crls.isEmpty() && this.revocationConfig.isIgnoreUnavailableCRL() ) {
            log.debug("Not checking CRL as none is available and ignoreUnavailableCRL is set"); //$NON-NLS-1$
            return;
        }
        else if ( crls.isEmpty() ) {
            if ( !extraDps.isEmpty() && this.revocationConfig.isDownloadCRLs() ) {
                this.checkIntermediateCRL(cert, crlSelector, extraDps, paramsWithChain);
            }
            throw new CertPathValidatorException(
                "Could not determine revocation status, no CRL available", //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
        else {
            log.debug("Checking CRL"); //$NON-NLS-1$
            for ( X509CRL crl : crls ) {
                try {
                    doCheckCandidate(cert, crl, paramsWithChain);
                }
                catch ( CertPathValidatorException e ) {
                    log.debug("CRL validation failed", e); //$NON-NLS-1$
                    throw e;
                }
            }
        }
    }


    /**
     * @param cert
     * @param crlSelector
     * @param extraDps
     * @param paramsWithChain
     * @throws CertPathValidatorException
     */
    private void checkIntermediateCRL ( X509Certificate cert, X509CRLSelector crlSelector, Set<URI> extraDps, PKIXParameters paramsWithChain )
            throws CertPathValidatorException {
        log.info("Need to check intermediate CRL from " + extraDps); //$NON-NLS-1$

        X509CRL retrieved = tryGetCRLFromDistributionPoints(extraDps, paramsWithChain);

        if ( retrieved == null ) {
            throw new CertPathValidatorException(
                "Could not determine revocation status, could not fetch CRL distribution points", //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }

        doCheckCandidate(cert, retrieved, paramsWithChain);
    }


    /**
     * @param extraDps
     * @param paramsWithChain
     * @return
     */
    protected X509CRL tryGetCRLFromDistributionPoints ( Set<URI> extraDps, PKIXParameters paramsWithChain ) {
        X509CRL retrieved = null;
        for ( URI uri : extraDps ) {
            try {
                retrieved = this.dpCache.getCRL(uri, paramsWithChain);
                if ( retrieved != null ) {
                    break;
                }
            }
            catch (
                IOException |
                CRLException e ) {
                log.warn("Failed to load CRL from distribution point", e); //$NON-NLS-1$
            }
        }
        return retrieved;
    }


    /**
     * @param cert
     * @param crl
     * @param paramsWithChain
     * @throws CertPathValidatorException
     */
    protected void doCheckCandidate ( X509Certificate cert, X509CRL crl, PKIXParameters paramsWithChain ) throws CertPathValidatorException {
        this.validateCRL(crl, paramsWithChain);
        IssuingDistributionPoint crlDP;
        try {
            crlDP = getIssuingDistributionPoint(crl);
        }
        catch ( IOException e ) {
            throw new CertPathValidatorException("Failed to parse CRL extensions", e); //$NON-NLS-1$
        }

        boolean indirect = false;
        int reasonMask = -1;
        if ( crlDP != null ) {
            if ( crlDP.isIndirectCRL() ) {
                indirect = true;
            }

            if ( isNotApplicableDistributionPoint(cert, crlDP) ) {
                return;
            }

            ReasonFlags rf = crlDP.getOnlySomeReasons();

            if ( rf != null ) {
                reasonMask = rf.intValue();
            }
        }

        X509CRLEntry revokedCertificate = crl.getRevokedCertificate(cert);
        if ( revokedCertificate != null ) {
            checkCRLEntry(revokedCertificate, indirect, reasonMask, cert, paramsWithChain);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Certificate not found in CRL " + cert.getSubjectDN().getName()); //$NON-NLS-1$
        }
    }


    /**
     * @param revokedCertificate
     * @param indirect
     * @param reasonMask
     * @param cert
     * @throws CertPathValidatorException
     */
    private static void checkCRLEntry ( X509CRLEntry revokedCertificate, boolean indirect, int reasonMask, X509Certificate cert,
            PKIXParameters paramsWithChain ) throws CertPathValidatorException {

        Set<String> criticalExtensions = revokedCertificate.getCriticalExtensionOIDs();
        if ( criticalExtensions != null && !criticalExtensions.isEmpty() ) {
            throw new CertPathValidatorException(
                "CRL entry contains unsupported critical extensions " + criticalExtensions, //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }

        Date revocationDate = revokedCertificate.getRevocationDate();

        Date checkDate = paramsWithChain.getDate();
        if ( checkDate == null ) {
            checkDate = new Date();
        }
        if ( revocationDate != null && checkDate.before(revocationDate) ) {
            return;
        }

        throw new CertPathValidatorException("Certificate is revoked by CRL", null, null, -1, BasicReason.REVOKED); //$NON-NLS-1$
    }


    /**
     * @param cert
     * @param crlDP
     * @return
     */
    protected boolean isNotApplicableDistributionPoint ( X509Certificate cert, IssuingDistributionPoint crlDP ) {
        return crlDP.onlyContainsAttributeCerts() || ( crlDP.onlyContainsCACerts() && cert.getBasicConstraints() == -1 )
                || ( crlDP.onlyContainsUserCerts() && cert.getBasicConstraints() != -1 );
    }


    /**
     * @param crl
     * @return
     * @throws IOException
     */
    protected IssuingDistributionPoint getIssuingDistributionPoint ( X509CRL crl ) throws IOException {
        byte[] crlDPExt = crl.getExtensionValue(Extension.issuingDistributionPoint.toString());
        if ( crlDPExt != null ) {
            DEROctetString str = (DEROctetString) ASN1Primitive.fromByteArray(crlDPExt);
            return IssuingDistributionPoint.getInstance(str.getOctets());
        }
        return null;
    }


    /**
     * @param crl
     * @return
     * @throws IOException
     */
    protected CRLNumber getCRLNumberExt ( X509CRL crl ) throws IOException {
        byte[] crlNumExt = crl.getExtensionValue(Extension.cRLNumber.toString());
        if ( crlNumExt != null ) {
            DEROctetString str = (DEROctetString) ASN1Primitive.fromByteArray(crlNumExt);
            return CRLNumber.getInstance(str);
        }
        return null;
    }


    /**
     * @param crlSelector
     * @return
     * @throws CertPathValidatorException
     */
    protected Set<X509CRL> findCRLCandidates ( X509CRLSelector crlSelector, PKIXParameters paramsWithChain ) throws CertPathValidatorException {
        Set<X509CRL> crls;
        try {
            crls = CRLUtil.findCRLs(X509CRLStoreSelector.getInstance(crlSelector), paramsWithChain);
        }
        catch ( AnnotatedException e ) {
            log.debug("Failed to get CRLs", e); //$NON-NLS-1$
            throw new CertPathValidatorException("Error obtaining CRLs", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS); //$NON-NLS-1$
        }
        return crls;
    }


    /**
     * @param crl
     * @param paramsWithChain
     * @throws CertPathValidatorException
     * @throws IOException
     * @throws CRLException
     */
    private void validateCRL ( X509CRL crl, PKIXParameters paramsWithChain ) throws CertPathValidatorException {
        Date now = new Date();

        if ( !this.revocationConfig.isIgnoreExpiredCRL() && crl.getNextUpdate().before(now) ) {
            throw new CertPathValidatorException(
                "Could not determine revocation status, expired CRL found in store", //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }

        checkCRLCriticalExtensions(crl);
        X509CertSelector selector = new X509CertSelector();
        selector.setSubject(crl.getIssuerX500Principal());

        X509CRLHolder holder;
        try {
            holder = new X509CRLHolder(crl.getEncoded());
        }
        catch (
            IOException |
            CRLException e ) {
            log.debug("Could not determine revocation status", e); //$NON-NLS-1$
            throw new CertPathValidatorException(
                "Could not determine revocation status, failed to parse CRL", //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
        Extensions exts = holder.getExtensions();
        if ( exts != null ) {
            AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.fromExtensions(exts);
            setupSelector(aki, selector);
        }

        validateIssuer(crl, selector, paramsWithChain);
    }


    /**
     * @param crl
     * @throws CertPathValidatorException
     */
    protected void checkCRLCriticalExtensions ( X509CRL crl ) throws CertPathValidatorException {
        Set<String> criticalExtensionOIDs = crl.getCriticalExtensionOIDs();
        Set<String> criticalOids = new HashSet<>(criticalExtensionOIDs != null ? criticalExtensionOIDs : Collections.EMPTY_SET);
        criticalOids.remove(Extension.issuingDistributionPoint.toString());
        criticalOids.remove(Extension.cRLNumber.toString());

        if ( !criticalOids.isEmpty() ) {
            throw new CertPathValidatorException(
                "Could not determine revocation status, unsupported critical extensions in CRL " + criticalOids, //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
    }


    /**
     * @param crl
     * @param selector
     * @param paramsWithChain
     * @throws CertPathValidatorException
     */
    protected void validateIssuer ( X509CRL crl, X509CertSelector selector, PKIXParameters paramsWithChain ) throws CertPathValidatorException {

        for ( CertStore cs : paramsWithChain.getCertStores() ) {
            try {

                for ( Certificate cert : cs.getCertificates(selector) ) {
                    X509Certificate x509cert = (X509Certificate) cert;
                    if ( log.isDebugEnabled() ) {
                        log.debug("Found issuer cert for " + x509cert.getSubjectDN().getName()); //$NON-NLS-1$
                    }
                    if ( x509cert.getBasicConstraints() == -1 || !x509cert.getKeyUsage()[ 6 ] ) {
                        log.debug("Not a CA certificate or crlSigning usage, skip"); //$NON-NLS-1$
                        continue;
                    }
                    crl.verify(cert.getPublicKey());
                    return;
                }
            }
            catch (
                CertStoreException |
                InvalidKeyException |
                CRLException |
                NoSuchAlgorithmException |
                NoSuchProviderException |
                SignatureException e ) {
                log.debug("Failed to check CRL against issuer cert", e); //$NON-NLS-1$
            }
        }

        if ( log.isDebugEnabled() ) {
            log.info("No valid issuer certificate found using selector " + selector); //$NON-NLS-1$
        }

        throw new CertPathValidatorException(
            "Could not determine revocation status, failed to validate CRL issuer", //$NON-NLS-1$
            null,
            null,
            -1,
            BasicReason.UNDETERMINED_REVOCATION_STATUS);
    }


    /**
     * @param aki
     * @param selector
     * @throws CertPathValidatorException
     */
    private static void setupSelector ( AuthorityKeyIdentifier aki, X509CertSelector selector ) throws CertPathValidatorException {
        if ( aki.getKeyIdentifier() != null ) {
            SubjectKeyIdentifier subjectKeyIdentifier = new SubjectKeyIdentifier(aki.getKeyIdentifier());
            try {
                selector.setSubjectKeyIdentifier(subjectKeyIdentifier.getEncoded());
            }
            catch ( IOException e ) {
                log.debug("Failed to handle authority key identifier", e); //$NON-NLS-1$
                throw new CertPathValidatorException(
                    "Could not determine revocation status, failed handle authority key identifier", //$NON-NLS-1$
                    null,
                    null,
                    -1,
                    BasicReason.UNDETERMINED_REVOCATION_STATUS);
            }
        }

        if ( aki.getAuthorityCertSerialNumber() != null ) {
            selector.setSerialNumber(aki.getAuthorityCertSerialNumber());
        }
    }

}

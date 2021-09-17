/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.truststore.CRLUpdater;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.truststore.revocation.RemoteLoaderUtil;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = CRLUpdater.class )
public class CRLUpdaterImpl implements CRLUpdater {

    private static final String HTTPS = "https"; //$NON-NLS-1$
    private static final String HTTP = "http"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(CRLUpdaterImpl.class);
    private SecureRandom rand;

    private SecureRandomProvider secureRandomProvider;


    /**
     * 
     */
    public CRLUpdaterImpl () {}


    /**
     * @param rand
     * 
     */
    public CRLUpdaterImpl ( SecureRandom rand ) {
        this.rand = rand;
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
        }
    }


    private SecureRandom getSecureRandom () {
        if ( this.rand == null ) {
            this.rand = this.secureRandomProvider.getSecureRandom();
        }
        return this.rand;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.CRLUpdater#updateCRLsFromDistributionPoints(eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager)
     */
    @Override
    public void updateCRLsFromDistributionPoints ( TruststoreManager tm ) throws TruststoreManagerException {

        RevocationConfig revocationConfig = tm.getRevocationConfig();
        if ( revocationConfig == null || !revocationConfig.isCheckCRL() ) {
            log.debug("Not updating CRLs as CRL checking is disabled on this trust store"); //$NON-NLS-1$
            return;
        }

        Set<X509Certificate> certs = tm.listCertificates();
        Map<X500Principal, X509CRL> currentCrls = buildCRLMap(tm.listCRLs());
        Map<X500Principal, X509Certificate> certMap = buildCertificateMap(certs);
        MultiValuedMap<X500Principal, URI> distributionPoints = new ArrayListValuedHashMap<>();
        collectDistributionUrls(certs, distributionPoints);
        CertificateFactory cf = getCertificateFactory();
        for ( X500Principal issuer : distributionPoints.keySet() ) {
            updateIssuerCRL(issuer, distributionPoints.get(issuer), currentCrls, revocationConfig, certMap, certs, cf, tm);
        }
    }


    /**
     * @param certMap
     * @param revocationConfig
     * @param currentCrls
     * @param collection
     * @param issuer
     * @param cf
     * @param certs
     * @param tm
     * @throws TruststoreManagerException
     * 
     */
    private void updateIssuerCRL ( X500Principal issuer, Collection<URI> dpUris, Map<X500Principal, X509CRL> currentCrls,
            RevocationConfig revocationConfig, Map<X500Principal, X509Certificate> certMap, Set<X509Certificate> certs, CertificateFactory cf,
            TruststoreManager tm ) throws TruststoreManagerException {
        List<URI> shuffled = new ArrayList<>(dpUris);
        Collections.shuffle(shuffled);

        CRLAndLocation updatedCrl = getNewIssuerCRL(issuer, dpUris, currentCrls, revocationConfig, certs, cf, shuffled);

        if ( updatedCrl == null ) {
            log.debug("Did not find updated CRL"); //$NON-NLS-1$
            return;
        }

        if ( !tryUpdateCRL(issuer, certMap, tm, updatedCrl) ) {
            X509CRL crl = currentCrls.get(issuer);
            if ( crl != null && !validateCRL(updatedCrl, issuer, certMap.get(issuer)) ) {
                log.warn("Old CRL also is invalid"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param issuer
     * @param certMap
     * @param tm
     * @param updatedCrl
     * @param oldCrl
     * @throws TruststoreManagerException
     */
    private boolean tryUpdateCRL ( X500Principal issuer, Map<X500Principal, X509Certificate> certMap, TruststoreManager tm,
            CRLAndLocation updatedCrl ) throws TruststoreManagerException {
        X509CRL c = updatedCrl.getCrl();
        X509Certificate issuerCertificate = certMap.get(issuer);

        if ( !validateCRL(updatedCrl, issuer, issuerCertificate) ) {
            return false;
        }

        if ( c.getRevokedCertificates() != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Got a CRL with %d revoked certificates for %s", //$NON-NLS-1$
                    c.getRevokedCertificates().size(),
                    issuer));
            }
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("No certificates revoked for " + issuer); //$NON-NLS-1$
        }

        tm.updateCRL(c);
        return true;
    }


    /**
     * @param issuer
     * @param dpUris
     * @param currentCrls
     * @param revocationConfig
     * @param certs
     * @param cf
     * @param shuffled
     * @return
     */
    private CRLAndLocation getNewIssuerCRL ( X500Principal issuer, Collection<URI> dpUris, Map<X500Principal, X509CRL> currentCrls,
            RevocationConfig revocationConfig, Set<X509Certificate> certs, CertificateFactory cf, List<URI> shuffled ) {
        if ( dpUris.isEmpty() ) {
            return null;
        }

        X509CRL currentCrl = currentCrls.get(issuer);
        CRLAndLocation crl = findUpdatedCRL(revocationConfig, certs, currentCrl, cf, issuer, shuffled);

        if ( crl == null ) {
            return null;
        }

        if ( currentCrl != null && currentCrl.equals(crl) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No new CRL available for " + issuer); //$NON-NLS-1$
            }
            return null;
        }
        return crl;
    }


    /**
     * @param crl
     * @param issuerCertificate
     * @return
     */
    protected boolean validateCRL ( CRLAndLocation crl, X500Principal ca, X509Certificate issuerCertificate ) {
        X509CRL c = crl.getCrl();
        if ( issuerCertificate == null ) {
            log.error(String.format(
                "Could not find trusted CRL issuer certificate for CA %s (%s), issuer is %s", //$NON-NLS-1$
                ca.getName(),
                crl.getLocation() != null ? crl.getLocation() : "local", //$NON-NLS-1$
                c.getIssuerX500Principal().getName()));
            return false;
        }

        if ( !validateCRLSignature(crl, ca, issuerCertificate) ) {
            return false;
        }

        if ( !validateCRLExtensions(c, ca) ) {
            return false;
        }

        if ( !validateCRLValiditiy(c, ca) ) {
            return false;
        }

        return true;
    }


    /**
     * @param crl
     */
    private static boolean validateCRLExtensions ( X509CRL crl, X500Principal ca ) {
        Set<String> critExt = crl.getCriticalExtensionOIDs();
        if ( critExt != null && !critExt.isEmpty() ) {
            critExt.remove(Extension.reasonCode.toString());
            critExt.remove(Extension.certificateIssuer.toString());
            if ( !critExt.isEmpty() ) {
                log.error(String.format("CRL for %s containst unknown critical extensions: %s", ca.getName(), critExt)); //$NON-NLS-1$
                return false;
            }
        }

        return true;
    }


    /**
     * @param crl
     */
    private static boolean validateCRLValiditiy ( X509CRL crl, X500Principal ca ) {
        Date now = new Date();
        if ( crl.getNextUpdate().before(now) ) {
            log.warn(String.format("CRL is already expired on %s for %s", crl.getNextUpdate(), ca.getName())); //$NON-NLS-1$
            return false;
        }

        if ( crl.getThisUpdate().after(now) ) {
            log.warn(String.format("CRL thisUpdate is from the future %s for %s", crl.getThisUpdate(), ca.getName())); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param crl
     * @param issuerCertificate
     */
    private static boolean validateCRLSignature ( CRLAndLocation crl, X500Principal ca, X509Certificate issuerCertificate ) {
        X509CRL c = crl.getCrl();
        try {
            if ( !ca.equals(c.getIssuerX500Principal()) ) {
                log.error(String.format(
                    "Mismatch between CA %s CRL issuer %s (%s), verification certificate subject is %s", //$NON-NLS-1$
                    ca.getName(),
                    crl.getLocation() != null ? crl.getLocation() : "local", //$NON-NLS-1$
                    c.getIssuerX500Principal().getName(),
                    issuerCertificate.getSubjectX500Principal().getName()));
                return false;
            }

            c.verify(issuerCertificate.getPublicKey());
            return true;
        }
        catch (
            CRLException |
            InvalidKeyException |
            NoSuchAlgorithmException |
            NoSuchProviderException |
            SignatureException e ) {
            log.error(String.format(
                "CRL signature invalid for CA %s (%s), issuer is %s, certificate subject is %s", //$NON-NLS-1$
                ca.getName(),
                crl.getLocation() != null ? crl.getLocation() : "local", //$NON-NLS-1$
                c.getIssuerX500Principal().getName(),
                issuerCertificate.getSubjectX500Principal().getName()), e);
            return false;
        }
    }


    /**
     * @param certs
     * @param currentCrls
     * @param cf
     * @param issuer
     * @param shuffled
     * @return
     */
    protected CRLAndLocation findUpdatedCRL ( RevocationConfig revConfig, Set<X509Certificate> certs, X509CRL currentCrl, CertificateFactory cf,
            X500Principal issuer, List<URI> shuffled ) {
        if ( currentCrl != null && !needsUpdate(revConfig, currentCrl) ) {
            return new CRLAndLocation(currentCrl);
        }

        for ( URI uriToTry : shuffled ) {
            try {
                X509CRL loadedCRL = RemoteLoaderUtil.loadCRL(
                    makeParams(certs),
                    getSecureRandom(),
                    currentCrl,
                    uriToTry,
                    revConfig.getConnectTimeout(),
                    revConfig.getReadTimeout(),
                    3);
                if ( loadedCRL != null ) {
                    return new CRLAndLocation(uriToTry, loadedCRL);
                }
            }
            catch (
                IOException |
                GeneralSecurityException e ) {
                log.warn("Failed to load CRL from " + uriToTry, e); //$NON-NLS-1$
            }
        }

        log.error("No CRL could be loaded for " + issuer); //$NON-NLS-1$
        return currentCrl != null ? new CRLAndLocation(currentCrl) : null;
    }


    /**
     * @param certs
     * @return
     * @throws InvalidAlgorithmParameterException
     */
    private static PKIXBuilderParameters makeParams ( Set<X509Certificate> certs ) throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        for ( X509Certificate cert : certs ) {
            trustAnchors.add(new TrustAnchor(cert, null));
        }
        PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, null);
        params.setRevocationEnabled(false);
        return params;
    }


    /**
     * @return
     * @throws TruststoreManagerException
     */
    protected CertificateFactory getCertificateFactory () throws TruststoreManagerException {
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
        }
        catch ( CertificateException e ) {
            throw new TruststoreManagerException("Failed to get certificate factory", e); //$NON-NLS-1$
        }
        return cf;
    }


    /**
     * @param currentCrl
     * @return
     */
    protected boolean needsUpdate ( RevocationConfig revConfig, X509CRL crl ) {

        DateTime lastUpdate = new DateTime(crl.getThisUpdate());
        DateTime nextUpdate = new DateTime(crl.getNextUpdate());
        Duration lifeTime = new Duration(lastUpdate, nextUpdate);

        long halfTime = lifeTime.getStandardMinutes() / 2;

        if ( halfTime <= revConfig.getCrlUpdateIntervalMinutes() ) {
            log.warn("CRL update interval is too fast " + crl); //$NON-NLS-1$
            return true;
        }

        if ( nextUpdate.isBeforeNow() ) {
            log.warn("CRL is already expired " + crl); //$NON-NLS-1$
            return true;
        }

        if ( nextUpdate.isBefore(DateTime.now().minusMinutes(revConfig.getCrlUpdateIntervalMinutes())) ) {
            return true;
        }

        return false;
    }


    /**
     * @param listCRLs
     * @return
     */
    private static Map<X500Principal, X509CRL> buildCRLMap ( Set<X509CRL> crls ) {
        Map<X500Principal, X509CRL> res = new HashMap<>();
        for ( X509CRL crl : crls ) {
            res.put(crl.getIssuerX500Principal(), crl);
        }
        return res;
    }


    private static Map<X500Principal, X509Certificate> buildCertificateMap ( Set<X509Certificate> certs ) {
        Map<X500Principal, X509Certificate> res = new HashMap<>();
        for ( X509Certificate cert : certs ) {
            res.put(cert.getSubjectX500Principal(), cert);
        }
        return res;
    }


    /**
     * @param certs
     * @param distributionPoints
     */
    protected void collectDistributionUrls ( Set<X509Certificate> certs, MultiValuedMap<X500Principal, URI> distributionPoints ) {
        for ( X509Certificate cert : certs ) {
            getDistributionUrls(distributionPoints, cert);
        }
    }


    /**
     * @param distributionPoints
     * @param cert
     */
    protected void getDistributionUrls ( MultiValuedMap<X500Principal, URI> distributionPoints, X509Certificate cert ) {
        try {
            JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(cert);

            Extension extension = certHolder.getExtension(Extension.cRLDistributionPoints);

            if ( extension == null ) {
                log.trace("No distribution point extension, skip"); //$NON-NLS-1$
                return;
            }

            getDistributionUrls(distributionPoints, cert, extension);

        }
        catch ( CertificateEncodingException e ) {
            log.warn("Failed to read certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * @param distributionPoints
     * @param cert
     * @param extension
     */
    protected void getDistributionUrls ( MultiValuedMap<X500Principal, URI> distributionPoints, X509Certificate cert, Extension extension ) {
        CRLDistPoint distPoints = CRLDistPoint.getInstance(extension.getParsedValue());

        for ( DistributionPoint distPoint : distPoints.getDistributionPoints() ) {
            if ( distPoint.getDistributionPoint().getType() != DistributionPointName.FULL_NAME ) {
                log.trace("Not a full name distribution point, skip"); //$NON-NLS-1$
                continue;
            }

            GeneralNames names = GeneralNames.getInstance(distPoint.getDistributionPoint().getName());

            getDistributionPoints(distributionPoints, cert, names);
        }
    }


    /**
     * @param distributionPoints
     * @param cert
     * @param names
     */
    protected void getDistributionPoints ( MultiValuedMap<X500Principal, URI> distributionPoints, X509Certificate cert, GeneralNames names ) {
        for ( GeneralName gn : names.getNames() ) {
            if ( gn.getTagNo() != GeneralName.uniformResourceIdentifier ) {
                log.trace("DistributionPointName that is not an URI, skip"); //$NON-NLS-1$
                continue;
            }
            getDistributionPoint(distributionPoints, cert, gn);
        }
    }


    /**
     * @param distributionPoints
     * @param cert
     * @param gn
     */
    protected void getDistributionPoint ( MultiValuedMap<X500Principal, URI> distributionPoints, X509Certificate cert, GeneralName gn ) {
        URI uri;
        try {
            uri = new URI(gn.getName().toString());
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to parse distribution point URI " + gn.getName(), e); //$NON-NLS-1$
            return;
        }

        if ( !isSupportedDistributionPointURL(uri) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Adding URL: " + uri); //$NON-NLS-1$
        }

        distributionPoints.put(cert.getSubjectX500Principal(), uri);
    }


    /**
     * @param uri
     */
    protected boolean isSupportedDistributionPointURL ( URI uri ) {
        if ( !HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme()) ) {
            if ( log.isDebugEnabled() ) {
                log.trace("Not a HTTP(s) distribution point, skip " + uri); //$NON-NLS-1$
            }
            return false;
        }

        if ( !uri.isAbsolute() ) {
            log.debug("Not an absolute URI, skip"); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    private static final class CRLAndLocation {

        private final URI location;
        private final X509CRL crl;


        /**
         * @param location
         * @param crl
         * 
         */
        public CRLAndLocation ( URI location, X509CRL crl ) {
            this.location = location;
            this.crl = crl;
        }


        /**
         * @param crl
         * 
         */
        public CRLAndLocation ( X509CRL crl ) {
            this.crl = crl;
            this.location = null;
        }


        /**
         * @return the location
         */
        public URI getLocation () {
            return this.location;
        }


        /**
         * @return the crl
         */
        public X509CRL getCrl () {
            return this.crl;
        }

    }
}

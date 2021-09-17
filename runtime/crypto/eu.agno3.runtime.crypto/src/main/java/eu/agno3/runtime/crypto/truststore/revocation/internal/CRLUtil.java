/**
 * Mostly copied from bouncycastle 1.52
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.x509.ExtendedPKIXParameters;
import org.bouncycastle.x509.X509AttributeCertificate;
import org.bouncycastle.x509.X509CRLStoreSelector;
import org.bouncycastle.x509.X509Store;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "all"
} )
public class CRLUtil {

    public static Set findCRLs ( X509CRLStoreSelector crlselect, ExtendedPKIXParameters paramsPKIX, Date currentDate ) throws AnnotatedException {
        Set initialSet = new HashSet();

        // get complete CRL(s)
        try {
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getAdditionalStores()));
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getStores()));
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getCertStores()));
        }
        catch ( AnnotatedException e ) {
            throw new AnnotatedException("Exception obtaining complete CRLs.", e);
        }

        Set finalSet = new HashSet();
        Date validityDate = currentDate;

        if ( paramsPKIX.getDate() != null ) {
            validityDate = paramsPKIX.getDate();
        }

        // based on RFC 5280 6.3.3
        for ( Iterator it = initialSet.iterator(); it.hasNext(); ) {
            X509CRL crl = (X509CRL) it.next();

            if ( crl.getNextUpdate().after(validityDate) ) {
                X509Certificate cert = crlselect.getCertificateChecking();

                if ( cert != null ) {
                    if ( crl.getThisUpdate().before(cert.getNotAfter()) ) {
                        finalSet.add(crl);
                    }
                }
                else {
                    finalSet.add(crl);
                }
            }
        }

        return finalSet;
    }


    public static Set findCRLs ( X509CRLStoreSelector crlselect, PKIXParameters paramsPKIX ) throws AnnotatedException {
        Set completeSet = new HashSet();

        // get complete CRL(s)
        try {
            completeSet.addAll(findCRLs(crlselect, paramsPKIX.getCertStores()));
        }
        catch ( AnnotatedException e ) {
            throw new AnnotatedException("Exception obtaining complete CRLs.", e);
        }

        return completeSet;
    }


    /**
     * Return a Collection of all CRLs found in the X509Store's that are
     * matching the crlSelect criteriums.
     *
     * @param crlSelect
     *            a {@link X509CRLStoreSelector} object that will be used
     *            to select the CRLs
     * @param crlStores
     *            a List containing only {@link org.bouncycastle.x509.X509Store X509Store} objects.
     *            These are used to search for CRLs
     *
     * @return a Collection of all found {@link java.security.cert.X509CRL X509CRL} objects. May be
     *         empty but never <code>null</code>.
     */
    private static final Collection findCRLs ( X509CRLStoreSelector crlSelect, List crlStores ) throws AnnotatedException {
        Set crls = new HashSet();

        Iterator iter = crlStores.iterator();

        AnnotatedException lastException = null;
        boolean foundValidStore = false;

        while ( iter.hasNext() ) {
            Object obj = iter.next();

            if ( obj instanceof X509Store ) {
                X509Store store = (X509Store) obj;

                try {
                    crls.addAll(store.getMatches(crlSelect));
                    foundValidStore = true;
                }
                catch ( StoreException e ) {
                    lastException = new AnnotatedException("Exception searching in X.509 CRL store.", e);
                }
            }
            else {
                CertStore store = (CertStore) obj;

                try {
                    crls.addAll(store.getCRLs(crlSelect));
                    foundValidStore = true;
                }
                catch ( CertStoreException e ) {
                    lastException = new AnnotatedException("Exception searching in X.509 CRL store.", e);
                }
            }
        }
        if ( !foundValidStore && lastException != null ) {
            throw lastException;
        }
        return crls;
    }


    /**
     * Search the given Set of TrustAnchor's for one that is the
     * issuer of the given X509 certificate. Uses the default provider
     * for signature verification.
     *
     * @param cert
     *            the X509 certificate
     * @param trustAnchors
     *            a Set of TrustAnchor's
     * @return the <code>TrustAnchor</code> object if found or <code>null</code> if not.
     * @throws AnnotatedException
     *             if a TrustAnchor was found but the signature verification
     *             on the given certificate has thrown an exception.
     */
    protected static TrustAnchor findTrustAnchor ( X509Certificate cert, Set trustAnchors ) throws AnnotatedException {
        return findTrustAnchor(cert, trustAnchors, null);
    }


    /**
     * Returns the issuer of an attribute certificate or certificate.
     *
     * @param cert
     *            The attribute certificate or certificate.
     * @return The issuer as <code>X500Principal</code>.
     */
    static X500Name getEncodedIssuerPrincipal ( Object cert ) {
        if ( cert instanceof X509Certificate ) {
            return getIssuerPrincipal((X509Certificate) cert);
        }
        else {
            return X500Name.getInstance( ( (X500Principal) ( (X509AttributeCertificate) cert ).getIssuer().getPrincipals()[ 0 ] ).getEncoded());
        }
    }


    static X500Name getCA ( TrustAnchor trustAnchor ) {
        return X500Name.getInstance(trustAnchor.getCA().getEncoded());
    }


    static X500Name getIssuerPrincipal ( X509Certificate cert ) {
        return X500Name.getInstance(cert.getIssuerX500Principal().getEncoded());
    }


    /**
     * Search the given Set of TrustAnchor's for one that is the
     * issuer of the given X509 certificate. Uses the specified
     * provider for signature verification, or the default provider
     * if null.
     *
     * @param cert
     *            the X509 certificate
     * @param trustAnchors
     *            a Set of TrustAnchor's
     * @param sigProvider
     *            the provider to use for signature verification
     * @return the <code>TrustAnchor</code> object if found or <code>null</code> if not.
     * @throws AnnotatedException
     *             if a TrustAnchor was found but the signature verification
     *             on the given certificate has thrown an exception.
     */
    protected static TrustAnchor findTrustAnchor ( X509Certificate cert, Set trustAnchors, String sigProvider ) throws AnnotatedException {
        TrustAnchor trust = null;
        PublicKey trustPublicKey = null;
        Exception invalidKeyEx = null;

        X509CertSelector certSelectX509 = new X509CertSelector();
        X500Name certIssuer = getEncodedIssuerPrincipal(cert);

        try {
            certSelectX509.setSubject(certIssuer.getEncoded());
        }
        catch ( IOException ex ) {
            throw new AnnotatedException("Cannot set subject search criteria for trust anchor.", ex);
        }

        Iterator iter = trustAnchors.iterator();
        while ( iter.hasNext() && trust == null ) {
            trust = (TrustAnchor) iter.next();
            if ( trust.getTrustedCert() != null ) {
                if ( certSelectX509.match(trust.getTrustedCert()) ) {
                    trustPublicKey = trust.getTrustedCert().getPublicKey();
                }
                else {
                    trust = null;
                }
            }
            else if ( trust.getCAName() != null && trust.getCAPublicKey() != null ) {
                try {
                    X500Name caName = getCA(trust);
                    if ( certIssuer.equals(caName) ) {
                        trustPublicKey = trust.getCAPublicKey();
                    }
                    else {
                        trust = null;
                    }
                }
                catch ( IllegalArgumentException ex ) {
                    trust = null;
                }
            }
            else {
                trust = null;
            }

            if ( trustPublicKey != null ) {
                try {
                    verifyX509Certificate(cert, trustPublicKey, sigProvider);
                }
                catch ( Exception ex ) {
                    invalidKeyEx = ex;
                    trust = null;
                    trustPublicKey = null;
                }
            }
        }

        if ( trust == null && invalidKeyEx != null ) {
            throw new AnnotatedException("TrustAnchor found but certificate validation failed.", invalidKeyEx);
        }

        return trust;
    }


    protected static void verifyX509Certificate ( X509Certificate cert, PublicKey publicKey, String sigProvider ) throws GeneralSecurityException {
        if ( sigProvider == null ) {
            cert.verify(publicKey);
        }
        else {
            cert.verify(publicKey, sigProvider);
        }
    }


    /**
     * @param dp
     * @param certIssuer
     * @param selector
     * @param pkixParams
     * @return the valid issuers
     * @throws AnnotatedException
     */
    public static Set<X500Name> getCRLIssuersFromDistributionPoint ( DistributionPoint dp, X500Principal certIssuer, X509CRLSelector selector,
            PKIXParameters pkixParams ) throws AnnotatedException {

        Set<X500Name> issuers = new HashSet<>();
        if ( certIssuer != null ) {
            issuers.add(X500Name.getInstance(certIssuer.getEncoded()));
        }
        getCRLIssuersFromDistributionPoint(dp, issuers, selector);

        return issuers;
    }


    /**
     * Add the CRL issuers from the cRLIssuer field of the distribution point or
     * from the certificate if not given to the issuer criterion of the <code>selector</code>.
     * <p>
     * The <code>issuerPrincipals</code> are a collection with a single <code>X500Name</code> for
     * <code>X509Certificate</code>s.
     * </p>
     * 
     * @param dp
     *            The distribution point.
     * @param issuerPrincipals
     *            The issuers of the certificate or attribute
     *            certificate which contains the distribution point.
     * @param selector
     *            The CRL selector.
     * @throws AnnotatedException
     *             if an exception occurs while processing.
     * @throws ClassCastException
     *             if <code>issuerPrincipals</code> does not
     *             contain only <code>X500Name</code>s.
     */
    protected static void getCRLIssuersFromDistributionPoint ( DistributionPoint dp, Collection issuerPrincipals, X509CRLSelector selector )
            throws AnnotatedException {
        List issuers = new ArrayList();
        // indirect CRL
        if ( dp.getCRLIssuer() != null ) {
            GeneralName genNames[] = dp.getCRLIssuer().getNames();
            // look for a DN
            for ( int j = 0; j < genNames.length; j++ ) {
                if ( genNames[ j ].getTagNo() == GeneralName.directoryName ) {
                    try {
                        issuers.add(X500Name.getInstance(genNames[ j ].getName().toASN1Primitive().getEncoded()));
                    }
                    catch ( IOException e ) {
                        throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", e);
                    }
                }
            }
        }
        else {
            /*
             * certificate issuer is CRL issuer, distributionPoint field MUST be
             * present.
             */
            if ( dp.getDistributionPoint() == null ) {
                throw new AnnotatedException("CRL issuer is omitted from distribution point but no distributionPoint field present.");
            }
            // add and check issuer principals
            for ( Iterator it = issuerPrincipals.iterator(); it.hasNext(); ) {
                issuers.add(it.next());
            }
        }
        // TODO: is not found although this should correctly add the rel name. selector of Sun is buggy here or PKI test
        // case is invalid
        // distributionPoint
        // if (dp.getDistributionPoint() != null)
        // {
        // // look for nameRelativeToCRLIssuer
        // if (dp.getDistributionPoint().getType() == DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER)
        // {
        // // append fragment to issuer, only one
        // // issuer can be there, if this is given
        // if (issuers.size() != 1)
        // {
        // throw new AnnotatedException(
        // "nameRelativeToCRLIssuer field is given but more than one CRL issuer is given.");
        // }
        // ASN1Encodable relName = dp.getDistributionPoint().getName();
        // Iterator it = issuers.iterator();
        // List issuersTemp = new ArrayList(issuers.size());
        // while (it.hasNext())
        // {
        // Enumeration e = null;
        // try
        // {
        // e = ASN1Sequence.getInstance(
        // new ASN1InputStream(((X500Principal) it.next())
        // .getEncoded()).readObject()).getObjects();
        // }
        // catch (IOException ex)
        // {
        // throw new AnnotatedException(
        // "Cannot decode CRL issuer information.", ex);
        // }
        // ASN1EncodableVector v = new ASN1EncodableVector();
        // while (e.hasMoreElements())
        // {
        // v.add((ASN1Encodable) e.nextElement());
        // }
        // v.add(relName);
        // issuersTemp.add(new X500Principal(new DERSequence(v)
        // .getDEREncoded()));
        // }
        // issuers.clear();
        // issuers.addAll(issuersTemp);
        // }
        // }
        Iterator it = issuers.iterator();
        while ( it.hasNext() ) {
            try {
                selector.addIssuerName( ( (X500Name) it.next() ).getEncoded());
            }
            catch ( IOException ex ) {
                throw new AnnotatedException("Cannot decode CRL issuer information.", ex);
            }
        }
    }
}

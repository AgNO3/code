/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public class OpenSSLCRLCertStoreSPI extends CertStoreSpi {

    /**
     * 
     */
    private static final String X509 = "X509"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(OpenSSLCRLCertStoreSPI.class);
    private OpenSSLCrlCertStoreParameters crtStoreParams;


    /**
     * @param params
     * @throws InvalidAlgorithmParameterException
     */
    public OpenSSLCRLCertStoreSPI ( CertStoreParameters params ) throws InvalidAlgorithmParameterException {
        super(params);
        if ( ! ( params instanceof OpenSSLCrlCertStoreParameters ) ) {
            throw new InvalidAlgorithmParameterException("Need OpenSSLCrlCertStoreParamters"); //$NON-NLS-1$
        }
        this.crtStoreParams = (OpenSSLCrlCertStoreParameters) params;
    }


    private File getCRLDir () {
        return this.crtStoreParams.getCRLDir();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.CertStoreSpi#engineGetCertificates(java.security.cert.CertSelector)
     */
    @Override
    public Collection<? extends Certificate> engineGetCertificates ( CertSelector selector ) throws CertStoreException {
        return Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.CertStoreSpi#engineGetCRLs(java.security.cert.CRLSelector)
     */
    @Override
    public Collection<? extends CRL> engineGetCRLs ( CRLSelector selector ) throws CertStoreException {

        if ( ! ( selector instanceof X509CRLSelector ) ) {
            return Collections.EMPTY_LIST;
        }

        X509CRLSelector x509selector = (X509CRLSelector) selector;
        Set<X509CRL> crls = new HashSet<>();
        try {
            for ( X509CRL crl : getCandidateCRLs(x509selector) ) {
                if ( x509selector.match(crl) ) {
                    crls.add(crl);
                }
            }
        }
        catch ( CertificateException e ) {
            throw new CertStoreException("Failed to load CRLs", e); //$NON-NLS-1$
        }

        return crls;
    }


    /**
     * @param x509selector
     * @return
     * @throws CertificateException
     */
    protected Collection<X509CRL> getCandidateCRLs ( X509CRLSelector x509selector ) throws CertificateException {
        FileFilter filter = getFileFilterForSelector(x509selector);

        File[] crlFiles = this.getCRLDir().listFiles(filter);

        if ( crlFiles == null ) {
            return Collections.EMPTY_SET;
        }

        Set<X509CRL> crls = new HashSet<>();

        CertificateFactory cf = CertificateFactory.getInstance(X509);
        for ( File f : crlFiles ) {
            X509CRL crl = loadFromFile(cf, f);
            if ( crl != null ) {
                crls.add(crl);
            }
        }
        return crls;
    }


    protected X509CRL loadFromFile ( CertificateFactory cf, File f ) {
        try ( FileInputStream fis = new FileInputStream(f) ) {
            CRL tmp = cf.generateCRL(fis);
            if ( ! ( tmp instanceof X509CRL ) ) {
                return null;
            }
            return (X509CRL) tmp;
        }
        catch (
            IOException |
            CRLException e ) {
            log.warn("Failed to load CRL", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param x509selector
     * @return
     */
    protected FileFilter getFileFilterForSelector ( X509CRLSelector x509selector ) {
        Collection<X500Principal> matchIssuers = x509selector.getIssuers();
        X509Certificate certToCheck = x509selector.getCertificateChecking();

        FileFilter filter;
        if ( matchIssuers != null || certToCheck != null ) {
            Set<String> matchHashes = new HashSet<>();
            if ( matchIssuers != null ) {
                matchHashes.addAll(issuersToHashes(matchIssuers));
            }
            else if ( certToCheck != null ) {
                matchHashes.addAll(issuersToHashes(Arrays.asList(certToCheck.getIssuerX500Principal())));
            }
            filter = new HashMatchingFilter(matchHashes);
        }
        else {
            filter = new AllHashedMatchingFilter();
        }
        return filter;
    }


    /**
     * @param matchIssuers
     * @return
     */
    private static Collection<? extends String> issuersToHashes ( Collection<X500Principal> matchIssuers ) {
        Set<String> hashes = new HashSet<>();
        for ( X500Principal issuer : matchIssuers ) {
            try {
                hashes.add(NameHashUtil.opensslNameHash(issuer));
            }
            catch ( CryptoException e ) {
                log.warn("Failed to generate issuer hash for " + issuer.getName(), e); //$NON-NLS-1$
            }
        }

        return hashes;
    }
}

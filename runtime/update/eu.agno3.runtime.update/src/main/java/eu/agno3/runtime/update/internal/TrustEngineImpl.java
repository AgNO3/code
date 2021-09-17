/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.osgi.internal.signedcontent.SignedContentConstants;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.update.UpdateTrustConfiguration;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
@Component ( service = TrustEngine.class, immediate = true, property = {
    SignedContentConstants.TRUST_ENGINE + "=" + TrustEngineImpl.TRUST_ENGINE_NAME
} )
public class TrustEngineImpl extends TrustEngine {

    /**
     * 
     */
    private static final String CS_EKU = "1.3.6.1.5.5.7.3.3"; //$NON-NLS-1$
    private static final String TSA_EKU = "1.3.6.1.5.5.7.3.8"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(TrustEngineImpl.class);

    /**
     * 
     */
    public static final String TRUST_ENGINE_NAME = "agno3"; //$NON-NLS-1$

    private TrustChecker trustChecker;
    private UpdateTrustConfiguration trustConfig;
    private TrustConfiguration globalTrust;


    @Reference
    protected synchronized void setTrustChecker ( TrustChecker tc ) {
        this.trustChecker = tc;
    }


    protected synchronized void unsetTrustChecker ( TrustChecker tc ) {
        if ( this.trustChecker == tc ) {
            this.trustChecker = null;
        }
    }


    @Reference ( target = "(instanceId=client)", cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setGlobalTrustConfiguration ( TrustConfiguration tc ) {
        this.globalTrust = tc;
    }


    protected synchronized void unsetGlobalTrustConfiguration ( TrustConfiguration tc ) {
        if ( this.globalTrust == tc ) {
            this.globalTrust = null;
        }
    }


    @Reference
    protected synchronized void setTrustConfiguration ( UpdateTrustConfiguration tc ) {
        this.trustConfig = tc;
    }


    protected synchronized void unsetTrustConfiguration ( UpdateTrustConfiguration tc ) {
        if ( this.trustConfig == tc ) {
            this.trustConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#doAddTrustAnchor(java.security.cert.Certificate,
     *      java.lang.String)
     */
    @Override
    protected String doAddTrustAnchor ( Certificate cert, String alias ) throws IOException, GeneralSecurityException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#doRemoveTrustAnchor(java.security.cert.Certificate)
     */
    @Override
    protected void doRemoveTrustAnchor ( Certificate cert ) throws IOException, GeneralSecurityException {}


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#doRemoveTrustAnchor(java.lang.String)
     */
    @Override
    protected void doRemoveTrustAnchor ( String alias ) throws IOException, GeneralSecurityException {}


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#findTrustAnchor(java.security.cert.Certificate[])
     */
    @Override
    public synchronized Certificate findTrustAnchor ( Certificate[] certChain ) throws IOException {

        if ( this.trustChecker == null || this.trustConfig == null ) {
            throw new IOException("Trust checker has been disposed of"); //$NON-NLS-1$
        }

        List<Certificate> chain = Arrays.asList(certChain);
        boolean tsa = haveTSA(chain);
        boolean[] keyUsage = new boolean[] {
            true, false, false, false, false, false, false, false, false
        };

        Date validateDate = null;
        Set<String> ekus = null;
        TrustConfiguration tc = this.trustConfig;
        if ( tsa ) {
            // verify against global CAs
            ekus = Collections.singleton(TSA_EKU); // $NON-NLS-1$
        }
        else {
            // this is bad, we have to disable the validity check
            // but this check is done later in SignedContentFactory
            validateDate = ( (X509Certificate) chain.get(0) ).getNotBefore();
            ekus = Collections.singleton(CS_EKU);
        }

        try {
            return this.trustChecker.validateChain(tc, chain, validateDate, keyUsage, ekus).getTrustAnchor().getTrustedCert();
        }
        catch ( CryptoException e ) {
            return handleValidationFailure(certChain, chain, tsa, keyUsage, validateDate, ekus, e);
        }

    }


    /**
     * @param certChain
     * @param chain
     * @param tsa
     * @param keyUsage
     * @param validateDate
     * @param ekus
     * @param e
     * @return
     */
    protected Certificate handleValidationFailure ( Certificate[] certChain, List<Certificate> chain, boolean tsa, boolean[] keyUsage,
            Date validateDate, Set<String> ekus, CryptoException e ) {
        log.trace("Primary certificate validation failed", e); //$NON-NLS-1$
        if ( tsa && this.globalTrust != null ) {
            return handleTSAValidationFailure(chain, keyUsage, validateDate, ekus, e);
        }
        else if ( this.trustConfig.hasDelegate() ) {
            try {
                // fallback to validating with the builtin fallback trust store
                PKIXCertPathBuilderResult validateChain = this.trustChecker
                        .validateChain(this.trustConfig.getFallback(), chain, validateDate, keyUsage, ekus);
                if ( validateChain != null ) {
                    return validateChain.getTrustAnchor().getTrustedCert();
                }
                log.debug("Chain is empty"); //$NON-NLS-1$
            }
            catch ( CryptoException e1 ) {
                log.trace("Failed to validate certificate with fallback", e1); //$NON-NLS-1$
            }
        }

        return null;
    }


    /**
     * @param certChain
     * @param e
     */
    protected void debugChainFailure ( Certificate[] certChain, CryptoException e ) {
        log.trace("Failed to validate certificate", e); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            for ( int i = 0; i < certChain.length; i++ ) {
                log.debug(String.format("Certificate %d: %s", i, ( (X509Certificate) certChain[ i ] ).getSubjectX500Principal())); //$NON-NLS-1$
                try {
                    log.debug(org.apache.commons.codec.binary.Base64.encodeBase64String(certChain[ i ].getEncoded()));
                }
                catch ( CertificateEncodingException e1 ) {
                    log.debug("Encoding failed", e1); //$NON-NLS-1$
                    log.warn("Invalid cert", e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * @param chain
     * @param keyUsage
     * @param validateDate
     * @param ekus
     * @return
     */
    protected Certificate handleTSAValidationFailure ( List<Certificate> chain, boolean[] keyUsage, Date validateDate, Set<String> ekus,
            CryptoException originalFailure ) {
        try {
            // fallback to validating TSAs with global trust
            PKIXCertPathBuilderResult validateChain = this.trustChecker.validateChain(this.globalTrust, chain, validateDate, keyUsage, ekus);
            if ( validateChain != null ) {
                return validateChain.getTrustAnchor().getTrustedCert();
            }
            log.trace("Chain is empty"); //$NON-NLS-1$
        }
        catch ( CryptoException e1 ) {
            log.debug("Failed to validate certificate with fallback", e1); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            debugChainFailure(chain.toArray(new Certificate[chain.size()]), originalFailure);
        }
        return null;
    }


    /**
     * @param chain
     * @return
     */
    protected boolean haveTSA ( List<Certificate> chain ) {
        // eclipse design is bad here, in fact we would need to know whether this should be a TSA or signing cert.
        // so we have to guess what it is.
        // Also for verifying validity dates here, additional information would be required.
        boolean tsa = false;
        try {
            if ( !chain.isEmpty() && chain.get(0) instanceof X509Certificate && ( (X509Certificate) chain.get(0) ).getExtendedKeyUsage() != null ) {
                tsa = ( (X509Certificate) chain.get(0) ).getExtendedKeyUsage().contains(TSA_EKU); // $NON-NLS-1$
            }
        }
        catch ( CertificateParsingException e ) {
            log.warn("Failed to get certificate type", e); //$NON-NLS-1$
        }
        return tsa;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#getTrustAnchor(java.lang.String)
     */
    @Override
    public Certificate getTrustAnchor ( String alias ) throws IOException, GeneralSecurityException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#getAliases()
     */
    @Override
    public String[] getAliases () throws IOException, GeneralSecurityException {
        return new String[] {};
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#getName()
     */
    @Override
    public String getName () {
        return TRUST_ENGINE_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.service.security.TrustEngine#isReadOnly()
     */
    @Override
    public boolean isReadOnly () {
        return true;
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation;


import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultEntry;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultSeverity;
import eu.agno3.orchestrator.config.web.X509CertChainInvalidTestResultEntry;
import eu.agno3.runtime.crypto.truststore.EmptyTruststoreCertificateException;


/**
 * @author mbechler
 *
 */
public final class TLSValidationUtils {

    /**
     * 
     */
    private static final String SSLCIENT_OBJECT_TYPE = "urn:agno3:objects:1.0:web:sslclient"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(TLSValidationUtils.class);


    /**
     * 
     */
    private TLSValidationUtils () {}


    /**
     * 
     * @param tc
     * @param r
     */
    public static void checkTruststoreUsage ( TLSTestContext tc, ConfigTestResult r ) {
        if ( tc == null ) {
            return;
        }

        if ( tc.isTrustFallback() ) {
            ConfigTestResultEntry re = new ConfigTestResultEntry(
                ConfigTestResultSeverity.WARNING,
                "TLS_TRUSTSTORE_FALLBACK", //$NON-NLS-1$
                tc.getTrustFallbackFrom(),
                tc.getTrustFallbackTo());
            re.setObjectType(SSLCIENT_OBJECT_TYPE);
            r.addEntry(re);
        }
    }


    /**
     * @param x509Certificate
     * @return names present in certificate
     * @throws CertificateParsingException
     */
    public static Set<String> extractValidNames ( X509Certificate x509Certificate ) throws CertificateParsingException {
        Set<String> names = new HashSet<>();
        X500Name n = X500Name.getInstance(x509Certificate.getSubjectX500Principal().getEncoded());
        RDN[] cns = n.getRDNs(BCStyle.CN);
        if ( cns != null ) {
            for ( RDN cn : cns ) {
                names.add(IETFUtils.valueToString(cn.getFirst().getValue()));
            }
        }

        Collection<List<?>> sans = x509Certificate.getSubjectAlternativeNames();
        if ( sans != null ) {
            for ( List<?> san : sans ) {
                int type = (int) san.get(0);

                if ( type == 2 || type == 6 || type == 7 ) {
                    names.add((String) san.get(1));
                }
            }
        }
        return names;
    }


    private static ConfigTestResultEntry makeErrorTestResult ( String msgTpl, X509Certificate[] chain, String... args ) {
        X509CertChainInvalidTestResultEntry e = new X509CertChainInvalidTestResultEntry(ConfigTestResultSeverity.ERROR, msgTpl, chain, args);
        e.setObjectType(SSLCIENT_OBJECT_TYPE);
        return e;
    }


    /**
     * @param r
     * @param e
     * @param tc
     */
    public static void handleTLSNameMismatch ( Exception e, ConfigTestResult r, TLSTestContext tc ) {
        log.debug("Certificate name mismatch", e); //$NON-NLS-1$
        Map<String, X509Certificate[]> nameValFails = tc.getNameValidationFailures();

        for ( Entry<String, X509Certificate[]> vf : nameValFails.entrySet() ) {
            try {
                Set<String> names = extractValidNames(vf.getValue()[ 0 ]);
                r.addEntry(makeErrorTestResult("FAIL_TLS_NAME_MISMATCH", vf.getValue(), vf.getKey(), names.toString(), e.getMessage())); //$NON-NLS-1$
            }
            catch ( CertificateParsingException ex ) {
                r.error("FAIL_TLS_INVALID_CERT_FOR", vf.getKey(), ex.getMessage()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param he
     * @param r
     * @param tc
     */
    public static void handleHandshakeException ( SSLHandshakeException he, ConfigTestResult r, TLSTestContext tc ) {
        log.trace("Handshake exception", he); //$NON-NLS-1$
        if ( he.getCause() instanceof CertificateException ) {
            handleCertificateException((CertificateException) he.getCause(), r, tc);
        }
        else {
            log.debug("SSL handshake failure", he); //$NON-NLS-1$
            r.error("FAIL_TLS_HANDSHAKE", he.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param he
     * @param r
     * @param tc
     */
    static void handleCertificateException ( CertificateException ce, ConfigTestResult r, TLSTestContext tc ) {

        ConfigTestResultEntry re;

        List<X509Certificate[]> chains = tc.getChainValidationFailures();
        X509Certificate[] chain = null;
        if ( !chains.isEmpty() ) {
            chain = chains.get(0);
            if ( chains.size() > 1 ) {
                log.warn("Found multiple invalid chains"); //$NON-NLS-1$
            }
        }
        else {
            log.debug("No chain found"); //$NON-NLS-1$
        }

        if ( ce instanceof EmptyTruststoreCertificateException ) {
            re = makeErrorTestResult("FAIL_TLS_TRUST_EMPTY", chain, ( (EmptyTruststoreCertificateException) ce ).getTrustStore(), ce.getMessage()); //$NON-NLS-1$
        }
        else if ( "sun.security.validator.ValidatorException".equals(ce.getClass().getName()) ) { //$NON-NLS-1$
            re = makeErrorTestResult(
                "FAIL_TLS_CERT_VALIDATION", //$NON-NLS-1$
                chain,
                ce.getCause() != null ? ce.getCause().getMessage() : ce.getMessage());
        }
        else if ( ce instanceof CertificateExpiredException ) {
            re = makeErrorTestResult("FAIL_TLS_CERT_EXPIRED", chain, ce.getMessage()); //$NON-NLS-1$
        }
        else if ( ce instanceof CertificateNotYetValidException ) {
            re = makeErrorTestResult("FAIL_TLS_CERT_NOTYETVALID", chain, ce.getMessage()); //$NON-NLS-1$
        }
        else if ( ce instanceof CertificateRevokedException ) {
            CertificateRevokedException reve = (CertificateRevokedException) ce;
            re = makeErrorTestResult(
                "FAIL_TLS_CERT_REVOKED", //$NON-NLS-1$
                chain,
                reve.getRevocationDate().toString(),
                reve.getRevocationReason() != null ? reve.getRevocationReason().name() : StringUtils.EMPTY,
                ce.getMessage());
        }
        else if ( ce instanceof CertificateParsingException || ce instanceof CertificateEncodingException ) {
            re = makeErrorTestResult("FAIL_TLS_CERT_INVALID", chain, ce.getMessage()); //$NON-NLS-1$
        }
        else {
            re = makeErrorTestResult("FAIL_TLS_CERT_UNKNOWN", chain, ce.getMessage()); //$NON-NLS-1$
        }
        r.addEntry(re);
    }


    /**
     * @param e
     * @param r
     * @param tc
     */
    public static void handleTLSException ( SSLException e, ConfigTestResult r, TLSTestContext tc ) {
        if ( e instanceof SSLHandshakeException ) {
            handleHandshakeException((SSLHandshakeException) e, r, tc);
            return;
        }
        else if ( e.getCause() instanceof SSLHandshakeException ) {
            handleHandshakeException((SSLHandshakeException) e.getCause(), r, tc);
            return;
        }
        else if ( e instanceof SSLPeerUnverifiedException ) {
            TLSValidationUtils.handleTLSNameMismatch(e, r, tc);
            return;
        }

        log.debug("SSL connection failure", e); //$NON-NLS-1$
        r.error("FAIL_TLS_CONNECTION", e.getMessage()); //$NON-NLS-1$
    }

}

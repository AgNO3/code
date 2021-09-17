/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.io.ByteArrayInputStream;
import java.net.IDN;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = HostnameVerifier.class, property = "instanceId=default", configurationPid = "tls.verify" )
public class DefaultHostnameVerifierImpl implements HostnameVerifier {

    /**
     * 
     */
    private static final String X_509_CERT_TYPE = "X.509"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DefaultHostnameVerifierImpl.class);

    private static SignatureAlgorithm[] DEFAULT_VALID_SIG_ALGOS = new SignatureAlgorithm[] {
        SignatureAlgorithm.SHA224, SignatureAlgorithm.SHA224RSA, SignatureAlgorithm.SHA256, SignatureAlgorithm.SHA256RSA, SignatureAlgorithm.SHA384,
        SignatureAlgorithm.SHA384RSA, SignatureAlgorithm.SHA512, SignatureAlgorithm.SHA512RSA, SignatureAlgorithm.SHA224ECDSA,
        SignatureAlgorithm.SHA256ECDSA, SignatureAlgorithm.SHA384ECDSA, SignatureAlgorithm.SHA512ECDSA
    };

    private static Set<String> MD5_SIG_OIDS = new HashSet<>(Arrays.asList(SignatureAlgorithm.MD5.getOid(), SignatureAlgorithm.MD5RSA.getOid()));
    private static Set<String> SHA1_SIG_OIDS = new HashSet<>(
        Arrays.asList(
            SignatureAlgorithm.SHA1.getOid(),
            SignatureAlgorithm.SHA1DSA.getOid(),
            SignatureAlgorithm.SHA1ECDSA.getOid(),
            SignatureAlgorithm.SHA1RSA.getOid()));

    private static Set<String> DEFAULT_VALID_SIG_OIDS = new HashSet<>();


    static {
        for ( SignatureAlgorithm sigAlgo : DEFAULT_VALID_SIG_ALGOS ) {
            DEFAULT_VALID_SIG_OIDS.add(sigAlgo.getOid());
        }
    }

    private boolean allowMD5;
    private boolean allowSHA1;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    /**
     * @param properties
     */
    protected void configure ( Dictionary<String, Object> properties ) {
        this.allowMD5 = ConfigUtil.parseBoolean(properties, "allowMD5Sigs", false); //$NON-NLS-1$
        this.allowSHA1 = ConfigUtil.parseBoolean(properties, "allowSHA1Sigs", false); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    public boolean verify ( String hostname, SSLSession session ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking certificate against hostname " + hostname); //$NON-NLS-1$
        }

        X500Principal p;
        try {
            p = (X500Principal) session.getPeerPrincipal();
        }
        catch ( SSLPeerUnverifiedException e ) {
            log.debug("Failed to get peer principal", e); //$NON-NLS-1$
            return false;
        }

        X509Certificate[] peerCertificateChain;
        try {
            peerCertificateChain = session.getPeerCertificateChain();
        }
        catch ( SSLPeerUnverifiedException e ) {
            log.debug("Failed to get peer certifiacte chain", e); //$NON-NLS-1$
            return false;
        }

        if ( !validateCertChain(peerCertificateChain) ) {
            log.debug("Certificate chain is not accepted"); //$NON-NLS-1$
            return false;
        }

        X509Certificate peerCert = peerCertificateChain[ 0 ];
        java.security.cert.X509Certificate parsedCert;
        try {
            parsedCert = (java.security.cert.X509Certificate) CertificateFactory.getInstance(X_509_CERT_TYPE)
                    .generateCertificate(new ByteArrayInputStream(peerCert.getEncoded()));
        }
        catch (
            CertificateException |
            CertificateEncodingException e ) {
            log.debug("Failed to parse certificate", e); //$NON-NLS-1$
            return false;
        }

        Collection<List<?>> subjectAlternativeNames;
        try {
            subjectAlternativeNames = parsedCert.getSubjectAlternativeNames();
        }
        catch ( CertificateParsingException e ) {
            log.debug("Failed to get subjectaltNames", e); //$NON-NLS-1$
            return false;
        }

        JcaX509CertificateHolder jcaX509CertificateHolder;
        try {
            jcaX509CertificateHolder = new JcaX509CertificateHolder(parsedCert);
        }
        catch ( java.security.cert.CertificateEncodingException e ) {
            log.debug("Failed to parse certificate (BCE)", e); //$NON-NLS-1$
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Hostname: " + hostname); //$NON-NLS-1$
            log.debug("CN: " + p); //$NON-NLS-1$
        }

        if ( subjectAlternativeNames == null || subjectAlternativeNames.isEmpty() ) {
            if ( matchCN(hostname, jcaX509CertificateHolder) ) {
                log.debug("CN matched"); //$NON-NLS-1$
                return true;
            }
        }

        if ( matchSANs(hostname, subjectAlternativeNames) ) {
            log.debug("A subjectAltName matched"); //$NON-NLS-1$
            return true;
        }

        notifyError(hostname);
        return false;
    }


    /**
     * @param hostname
     */
    protected void notifyError ( String hostname ) {
        log.debug(String.format("Hostname '%s' does not match certificate", hostname)); //$NON-NLS-1$
    }


    /**
     * @param hostname
     * @param subjectAlternativeNames
     */
    protected boolean matchSANs ( String hostname, Collection<List<?>> subjectAlternativeNames ) {

        if ( subjectAlternativeNames == null ) {
            return false;
        }

        for ( List<?> entry : subjectAlternativeNames ) {
            int type = (Integer) entry.get(0);
            Object data = entry.get(1);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found SAN type %d: %s", type, data)); //$NON-NLS-1$
            }

            if ( type == 2 ) {
                // dNSname
                String dnsName = (String) data;

                if ( matchCNDNSName(dnsName, hostname) ) {
                    return true;
                }

            }
            else if ( type == 7 ) {
                // iPAddress
                String ipAddress = (String) data;

                if ( hostname.equals(ipAddress) ) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * @param hostname
     * @param jcaX509CertificateHolder
     */
    protected boolean matchCN ( String hostname, JcaX509CertificateHolder jcaX509CertificateHolder ) {
        X500Name subject = jcaX509CertificateHolder.getSubject();
        RDN[] cns = subject.getRDNs(BCStyle.CN);

        for ( RDN cn : cns ) {
            String strCn = IETFUtils.valueToString(cn.getFirst().getValue());
            if ( matchCNDNSName(strCn, hostname) ) {
                return true;
            }
            else if ( log.isDebugEnabled() ) {
                log.debug("Did not match CN " + strCn); //$NON-NLS-1$
            }
        }

        return false;
    }


    private static boolean matchCNDNSName ( String inCert, String toMatch ) {

        // Check for IDN and normalize toMatch
        String asciiToMatch = IDN.toASCII(toMatch).toLowerCase(Locale.ROOT);
        if ( log.isDebugEnabled() ) {
            log.debug("Normalized to match " + asciiToMatch); //$NON-NLS-1$
        }

        // Check for problematic characters in inCert, should be pure ASCII printable
        for ( char c : inCert.toCharArray() ) {
            if ( c == '-' || c == '_' || c == '.' ) {
                continue;
            }
            else if ( c >= 48 && c <= 57 ) {
                continue;
            }
            else if ( c >= 65 && c <= 90 ) {
                continue;
            }
            else if ( c >= 97 && c <= 122 ) {
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Disallowed character in name " + inCert); //$NON-NLS-1$
            }
            return false;
        }

        // Check for wildcard in inCert and match accordingly
        String[] labelsInCert = StringUtils.splitPreserveAllTokens(inCert.toLowerCase(Locale.ROOT), '.');
        String[] labelsInToMatch = StringUtils.splitPreserveAllTokens(asciiToMatch, '.');

        if ( labelsInToMatch.length == 0 || labelsInCert.length != labelsInToMatch.length ) {
            // don't match blank names or names with a different number of labels
            return false;
        }

        for ( int i = 0; i < labelsInCert.length; i++ ) {

            if ( i == 0 && "*".equals(labelsInCert) && labelsInToMatch.length >= 3 ) { //$NON-NLS-1$
                // wildcard match (single level, no substring patterns, only first label)
                // only allow if the name contains at least three labels
                continue;
            }
            else if ( labelsInCert[ i ].equals(labelsInToMatch[ i ]) ) {
                // matched
                continue;
            }
            return false;
        }
        return true;
    }


    /**
     * @param peerCertificateChain
     */
    private boolean validateCertChain ( X509Certificate[] peerCertificateChain ) {
        int i = 0;
        for ( X509Certificate cert : peerCertificateChain ) {
            // skip root certificate validation, signature on the root does not matter as it is on the local system
            if ( i != peerCertificateChain.length - 1 && !validateCert(cert) ) {
                log.debug("Rejecting certificate chain because of disallowed signature algorithm " + cert.getSigAlgOID()); //$NON-NLS-1$
                return false;
            }
            i++;
        }

        return true;
    }


    /**
     * @param cert
     * @return
     */
    private boolean validateCert ( X509Certificate cert ) {
        String sigAlgOid = cert.getSigAlgOID();

        if ( DEFAULT_VALID_SIG_OIDS.contains(sigAlgOid) ) {
            return true;
        }
        else if ( this.allowSHA1 && SHA1_SIG_OIDS.contains(sigAlgOid) ) {
            return true;
        }
        else if ( this.allowMD5 && MD5_SIG_OIDS.contains(sigAlgOid) ) {
            return true;
        }

        log.warn("Chain contains illegal signature algorithm " + cert.getSigAlgName()); //$NON-NLS-1$
        return false;

    }
}

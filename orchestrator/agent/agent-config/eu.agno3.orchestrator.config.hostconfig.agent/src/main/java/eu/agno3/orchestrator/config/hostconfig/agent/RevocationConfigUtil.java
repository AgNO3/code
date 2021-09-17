/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CHECK_ONLY_EE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_CACHE_NEGATIVE_MINUTES;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_CACHE_SIZE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_DOWNLOAD_UNAVAILABLE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_ENABLE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_IGNORE_EXPIRED;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_IGNORE_UNAVAILABLE_CRL;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_REQUIRE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.CRL_UPDATE_INTERVAL_MINUTES;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.HTTP_CONNECT_TIMEOUT;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.HTTP_MAX_REDIRECTS;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.HTTP_READ_TIMEOUT;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_CACHE_SIZE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_CHECK_ALL_WITH_SYSTEM;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_ENABLE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_REQUIRE;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_SYSTEM_CERT;
import static eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigProperties.OCSP_SYSTEM_URI;

import java.security.cert.CertificateEncodingException;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.orchestrator.config.crypto.truststore.CRLCheckLevel;
import eu.agno3.orchestrator.config.crypto.truststore.OCSPCheckLevel;
import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfig;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public final class RevocationConfigUtil {

    /**
     * 
     */
    private RevocationConfigUtil () {}


    /**
     * @param revocationConfiguration
     * @return revocation property configuration
     * @throws CertificateEncodingException
     */
    public static Properties makeRevocationProperties ( RevocationConfig revocationConfiguration ) throws CertificateEncodingException {
        Properties props = new Properties();
        addGeneral(revocationConfiguration, props);
        addOCSP(revocationConfiguration, props);
        addCRL(revocationConfiguration, props);
        return props;
    }


    /**
     * @param revocationConfiguration
     * @param props
     */
    private static void addCRL ( RevocationConfig revocationConfiguration, Properties props ) {
        props.put(CRL_ENABLE, revocationConfiguration.getCrlCheckLevel() != CRLCheckLevel.DISABLE);
        props.put(CRL_REQUIRE, revocationConfiguration.getCrlCheckLevel() == CRLCheckLevel.REQUIRE);
        props.put(CRL_IGNORE_EXPIRED, revocationConfiguration.getCrlCheckLevel() == CRLCheckLevel.OPPORTUNISTIC);
        props.put(CRL_IGNORE_UNAVAILABLE_CRL, revocationConfiguration.getCrlCheckLevel() == CRLCheckLevel.OPPORTUNISTIC);
        props.put(CRL_DOWNLOAD_UNAVAILABLE, revocationConfiguration.getOnDemandCRLDownload());
        props.put(CRL_CACHE_NEGATIVE_MINUTES, revocationConfiguration.getCrlUpdateInterval().toStandardMinutes().getMinutes());
        props.put(CRL_UPDATE_INTERVAL_MINUTES, revocationConfiguration.getCrlUpdateInterval().toStandardMinutes().getMinutes());
        props.put(CRL_CACHE_SIZE, revocationConfiguration.getOnDemandCRLCacheSize());
    }


    /**
     * @param revocationConfiguration
     * @param props
     * @throws CertificateEncodingException
     */
    private static void addOCSP ( RevocationConfig revocationConfiguration, Properties props ) throws CertificateEncodingException {
        props.put(OCSP_REQUIRE, revocationConfiguration.getOcspCheckLevel() == OCSPCheckLevel.REQUIRE);
        props.put(OCSP_ENABLE, revocationConfiguration.getOcspCheckLevel() != OCSPCheckLevel.DISABLE);
        props.put(OCSP_CACHE_SIZE, revocationConfiguration.getOcspCacheSize());
        props.put(OCSP_CHECK_ALL_WITH_SYSTEM, revocationConfiguration.getTrustedResponderCheckAll());

        if ( revocationConfiguration.getUseTrustedResponder() ) {
            props.setProperty(OCSP_SYSTEM_CERT, encodeCertificate(revocationConfiguration.getTrustedResponderTrustCertificate()));
            props.setProperty(OCSP_SYSTEM_URI, revocationConfiguration.getTrustedResponderUri().toString());
        }
    }


    /**
     * @param revocationConfiguration
     * @param props
     */
    private static void addGeneral ( RevocationConfig revocationConfiguration, Properties props ) {
        props.put(CHECK_ONLY_EE, revocationConfiguration.getCheckOnlyEndEntity());
        props.put(HTTP_READ_TIMEOUT, revocationConfiguration.getNetworkTimeout().getStandardMinutes());
        props.put(HTTP_CONNECT_TIMEOUT, revocationConfiguration.getNetworkTimeout().getStandardMinutes());
        props.put(HTTP_MAX_REDIRECTS, 2);
    }


    /**
     * @param cert
     * @return
     * @throws CertificateEncodingException
     */
    private static String encodeCertificate ( X509CertEntry cert ) throws CertificateEncodingException {
        return Base64.encodeBase64String(cert.getCertificate().getEncoded());
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureKeystore;
import eu.agno3.orchestrator.agent.crypto.truststore.units.EnsureTruststore;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig;
import eu.agno3.orchestrator.config.web.PublicKeyPinMode;
import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.orchestrator.config.web.SSLEndpointConfiguration;
import eu.agno3.orchestrator.config.web.SSLSecurityMode;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public final class SSLConfigUtil {

    private static final Set<String> INTERNAL_TRUSTSTORES = new HashSet<>(Arrays.asList("allInvalid", "noVerify"));


    /**
     * 
     */
    private SSLConfigUtil () {}


    /**
     * 
     * @param b
     * @param ctx
     * @param tlsContextName
     * @param subsystem
     * @param sec
     * @param hostnameVerifier
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    public static void setupSSLClientMapping ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, String tlsContextName, String subsystem,
            SSLClientConfiguration sec, String hostnameVerifier )
                    throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        String truststoreAlias = sec.getTruststoreAlias();
        if ( sec.getDisableCertificateVerification() != null && sec.getDisableCertificateVerification() ) {
            truststoreAlias = "noVerify"; //$NON-NLS-1$
        }

        if ( !INTERNAL_TRUSTSTORES.contains(truststoreAlias) ) {
            b.add(EnsureTruststore.class).truststore(truststoreAlias);
            ctx.ensureFactory("truststore", truststoreAlias);
        }

        String actualHostnameVerifier = hostnameVerifier;
        if ( sec.getDisableHostnameVerification() ) {
            actualHostnameVerifier = "noVerify"; //$NON-NLS-1$
        }

        PropertyConfigBuilder props = PropertyConfigBuilder.get();

        props.p("trustStore", truststoreAlias);
        props.p("subsystem", subsystem);
        props.p("hostnameVerifier", actualHostnameVerifier);
        props.p("protocols", sec.getSecurityMode().getProtocols());
        props.p("ciphers", sec.getSecurityMode().getCiphers());

        if ( sec.getPublicKeyPinMode() == PublicKeyPinMode.EXCLUSIVE ) {
            props.p("trustStore", "allInvalid");
        }
        else {
            props.p("trustStore", truststoreAlias);
        }

        List<String> pinKeys = new ArrayList<>();
        for ( PublicKeyEntry publicKeyEntry : sec.getPinnedPublicKeys() ) {
            PublicKey pk = publicKeyEntry.getPublicKey();
            pinKeys.add(String.format("%s:%s", pk.getAlgorithm(), Base64.getEncoder().encodeToString(pk.getEncoded())));
        }
        props.p("pinnedPublicKeys", pinKeys);
        ctx.factory("tls.mapping", tlsContextName, props);
    }


    /**
     * @param trustConfiguration
     * @param truststoreAlias
     * @return path to the OpenSSL truststore
     */
    public static Serializable getOpenSSLTruststorePath ( TruststoresConfig trustConfiguration, String truststoreAlias ) {
        if ( "allInvalid".equals(truststoreAlias) ) {
            return "/etc/truststores/empty/openssl/";
        }
        return String.format("/etc/truststores/%s/openssl/", truststoreAlias);
    }


    /**
     * @param b
     * @param ctx
     * @param tlsContextName
     * @param subsystem
     * @param sec
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    public static void setupSSLEndpointMapping ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, String tlsContextName, String subsystem,
            SSLEndpointConfiguration sec ) throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        b.add(EnsureKeystore.class).keystore(sec.getKeystoreAlias()).user(ctx.getServiceManager().getServicePrincipal()); // $NON-NLS-1$
        ctx.ensureFactory("keystore", sec.getKeystoreAlias());

        Set<String> protocols = sec.getCustomProtocols();
        List<String> ciphers = sec.getCustomCiphers();

        if ( protocols == null || protocols.isEmpty() ) {
            protocols = new HashSet<>(sec.getSecurityMode().getProtocols());
        }

        if ( ciphers == null || ciphers.isEmpty() ) {
            ciphers = sec.getSecurityMode().getCiphers();
        }

        ctx.factory(
            "tls.mapping",
            tlsContextName,
            PropertyConfigBuilder.get().p("keyStore", sec.getKeystoreAlias()).p("keyAlias", sec.getKeyAlias()).p("subsystem", subsystem)
                    .p("protocols", protocols).p("ciphers", ciphers));

    }


    /**
     * 
     * @param mode
     * @return a list of openssl protocol specifiers
     */
    public static List<String> toOpenSSLProtocols ( SSLSecurityMode mode ) {
        List<String> res = new LinkedList<>();
        for ( String proto : mode.getProtocols() ) {
            String openSSLProtocol = toOpenSSLProtocol(proto);
            if ( openSSLProtocol != null ) {
                res.add(openSSLProtocol);
            }
        }
        return res;
    }


    private static String toOpenSSLProtocol ( String proto ) {
        switch ( proto ) {
        case "SSLv3": //$NON-NLS-1$
        case "TLSv1": //$NON-NLS-1$
        case "TLSv1.1": //$NON-NLS-1$
        case "TLSv1.2": //$NON-NLS-1$
            return proto;
        case "SSLv2Hello": //$NON-NLS-1$
            return null;
        default:
            throw new IllegalArgumentException("Unknown protocol " + proto); //$NON-NLS-1$
        }
    }


    /**
     * @param mode
     * @return a list of openssl cipher specifications
     */
    public static List<String> toOpenSSLCiphers ( SSLSecurityMode mode ) {
        List<String> res = new LinkedList<>();
        for ( String cipher : mode.getCiphers() ) {
            String openSSLCipher = toOpenSSLCipher(cipher);
            if ( openSSLCipher != null ) {
                res.add(openSSLCipher);
            }
        }
        return res;
    }

    private static final Map<String, String> OPENSSL_CIPHER_MAP = new HashMap<>();


    static {
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "ECDHE-RSA-AES128-GCM-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "ECDHE-RSA-AES128-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "ECDHE-RSA-AES128-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "DHE-RSA-AES128-GCM-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "DHE-RSA-AES128-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "DHE-RSA-AES128-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_128_GCM_SHA256", "AES128-GCM-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_128_CBC_SHA256", "AES128-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_128_CBC_SHA", "AES128-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "ECDHE-RSA-AES256-GCM-SHA384");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "ECDHE-RSA-AES256-SHA384");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "ECDHE-RSA-AES256-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", "DHE-RSA-AES256-GCM-SHA384");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "DHE-RSA-AES256-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "DHE-RSA-AES256-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_256_GCM_SHA384", "AES256-GCM-SHA384");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_256_CBC_SHA256", "AES256-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_RSA_WITH_AES_256_CBC_SHA", "AES256-SHA");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "ECDHE-RSA-DES-CBC3-SHA");
        OPENSSL_CIPHER_MAP.put("SSL_RSA_WITH_3DES_EDE_CBC_SHA", "DES-CBC3-SHA");

        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "ECDHE-ECDSA-AES128-GCM-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "ECDHE-ECDSA-AES128-SHA256");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "ECDHE-ECDSA-AES256-GCM-SHA384");
        OPENSSL_CIPHER_MAP.put("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "ECDHE-ECDSA-AES256-SHA384");

        OPENSSL_CIPHER_MAP.put("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", StringUtils.EMPTY);
    };


    /**
     * @param cipher
     * @return
     */
    private static String toOpenSSLCipher ( String cipher ) {
        String mapped = OPENSSL_CIPHER_MAP.get(cipher);
        if ( mapped == null ) {
            throw new IllegalArgumentException("Unknown cipher " + cipher); //$NON-NLS-1$
        }

        if ( mapped.isEmpty() ) {
            return null;
        }
        return mapped;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader;


/**
 * @author mbechler
 *
 */
@Component ( service = RevocationConfigReader.class )
public class RevocationConfigReaderImpl implements RevocationConfigReader {

    private static final Logger log = Logger.getLogger(RevocationConfigReaderImpl.class);
    private static final Charset PROPERTIES_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader#fromProperties(java.util.Properties)
     */
    @Override
    public RevocationConfig fromProperties ( Properties props ) {
        RevocationConfigImpl config = new RevocationConfigImpl();
        readGeneralOpts(props, config);
        readCRLOpts(props, config);
        readOCSPOpts(props, config);
        readHTTPOpts(props, config);
        return config;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws FileNotFoundException
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader#fromFile(java.io.File)
     */
    @Override
    public RevocationConfig fromFile ( File file ) throws IOException {
        if ( !file.exists() ) {
            return null;
        }
        Properties props = new Properties();
        try ( FileInputStream fis = new FileInputStream(file);
              InputStreamReader reader = new InputStreamReader(fis, PROPERTIES_CHARSET) ) {
            props.load(reader);
            return this.fromProperties(props);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader#toProperties(eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig)
     */
    @Override
    public Properties toProperties ( RevocationConfig config ) {
        Properties props = new Properties();
        dumpGeneralOpts(config, props);
        dumpCRLOpts(config, props);
        dumpOCSPOpts(config, props);
        dumpHTTPOpts(config, props);
        return props;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader#toFile(java.io.File,
     *      eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig)
     */
    @Override
    public void toFile ( File file, RevocationConfig config ) throws IOException {
        try ( FileChannel ch = FileChannel.open(
                  file.toPath(),
                  EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                  PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"))); //$NON-NLS-1$
              OutputStream os = Channels.newOutputStream(ch);
              OutputStreamWriter writer = new OutputStreamWriter(os, PROPERTIES_CHARSET) ) {
            this.toProperties(config).store(writer, StringUtils.EMPTY);
        }
    }


    /**
     * @param config
     * @param props
     */
    private static void dumpGeneralOpts ( RevocationConfig config, Properties props ) {
        props.put(CHECK_ONLY_EE, String.valueOf(config.isCheckOnlyEndEntityCerts()));
    }


    /**
     * @param props
     * @param config
     */
    private static void readGeneralOpts ( Properties props, RevocationConfigImpl config ) {
        if ( props.contains(CHECK_ONLY_EE) ) {
            config.setCheckOnlyEndEntityCerts(Boolean.valueOf(props.getProperty(CHECK_ONLY_EE)));
        }
    }


    /**
     * @param config
     * @param props
     */
    private static void dumpCRLOpts ( RevocationConfig config, Properties props ) {
        props.put(CRL_ENABLE, String.valueOf(config.isCheckCRL()));
        props.put(CRL_REQUIRE, String.valueOf(config.isRequireCRL()));
        props.put(CRL_IGNORE_EXPIRED, String.valueOf(config.isIgnoreExpiredCRL()));
        props.put(CRL_IGNORE_UNAVAILABLE_CRL, String.valueOf(config.isIgnoreUnavailableCRL()));
        props.put(CRL_DOWNLOAD_UNAVAILABLE, String.valueOf(config.isDownloadCRLs()));
        props.put(CRL_CACHE_SIZE, String.valueOf(config.getCrlCacheSize()));
        props.put(CRL_CACHE_NEGATIVE_MINUTES, String.valueOf(config.getCrlNegativeCacheMinutes()));
        props.put(CRL_UPDATE_INTERVAL_MINUTES, String.valueOf(config.getCrlUpdateIntervalMinutes()));
    }


    private static void readCRLOpts ( Properties props, RevocationConfigImpl config ) {
        if ( props.containsKey(CRL_ENABLE) ) {
            config.setCheckCRL(Boolean.parseBoolean(props.getProperty(CRL_ENABLE)));
        }
        if ( props.containsKey(CRL_REQUIRE) ) {
            config.setRequireCRL(Boolean.parseBoolean(props.getProperty(CRL_REQUIRE)));
        }
        if ( props.containsKey(CRL_IGNORE_EXPIRED) ) {
            config.setIgnoreExpiredCRL(Boolean.parseBoolean(props.getProperty(CRL_IGNORE_EXPIRED)));
        }
        if ( props.containsKey(CRL_IGNORE_UNAVAILABLE_CRL) ) {
            config.setIgnoreNoCRL(Boolean.parseBoolean(props.getProperty(CRL_IGNORE_UNAVAILABLE_CRL)));
        }
        if ( props.containsKey(CRL_DOWNLOAD_UNAVAILABLE) ) {
            config.setDownloadCRLs(Boolean.parseBoolean(props.getProperty(CRL_DOWNLOAD_UNAVAILABLE)));
        }
        if ( props.containsKey(CRL_CACHE_SIZE) ) {
            config.setCrlCacheSize(Integer.parseInt(props.getProperty(CRL_CACHE_SIZE)));
        }
        if ( props.containsKey(CRL_CACHE_NEGATIVE_MINUTES) ) {
            config.setCrlNegativeCacheMinutes(Integer.parseInt(props.getProperty(CRL_CACHE_NEGATIVE_MINUTES)));
        }
        if ( props.containsKey(CRL_UPDATE_INTERVAL_MINUTES) ) {
            config.setCrlUpdateIntervalMinutes(Integer.parseInt(props.getProperty(CRL_UPDATE_INTERVAL_MINUTES)));
        }
    }


    /**
     * @param config
     * @param props
     */
    private static void dumpOCSPOpts ( RevocationConfig config, Properties props ) {
        props.put(OCSP_ENABLE, String.valueOf(config.isCheckOCSP()));
        props.put(OCSP_REQUIRE, String.valueOf(config.isRequireOCSP()));
        props.put(OCSP_CHECK_ALL_WITH_SYSTEM, String.valueOf(config.isCheckAllUsingSystemOCSP()));
        props.put(OCSP_CACHE_SIZE, String.valueOf(config.getOcspCacheSize()));

        if ( config.getSystemOCSPUri() != null ) {
            props.put(OCSP_SYSTEM_URI, config.getSystemOCSPUri().toString().trim());
        }

        dumpOCSPSystemCert(config, props);
    }


    /**
     * @param config
     * @param props
     */
    private static void dumpOCSPSystemCert ( RevocationConfig config, Properties props ) {
        X509Certificate systemOCSPTrustCert = config.getSystemOCSPTrustCert();
        if ( systemOCSPTrustCert != null ) {
            try {
                byte[] encoded = systemOCSPTrustCert.getEncoded();
                props.put(OCSP_SYSTEM_CERT, Base64.encodeBase64String(encoded));
            }
            catch (
                CertificateException |
                IllegalArgumentException e ) {
                log.warn("Failed to export ocsp system cert", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param props
     * @param config
     */
    private static void readOCSPOpts ( Properties props, RevocationConfigImpl config ) {
        if ( props.containsKey(OCSP_ENABLE) ) {
            config.setCheckOCSP(Boolean.parseBoolean(props.getProperty(OCSP_ENABLE)));
        }
        if ( props.containsKey(OCSP_REQUIRE) ) {
            config.setRequireOCSP(Boolean.parseBoolean(props.getProperty(OCSP_REQUIRE)));
        }
        if ( props.containsKey(OCSP_CHECK_ALL_WITH_SYSTEM) ) {
            config.setCheckAllUsingSystemOCSP(Boolean.parseBoolean(props.getProperty(OCSP_CHECK_ALL_WITH_SYSTEM)));
        }
        if ( props.containsKey(OCSP_CACHE_SIZE) ) {
            config.setOcspCacheSize(Integer.parseInt(props.getProperty(OCSP_CACHE_SIZE)));
        }
        if ( props.containsKey(OCSP_SYSTEM_URI) ) {
            try {
                config.setSystemOCSPUri(new URI(props.getProperty(OCSP_SYSTEM_URI)));
            }
            catch ( URISyntaxException e ) {
                log.warn("Failed to parse system ocsp uri", e); //$NON-NLS-1$
            }
        }
        readOCSPSystemCert(props, config);
    }


    /**
     * @param props
     * @param config
     */
    private static void readOCSPSystemCert ( Properties props, RevocationConfigImpl config ) {
        if ( props.containsKey(OCSP_SYSTEM_CERT) ) {
            try {
                config.setSystemOCSPCert((X509Certificate) CertificateFactory.getInstance("X509").generateCertificate( //$NON-NLS-1$
                    new ByteArrayInputStream(Base64.decodeBase64(props.getProperty(OCSP_SYSTEM_CERT)))));
            }
            catch (
                CertificateException |
                IllegalArgumentException e ) {
                log.warn("Failed to parse system ocsp certitificate, expect a base64 encoded x509 certificate", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param config
     * @param props
     */
    private static void dumpHTTPOpts ( RevocationConfig config, Properties props ) {
        props.put(HTTP_CONNECT_TIMEOUT, String.valueOf(config.getConnectTimeout()));
        props.put(HTTP_READ_TIMEOUT, String.valueOf(config.getReadTimeout()));
        props.put(HTTP_MAX_REDIRECTS, String.valueOf(config.getMaxRedirects()));
    }


    /**
     * @param props
     * @param config
     */
    private static void readHTTPOpts ( Properties props, RevocationConfigImpl config ) {
        if ( props.containsKey(HTTP_CONNECT_TIMEOUT) ) {
            config.setConnectTimeout(Integer.parseInt(props.getProperty(HTTP_CONNECT_TIMEOUT)));
        }
        if ( props.containsKey(HTTP_READ_TIMEOUT) ) {
            config.setReadTimeout(Integer.parseInt(props.getProperty(HTTP_READ_TIMEOUT)));
        }
        if ( props.containsKey(HTTP_MAX_REDIRECTS) ) {
            config.setMaxRedirects(Integer.parseInt(props.getProperty(HTTP_MAX_REDIRECTS)));
        }
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateDescriptorLoader;
import eu.agno3.orchestrator.system.update.UpdateDescriptorRef;
import eu.agno3.orchestrator.system.update.UpdateException;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs7.MultiDigestInputStream;
import eu.agno3.runtime.crypto.pkcs7.PKCS7Verifier;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicensingService;
import eu.agno3.runtime.update.UpdateTrustConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = UpdateDescriptorLoader.class, configurationPid = "updateLoader", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class UpdateDescriptorLoaderImpl implements UpdateDescriptorLoader {

    private static final Logger log = Logger.getLogger(UpdateDescriptorLoaderImpl.class);
    private static final Set<String> VALID_CONTENT_TYPES = new HashSet<>(Arrays.asList("text/xml")); //$NON-NLS-1$

    private static final String CHARSET = "UTF-8"; //$NON-NLS-1$
    private static final String DEFAULT_VERSION = "1.0"; //$NON-NLS-1$
    private static final String DEFAULT_UPDATE_URI = "http://updates.agno3.eu/app-update/"; //$NON-NLS-1$

    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC() //$NON-NLS-1$
            .withLocale(Locale.US);

    private TLSContext tlsContext;
    private XmlParserFactory xmlParserFactory;
    private XmlMarshallingService marshallingService;
    private LicensingService licensingService;

    private URI cachedBaseURL;
    private String baseURL;
    private PKCS7Verifier pkcs7verifier;
    private UpdateTrustConfiguration trustConfig;
    private boolean[] expectKeyUsage = new boolean[] {
        true, false, false, false, false, false, false, false, false
    };
    // codesign EKU
    private Set<String> expectEKUs = new HashSet<>(Arrays.asList("1.3.6.1.5.5.7.3.3")); //$NON-NLS-1$
    private String version;


    @Reference
    protected synchronized void setPKCS7Verifier ( PKCS7Verifier pv ) {
        this.pkcs7verifier = pv;
    }


    protected synchronized void unsetPKCS7Verifier ( PKCS7Verifier pv ) {
        if ( this.pkcs7verifier == pv ) {
            this.pkcs7verifier = null;
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


    @Reference ( target = "(subsystem=runtime/update/httpClient)" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Reference
    protected synchronized void setXmlParserFactory ( XmlParserFactory xpf ) {
        this.xmlParserFactory = xpf;
    }


    protected synchronized void unsetXmlParserFactory ( XmlParserFactory xpf ) {
        if ( this.xmlParserFactory == xpf ) {
            this.xmlParserFactory = null;
        }
    }


    @Reference
    protected synchronized void setMarshallingService ( XmlMarshallingService xms ) {
        this.marshallingService = xms;
    }


    protected synchronized void unsetMarshallingService ( XmlMarshallingService xms ) {
        if ( this.marshallingService == xms ) {
            this.marshallingService = null;
        }
    }


    @Reference
    protected void setLicensingService ( LicensingService lic ) {
        this.licensingService = lic;
    }


    protected void unsetLicensingService ( LicensingService lic ) {
        if ( this.licensingService == lic ) {
            this.licensingService = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.baseURL = ConfigUtil.parseString(ctx.getProperties(), "updateUri", DEFAULT_UPDATE_URI); //$NON-NLS-1$
        this.version = ConfigUtil.parseString(ctx.getProperties(), "version", DEFAULT_VERSION); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.UpdateDescriptorLoader#getLatest(java.lang.String, java.lang.String,
     *      eu.agno3.orchestrator.system.update.UpdateDescriptor)
     */
    @Override
    public UpdateDescriptor getLatest ( String stream, String imageType, UpdateDescriptor cached ) throws UpdateException {
        return getFromURL(makeLatestURL(stream, imageType), stream, makeETag(cached));
    }


    /**
     * @param cached
     * @return
     */
    private static String makeETag ( UpdateDescriptor cached ) {
        if ( cached == null ) {
            return null;
        }
        return String.format("%s-%d", cached.getImageType(), cached.getSequence()); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private String getVersion () {
        return this.version;
    }


    /**
     * @param url
     * @return
     * @throws UpdateException
     */
    private UpdateDescriptor getFromURL ( URL url, String stream, String etag ) throws UpdateException {

        try {
            URLConnection conn = url.openConnection();

            if ( ! ( conn instanceof HttpURLConnection ) ) {
                throw new UpdateException("Not a http connection"); //$NON-NLS-1$
            }

            HttpURLConnection httpConn = (HttpURLConnection) conn;
            setupHttpClient(etag, httpConn);

            if ( etag != null && httpConn.getResponseCode() == 304 ) {
                return null;
            }

            if ( httpConn.getResponseCode() != 200 ) {
                throw new UpdateException(String.format(
                    "Could not load descriptor, status is %d: %s (%s)", //$NON-NLS-1$
                    httpConn.getResponseCode(),
                    httpConn.getResponseMessage(),
                    url.toString()));
            }

            String contentType = httpConn.getRequestProperty("Content-Type"); //$NON-NLS-1$
            if ( contentType != null && !VALID_CONTENT_TYPES.contains(contentType) ) {
                throw new UpdateException("Invalid content type " + contentType); //$NON-NLS-1$
            }

            UpdateDescriptor desc;
            Map<ASN1ObjectIdentifier, byte[]> bcDigests;
            try ( InputStream inputStream = httpConn.getInputStream();
                  MultiDigestInputStream dig = new MultiDigestInputStream(inputStream) ) {
                desc = fromStream(dig);
                bcDigests = dig.getBCDigests();
            }

            if ( log.isDebugEnabled() ) {
                for ( Entry<ASN1ObjectIdentifier, byte[]> entry : bcDigests.entrySet() ) {
                    log.debug(entry.getKey() + " : " + Hex.encodeHexString(entry.getValue())); //$NON-NLS-1$
                }
            }

            conn = makeSignatureURL(stream, desc).openConnection();

            if ( ! ( conn instanceof HttpURLConnection ) ) {
                throw new UpdateException("Not a http connection"); //$NON-NLS-1$
            }

            httpConn = (HttpURLConnection) conn;
            setupHttpClient(null, httpConn);

            if ( httpConn.getResponseCode() != 200 ) {
                throw new UpdateException(String.format(
                    "Could not load signature, status is %d: %s (%s)", //$NON-NLS-1$
                    httpConn.getResponseCode(),
                    httpConn.getResponseMessage(),
                    httpConn.getURL().toString()));
            }

            try ( InputStream inputStream = httpConn.getInputStream();
                  ASN1InputStream asn1 = new ASN1InputStream(inputStream) ) {
                ASN1Primitive sigData = asn1.readObject();
                if ( sigData == null ) {
                    throw new UpdateException("Invalid signature file"); //$NON-NLS-1$
                }
                CMSSignedData cmsSignedData = new CMSSignedData(bcDigests, ContentInfo.getInstance(sigData));
                this.pkcs7verifier.verify(this.trustConfig, cmsSignedData, this.expectKeyUsage, this.expectEKUs);
            }
            catch ( CMSException e ) {
                throw new UpdateException("Failed to verify signature", e); //$NON-NLS-1$
            }
            return desc;
        }
        catch (
            IOException |
            CryptoException |
            NoSuchAlgorithmException e ) {
            throw new UpdateException("Failed to load descriptor from " + url, e); //$NON-NLS-1$
        }

    }


    /**
     * @param etag
     * @param httpConn
     * @throws CryptoException
     * @throws ProtocolException
     */
    private void setupHttpClient ( String etag, HttpURLConnection httpConn ) throws CryptoException, ProtocolException {
        if ( httpConn instanceof HttpsURLConnection ) {
            setupTLS((HttpsURLConnection) httpConn);
        }

        httpConn.setAllowUserInteraction(false);
        httpConn.setConnectTimeout(1000);
        httpConn.setInstanceFollowRedirects(true);
        httpConn.setRequestMethod("GET"); //$NON-NLS-1$
        setupConn(httpConn);

        if ( etag != null ) {
            httpConn.setRequestProperty("If-None-Match", etag); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     */
    private void setupConn ( HttpURLConnection conn ) {
        License license = this.licensingService.getLicense();
        if ( license == null ) {
            conn.setRequestProperty(
                "X-Is-Demo", //$NON-NLS-1$
                "true"); //$NON-NLS-1$
        }
        else {
            conn.setRequestProperty(
                "X-License-Id", //$NON-NLS-1$
                license.getLicenseId().toString());
        }
        conn.setRequestProperty("Date", DateTime.now().toString(RFC1123_DATE_TIME_FORMATTER)); //$NON-NLS-1$

    }


    /**
     * @param httpConn
     * @throws CryptoException
     */
    private void setupTLS ( HttpsURLConnection httpConn ) throws CryptoException {
        httpConn.setHostnameVerifier(this.tlsContext.getHostnameVerifier());
        httpConn.setSSLSocketFactory(this.tlsContext.getSocketFactory());
    }


    /**
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private URI getBaseURI () throws URISyntaxException {
        if ( this.cachedBaseURL == null ) {
            this.cachedBaseURL = new URI(this.baseURL);
        }
        return this.cachedBaseURL;
    }


    /**
     * @param format
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private URL resolveFromBase ( String relative ) throws MalformedURLException, URISyntaxException {
        return this.getBaseURI().resolve(relative).toURL();
    }


    /**
     * @param stream
     * @param imageType
     * @return
     * @throws UpdateException
     * @throws MalformedURLException
     */
    private URL makeLatestURL ( String stream, String imageType ) throws UpdateException {
        try {
            return resolveFromBase(String.format(
                "%s/%s/%s/.latest", //$NON-NLS-1$
                URLEncoder.encode(imageType, CHARSET),
                getVersion(),
                URLEncoder.encode(stream, CHARSET)));
        }
        catch (
            MalformedURLException |
            UnsupportedEncodingException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to construct lastest update URI", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ref
     * @return
     * @throws UpdateException
     * @throws MalformedURLException
     */
    private URL makeReferenceURL ( UpdateDescriptorRef ref ) throws UpdateException {
        try {
            return resolveFromBase(String.format(
                "%s/%s/%s/update-%d.xml", //$NON-NLS-1$
                URLEncoder.encode(ref.getImageType(), CHARSET),
                getVersion(),
                URLEncoder.encode(ref.getStream(), CHARSET),
                ref.getSequence()));
        }
        catch (
            MalformedURLException |
            UnsupportedEncodingException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to construct update reference URI", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ref
     * @return
     * @throws UpdateException
     * @throws MalformedURLException
     */
    private URL makeSignatureURL ( String stream, UpdateDescriptor desc ) throws UpdateException {
        try {
            return resolveFromBase(String.format(
                "%s/%s/%s/update-%d.sig", //$NON-NLS-1$
                URLEncoder.encode(desc.getImageType(), CHARSET),
                getVersion(),
                URLEncoder.encode(stream, CHARSET),
                desc.getSequence()));
        }
        catch (
            MalformedURLException |
            UnsupportedEncodingException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to construct update reference URI", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UpdateException
     *
     * @see eu.agno3.orchestrator.system.update.UpdateDescriptorLoader#getReference(eu.agno3.orchestrator.system.update.UpdateDescriptorRef)
     */
    @Override
    public UpdateDescriptor getReference ( UpdateDescriptorRef ref ) throws UpdateException {
        return getFromURL(makeReferenceURL(ref), ref.getStream(), null);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UpdateException
     *
     * @see eu.agno3.orchestrator.system.update.UpdateDescriptorLoader#fromStream(java.io.InputStream)
     */
    @Override
    public UpdateDescriptor fromStream ( InputStream is ) throws UpdateException {
        try {
            return this.marshallingService.unmarshall(UpdateDescriptor.class, this.xmlParserFactory.createStreamReader(is));
        }
        catch (
            XMLBindingException |
            XMLParserConfigurationException e ) {
            throw new UpdateException("Failed to parse update descriptor", e); //$NON-NLS-1$
        }
    }
}

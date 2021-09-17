/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal.proxy;


import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.ProxyConnectionFactory.ProxyEndPoint;
import org.eclipse.jetty.server.Request;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSCipherSuiteSpec;
import eu.agno3.runtime.crypto.tls.TLSCipherSuiteSpecType;
import eu.agno3.runtime.crypto.tls.TLSCipherSuiteUtil;
import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.http.service.ReverseProxyConfig;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.ip.IpUtil;


/**
 * @author mbechler
 *
 */
@Component (
    service = ReverseProxyConfig.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    configurationPid = "httpservice.connector.revproxy" )
public class ReverseProxyConfigImpl implements ReverseProxyConfig {

    /**
     * 
     */
    private static final String HTTP = "http"; //$NON-NLS-1$
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final String HTTPS = "https"; //$NON-NLS-1$
    private static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * 
     */
    private static final String SEP = ", "; //$NON-NLS-1$

    /**
     * 
     */
    private static final String FORWARDED_HEADERS_CACHE = "forwarded-headers"; //$NON-NLS-1$
    private static final String FORWARDED_RFC7239_CACHE = "forwarded-rfc7239"; //$NON-NLS-1$
    private static final String FORWARD_RFC7239_LAST_TRUST_CACHE = "forwarded-rfc7239-last"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ReverseProxyConfigImpl.class);

    private String overrideScheme;
    private Integer overridePort;
    private String overrideHost;

    private String forwardedHostHeader;
    private String forwardedPortHeader;
    private String forwardedRemoteAddrHeader;

    private String forwardedSSLMatchHeader;
    private String forwardedSSLMatchValue;
    private String forwardedSSLCiphersHeader;

    private boolean usePortFromHostHeader;

    private TLSCipherSuiteSpecType forwardedSSLCiphersType;

    private List<TrustedProxyEntry> trustedProxies;

    private boolean rfc7239;
    private boolean haProxy;


    @Override
    public boolean isHAProxy () {
        return this.haProxy;
    }


    @Override
    public String getOverrideHost () {
        return this.overrideHost;
    }


    @Override
    public Integer getOverridePort () {
        return this.overridePort;
    }


    @Override
    public String getOverrideScheme () {
        return this.overrideScheme;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties(), this);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties(), this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#wrapRequest(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public HttpServletRequest wrapRequest ( HttpServletRequest req ) {
        return new ProxyHttpRequestWrapper(this, req);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#wrapResponse(javax.servlet.http.HttpServletResponse,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    public HttpServletResponse wrapResponse ( HttpServletResponse wrappedResp, HttpServletRequest wrappedReq ) {
        return new ProxyHttpResponseWrapper(wrappedResp, wrappedReq);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getOriginalHost(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getOriginalHost ( HttpServletRequest request ) {

        if ( getOverrideHost() != null ) {
            return this.getOverrideHost();
        }

        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }

        if ( this.haProxy ) {
            // layer 4, no override
            return null;
        }
        else if ( this.rfc7239 ) {
            return getRFC7239OriginalHost(request);

        }

        if ( this.forwardedHostHeader != null ) {
            return getForwardedOriginalHost(request);
        }

        return null;
    }


    /**
     * @param request
     * @return
     */
    protected String getForwardedOriginalHost ( HttpServletRequest request ) {
        String header = request.getHeader(this.forwardedHostHeader);
        if ( !StringUtils.isBlank(header) ) {
            if ( this.usePortFromHostHeader ) {
                return extractHost(header);
            }
            return header;
        }
        return null;
    }


    /**
     * @param request
     * @return
     */
    protected String getRFC7239OriginalHost ( HttpServletRequest request ) {
        ForwardedHeader lastTrustedForwared = getRFC7239LastTrustedForwarded(request);
        if ( lastTrustedForwared == null ) {
            return null;
        }
        return extractHost(lastTrustedForwared.getHostSpec());
    }


    /**
     * @param host
     * @return
     */
    private static String extractHost ( String host ) {
        if ( StringUtils.isBlank(host) ) {
            return null;
        }

        int offset = 0;
        if ( host.charAt(0) == '[' ) {
            offset = host.indexOf(']');
            if ( offset < 0 ) {
                log.warn("Invalid host " + host); //$NON-NLS-1$
                return null;
            }
        }

        int sepPos = host.indexOf(':', offset + 1);
        if ( sepPos >= 0 ) {
            return host.substring(0, sepPos);
        }
        return host;
    }


    /**
     * @param request
     * @param host
     * @return
     */
    private Integer extractPort ( HttpServletRequest request, String host ) {
        if ( !StringUtils.isBlank(host) ) {
            int offset = 0;
            if ( host.charAt(0) == '[' ) {
                offset = host.indexOf(']');
                if ( offset < 0 ) {
                    log.warn("Invalid host " + host); //$NON-NLS-1$
                    return null;
                }
            }
            int sepPos = host.lastIndexOf(':', offset + 1);
            if ( sepPos >= 0 && sepPos < host.length() - 1 ) {
                String portStr = host.substring(sepPos + 1);
                if ( StringUtils.isNumeric(portStr) ) {
                    return Integer.valueOf(portStr);
                }
            }

            if ( HTTPS.equals(getOriginalScheme(request)) ) {
                return DEFAULT_HTTPS_PORT;
            }

            return DEFAULT_HTTP_PORT;
        }
        return null;
    }


    /**
     * @param request
     * @return
     */
    private ForwardedHeader getRFC7239LastTrustedForwarded ( HttpServletRequest request ) {
        ForwardedHeader cached = (ForwardedHeader) request.getAttribute(FORWARD_RFC7239_LAST_TRUST_CACHE);
        if ( cached != null ) {
            return cached;
        }

        List<ForwardedHeader> forwarded = getRFC7239ForwardedHeaders(request);
        if ( forwarded == null ) {
            return null;
        }

        String lastFor = request.getRemoteAddr();

        for ( int i = forwarded.size() - 1; i >= 0; i-- ) {
            String extractHost = extractHost(forwarded.get(i).getBySpec());
            if ( StringUtils.isEmpty(extractHost) ) {
                extractHost = lastFor;
            }
            if ( !isTrustedProxy(extractHost) ) {
                return null;
            }

            String forwardedHost = forwarded.get(i).getForSpec();
            if ( i == 0 || StringUtils.isEmpty(forwardedHost) || !isTrustedProxy(forwardedHost) ) {
                request.setAttribute(FORWARD_RFC7239_LAST_TRUST_CACHE, forwarded.get(i));
                return forwarded.get(i);
            }
            lastFor = forwardedHost;
        }

        return null;
    }


    /**
     * @param request
     */
    private static List<ForwardedHeader> getRFC7239ForwardedHeaders ( HttpServletRequest request ) {
        @SuppressWarnings ( "unchecked" )
        List<ForwardedHeader> cached = (List<ForwardedHeader>) request.getAttribute(FORWARDED_RFC7239_CACHE);
        if ( cached != null ) {
            return cached;
        }

        List<ForwardedHeader> elements = new ArrayList<>();
        Enumeration<String> forwardedHeaders = request.getHeaders("Forwarded"); //$NON-NLS-1$
        if ( forwardedHeaders != null ) {
            while ( forwardedHeaders.hasMoreElements() ) {
                elements.addAll(ForwardedHeader.parse(forwardedHeaders.nextElement()));
            }
        }

        request.setAttribute(FORWARDED_RFC7239_CACHE, elements);
        return elements;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getOriginalPort(javax.servlet.http.HttpServletRequest)
     */
    @Override
    @SuppressWarnings ( "resource" )
    public Integer getOriginalPort ( HttpServletRequest request ) {
        if ( this.getOverridePort() != null ) {
            return this.getOverridePort();
        }

        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }
        if ( this.haProxy ) {
            ProxyEndPoint pep = getHAProxyEndpoint(request);
            if ( pep == null ) {
                return null;
            }
            return pep.getLocalAddress().getPort();
        }
        else if ( this.rfc7239 ) {
            ForwardedHeader lastTrustedForwared = getRFC7239LastTrustedForwarded(request);
            if ( lastTrustedForwared == null ) {
                return null;
            }
            return extractPort(request, lastTrustedForwared.getHostSpec());
        }

        if ( this.usePortFromHostHeader ) {
            String header = request.getHeader(this.forwardedHostHeader);
            return extractPort(request, header);
        }

        if ( this.forwardedPortHeader != null ) {
            String header = request.getHeader(this.forwardedPortHeader);
            if ( !StringUtils.isBlank(header) && StringUtils.isNumeric(header) ) {
                return Integer.parseInt(header);
            }

            if ( HTTPS.equals(getOriginalScheme(request)) ) {
                return DEFAULT_HTTPS_PORT;
            }

            return DEFAULT_HTTP_PORT;
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getOriginalScheme(javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public String getOriginalScheme ( HttpServletRequest request ) {
        if ( this.getOverrideScheme() != null ) {
            return this.getOverrideScheme();
        }

        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }

        if ( this.haProxy ) {
            ProxyEndPoint ep = getHAProxyEndpoint(request);
            if ( ep.getAttribute(ProxyConnectionFactory.TLS_VERSION) != null ) {
                return HTTPS;
            }

            return null;
        }
        else if ( this.rfc7239 ) {
            ForwardedHeader lastTrustedForwared = getRFC7239LastTrustedForwarded(request);
            if ( lastTrustedForwared == null ) {
                return null;
            }
            return lastTrustedForwared.getProtoSpec();
        }

        if ( this.forwardedSSLMatchHeader != null && this.forwardedSSLMatchValue != null ) {
            String header = request.getHeader(this.forwardedSSLMatchHeader);
            if ( !StringUtils.isBlank(header) && this.forwardedSSLMatchValue.equalsIgnoreCase(header.trim()) ) {
                return HTTPS;
            }
            return HTTP;
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getOriginalRemoteAddr(javax.servlet.http.HttpServletRequest)
     */
    @Override
    @SuppressWarnings ( "resource" )
    public String getOriginalRemoteAddr ( HttpServletRequest request ) {
        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }

        if ( this.haProxy ) {
            ProxyEndPoint pep = getHAProxyEndpoint(request);
            if ( pep == null ) {
                return null;
            }
            return pep.getRemoteAddress().getHostString();
        }
        else if ( this.rfc7239 ) {
            ForwardedHeader lastTrustedForwared = getRFC7239LastTrustedForwarded(request);
            if ( lastTrustedForwared == null ) {
                return null;
            }
            return lastTrustedForwared.getForSpec();
        }

        if ( this.forwardedRemoteAddrHeader != null ) {
            List<String> fwd = getForwardedHeaders(request);

            // first one before a trusted proxy is the original client
            for ( int i = fwd.size() - 1; i >= 0; i-- ) {
                String host = fwd.get(i);

                if ( isTrustedProxy(host) ) {
                    continue;
                }

                return host;
            }
        }

        return null;
    }


    /**
     * @param request
     * @return
     */
    protected static ProxyEndPoint getHAProxyEndpoint ( HttpServletRequest request ) {
        if ( ! ( request instanceof Request ) ) {
            return null;
        }
        Request r = (Request) request;
        HttpChannel ch = r.getHttpChannel();
        EndPoint ep = ch.getEndPoint();

        if ( ! ( ep instanceof ProxyEndPoint ) ) {
            return null;
        }

        return (ProxyEndPoint) ep;
    }


    protected static EndPoint getWrappedHAProxyEndpoint ( ProxyEndPoint request ) {
        if ( request == null ) {
            return null;
        }
        try {
            Field endpF = request.getClass().getDeclaredField("_endp"); //$NON-NLS-1$
            endpF.setAccessible(true);
            return (EndPoint) endpF.get(request);
        }
        catch ( Exception e ) {
            log.error("Failed to unwrap original request endpoint", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param request
     * @return
     */
    protected static EndPoint getWrappedHAProxyEndpoint ( HttpServletRequest request ) {
        return getWrappedHAProxyEndpoint(getHAProxyEndpoint(request));
    }


    /**
     * @param request
     * @param elements
     * @return
     */
    private List<String> getForwardedHeaders ( HttpServletRequest request ) {
        @SuppressWarnings ( "unchecked" )
        List<String> cached = (List<String>) request.getAttribute(FORWARDED_HEADERS_CACHE);
        if ( cached != null ) {
            return cached;
        }

        List<String> elements = new ArrayList<>();
        Enumeration<String> forwardedHeaders = request.getHeaders(this.forwardedRemoteAddrHeader);
        if ( forwardedHeaders != null ) {
            while ( forwardedHeaders.hasMoreElements() ) {
                String fwHeader = forwardedHeaders.nextElement();
                for ( String val : StringUtils.split(fwHeader, ',') ) {
                    elements.add(val.trim());
                }
            }
        }

        request.setAttribute(FORWARDED_HEADERS_CACHE, elements);
        return elements;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getProxiedVia(javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public String getProxiedVia ( HttpServletRequest request ) {
        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }

        if ( this.haProxy ) {
            EndPoint wrapped = getWrappedHAProxyEndpoint(request);
            if ( wrapped == null ) {
                return request.getRemoteAddr();
            }
            return wrapped.getRemoteAddress().getHostString();
        }
        else if ( this.rfc7239 ) {
            return makeRFC7239ProxyVia(request);
        }

        List<String> forwardedHeaders = getForwardedHeaders(request);
        if ( !forwardedHeaders.isEmpty() ) {
            return makeForwardedProxyVia(request, forwardedHeaders);
        }
        return request.getRemoteAddr();
    }


    /**
     * @param request
     * @param forwardedHeaders
     * @return
     */
    private String makeForwardedProxyVia ( HttpServletRequest request, List<String> forwardedHeaders ) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for ( int i = forwardedHeaders.size() - 1; i >= 0; i-- ) {
            String extractHost = forwardedHeaders.get(i);
            if ( !isTrustedProxy(extractHost) ) {
                break;
            }

            if ( extractHost.equals(request.getRemoteAddr()) ) {
                continue;
            }

            if ( !first ) {
                sb.append(SEP);
            }
            else {
                first = false;
            }
            sb.append(extractHost);
        }

        if ( !first ) {
            sb.append(SEP);
        }
        sb.append(request.getRemoteAddr());
        return sb.toString();
    }


    /**
     * @param request
     * @return
     */
    private String makeRFC7239ProxyVia ( HttpServletRequest request ) {
        String lastFor = request.getRemoteAddr();
        List<String> proxies = new ArrayList<>();
        List<ForwardedHeader> rfc7239ForwardedHeaders = getRFC7239ForwardedHeaders(request);
        for ( int i = rfc7239ForwardedHeaders.size() - 1; i >= 0; i-- ) {
            String extractHost = rfc7239ForwardedHeaders.get(i).getBySpec();
            if ( StringUtils.isEmpty(extractHost) ) {
                extractHost = lastFor;
            }
            if ( StringUtils.isEmpty(extractHost) || !isTrustedProxy(extractHost) ) {
                break;
            }

            if ( !extractHost.equals(request.getRemoteAddr()) ) {
                proxies.add(extractHost);
                continue;
            }

            String forwardedHost = rfc7239ForwardedHeaders.get(i).getForSpec();
            if ( StringUtils.isEmpty(forwardedHost) || !isTrustedProxy(forwardedHost) ) {
                break;
            }
            lastFor = forwardedHost;
        }

        Collections.reverse(proxies);
        proxies.add(request.getRemoteAddr());
        return StringUtils.join(proxies, SEP);
    }


    /**
     * @param remote
     * @return
     */
    private boolean isTrustedProxy ( String remote ) {
        short[] parsed;
        try {
            parsed = IpUtil.parse(remote);
        }
        catch ( IllegalArgumentException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to parse remote address " + remote, e); //$NON-NLS-1$
            }
            return false;
        }

        for ( TrustedProxyEntry e : this.trustedProxies ) {
            if ( e.match(parsed) ) {
                return true;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Not a trusted proxy " + remote); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#haveOriginalSSLCipherSpec()
     */
    @Override
    public boolean haveOriginalSSLCipherSpec () {
        return this.haProxy || this.forwardedSSLCiphersHeader != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ReverseProxyConfig#getOriginalSSLCipherSpec(javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public String getOriginalSSLCipherSpec ( HttpServletRequest request ) {
        if ( !isTrustedProxy(request.getRemoteAddr()) ) {
            return null;
        }

        if ( this.haProxy ) {
            ProxyEndPoint ep = getHAProxyEndpoint(request);
            if ( ep.getAttribute(ProxyConnectionFactory.TLS_VERSION) != null ) {
                // TODO: extract from request if frontend is terminating TLS
                return null;
            }

            // if there is no SSL info, assume that TLS is passed through
            return (String) request.getAttribute("javax.servlet.request.cipher_suite"); //$NON-NLS-1$
        }

        String header = request.getHeader(this.forwardedSSLCiphersHeader);
        if ( StringUtils.isBlank(header) ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Original SSL cipher header is " + header); //$NON-NLS-1$
        }

        try {
            TLSCipherSuiteSpec parseSpec = TLSCipherSuiteUtil.parseSpec(this.forwardedSSLCiphersType, header.trim());
            String javaSpec = parseSpec.toString();
            if ( log.isDebugEnabled() ) {
                log.debug("Transformed SSL cipher header is " + javaSpec); //$NON-NLS-1$
            }
            return javaSpec;
        }
        catch ( CryptoException e ) {
            log.warn("Failed to parse cipher spec " + header, e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param props
     * @return parsed config
     */
    public static ReverseProxyConfig fromProperties ( Dictionary<String, Object> props ) {
        ReverseProxyConfigImpl cfg = new ReverseProxyConfigImpl();
        parseConfig(props, cfg);
        return cfg;
    }


    /**
     * @param props
     * @param cfg
     */
    protected static void parseConfig ( Dictionary<String, Object> props, ReverseProxyConfigImpl cfg ) {
        String overrideUri = ConfigUtil.parseString(props, HttpServiceConfig.OVERRIDE_BASE_URI, (String) null);
        if ( !StringUtils.isBlank(overrideUri) ) {
            try {
                URI u = new URI(overrideUri);
                cfg.overrideScheme = u.getScheme();
                cfg.overrideHost = u.getHost();
                if ( u.getPort() > 0 ) {
                    cfg.overridePort = u.getPort();
                }
                else if ( HTTPS.equals(u.getScheme()) ) {
                    cfg.overridePort = DEFAULT_HTTPS_PORT;
                }
                else if ( HTTP.equals(u.getScheme()) ) {
                    cfg.overridePort = DEFAULT_HTTP_PORT;
                }
                else {
                    cfg.overridePort = null;
                }
            }
            catch ( URISyntaxException e ) {
                log.error("Failed to parse override URI", e); //$NON-NLS-1$
            }
        }
        else {
            String overrideHost = ConfigUtil.parseString(props, "proxyOverrideHost", (String) null); //$NON-NLS-1$
            if ( !StringUtils.isBlank(overrideHost) ) {
                cfg.overrideHost = overrideHost;
            }

            String overridePort = ConfigUtil.parseString(props, "proxyOverridePort", (String) null); //$NON-NLS-1$
            if ( !StringUtils.isBlank(overridePort) ) {
                cfg.overridePort = Integer.parseInt(overridePort);
            }

            String overrideScheme = ConfigUtil.parseString(props, "proxyOverrideScheme", (String) null); //$NON-NLS-1$
            if ( !StringUtils.isBlank(overrideScheme) ) {
                cfg.overrideScheme = overrideScheme;
            }
        }

        List<TrustedProxyEntry> trustedProxyEntries = new LinkedList<>();
        for ( String trustedProxy : ConfigUtil.parseStringSet(props, "proxyTrustedAddrs", Collections.EMPTY_SET) ) { //$NON-NLS-1$
            try {
                trustedProxyEntries.add(TrustedProxyEntry.fromString(trustedProxy));
            }
            catch ( IllegalArgumentException e ) {
                log.error("Failed to parse trusted proxy specification", e); //$NON-NLS-1$
            }
        }
        cfg.trustedProxies = trustedProxyEntries;

        cfg.rfc7239 = ConfigUtil.parseBoolean(props, "proxyRFC7239", false); //$NON-NLS-1$
        cfg.haProxy = ConfigUtil.parseBoolean(props, "proxyHAProxy", false); //$NON-NLS-1$

        cfg.forwardedHostHeader = ConfigUtil.parseString(props, "proxyForwardedHostHeader", null); //$NON-NLS-1$
        cfg.usePortFromHostHeader = ConfigUtil.parseBoolean(props, "proxyUsePortFromHostHeader", false); //$NON-NLS-1$
        cfg.forwardedPortHeader = ConfigUtil.parseString(props, "proxyForwardedPortHeader", null); //$NON-NLS-1$

        cfg.forwardedSSLMatchHeader = ConfigUtil.parseString(props, "proxyForwardedSSLHeader", null); //$NON-NLS-1$
        cfg.forwardedSSLMatchValue = ConfigUtil.parseString(
            props,
            "proxyForwardedSSLHeaderValue", //$NON-NLS-1$
            HTTPS);

        cfg.forwardedRemoteAddrHeader = ConfigUtil.parseString(props, "proxyForwardedRemoteAddrHeader", null); //$NON-NLS-1$

        cfg.forwardedSSLCiphersHeader = ConfigUtil.parseString(props, "proxyForwardedSSLCiphersHeader", null); //$NON-NLS-1$
        String type = ConfigUtil.parseString(props, "proxyForwardedSSLCiphersType", TLSCipherSuiteSpecType.OPENSSL.name()); //$NON-NLS-1$
        try {
            cfg.forwardedSSLCiphersType = TLSCipherSuiteSpecType.valueOf(type);
        }
        catch ( IllegalArgumentException e ) {
            log.error("Invalid cipher suite type " + type, e); //$NON-NLS-1$
        }
    }
}

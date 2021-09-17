/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.filter;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.http.service.filter.FilterConfig;
import eu.agno3.runtime.http.ua.UACapability;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.security.web.SecurityHeadersFilterConfig;

import net.sf.uadetector.ReadableUserAgent;


/**
 * @author mbechler
 *
 */
@Component ( service = Filter.class )
@FilterConfig ( priority = -10000 )
@WebFilter ( asyncSupported = true, filterName = "Security header filter", urlPatterns = "/*" )
public class SecurityHeadersFilter extends PathMatchingFilter {

    private static final String APPLICATION_BINARY = "application/binary"; //$NON-NLS-1$
    private static final String X_ROBOTS_TAG = "X-Robots-Tag"; //$NON-NLS-1$
    private static final String X_PERMITTED_CROSS_DOMAIN_POLICIES = "X-Permitted-Cross-Domain-Policies"; //$NON-NLS-1$

    private static final String X_WEB_KIT_CSP = "X-WebKit-CSP"; //$NON-NLS-1$
    private static final String X_CONTENT_SECURITY_POLICY = "X-Content-Security-Policy"; //$NON-NLS-1$
    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String[] CSP_HEADERS = {
        CONTENT_SECURITY_POLICY, X_CONTENT_SECURITY_POLICY, X_WEB_KIT_CSP
    };

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options"; //$NON-NLS-1$
    private static final String X_XSS_PROTECTION = "X-XSS-Protection"; //$NON-NLS-1$
    private static final String X_XSS_PROTECTION_ENABLE = "1; mode=block"; //$NON-NLS-1$
    private static final String X_FRAME_OPTIONS = "X-Frame-Options"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SecurityHeadersFilter.class);

    private static final Set<String> BAD_CONTENT_TYPES = new HashSet<>();

    static {
        BAD_CONTENT_TYPES.add("application/x-gears-worker"); //$NON-NLS-1$
        BAD_CONTENT_TYPES.add("text/x-cross-domain-policy"); //$NON-NLS-1$
    }

    private UADetector uaDetector;
    private SecurityHeadersFilterConfig config;


    /**
     * 
     */
    public SecurityHeadersFilter () {
        setName("SecurityPolicyFilter"); //$NON-NLS-1$
    }


    /**
     * @param detector
     * @param cfg
     */
    public SecurityHeadersFilter ( UADetector detector, SecurityHeadersFilterConfig cfg ) {
        this();
        this.uaDetector = detector;
        this.config = cfg;
    }


    @Reference
    protected synchronized void setUADetector ( UADetector uad ) {
        this.uaDetector = uad;
    }


    protected synchronized void unsetUADetector ( UADetector uad ) {
        if ( this.uaDetector == uad ) {
            this.uaDetector = null;
        }
    }


    @Reference
    protected synchronized void setConfiguration ( SecurityHeadersFilterConfig cfg ) {
        this.config = cfg;
    }


    protected synchronized void unsetConfiguration ( SecurityHeadersFilterConfig cfg ) {
        if ( this.config == cfg ) {

        }
    }


    private ReadableUserAgent getUserAgent ( HttpServletRequest req ) {
        if ( this.uaDetector == null ) {
            return null;
        }

        return this.uaDetector.parse(req);
    }


    private boolean hasCapability ( UACapability cap, ReadableUserAgent ua, String rawUa ) {
        if ( this.uaDetector == null ) {
            return false;
        }

        return this.uaDetector.hasCapability(cap, ua, rawUa);
    }


    /**
     * @param req
     * @return
     */
    private String getRawUA ( HttpServletRequest req ) {
        if ( this.uaDetector == null ) {
            return req.getHeader("User-Agent"); //$NON-NLS-1$
        }
        return this.uaDetector.getUA(req);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        setPolicyHeaders(httpReq, httpResp);

        String reqUri = httpReq.getRequestURI().toLowerCase();

        if ( this.config.isDenyIndex() ) {
            if ( handleNoIndex(httpReq, httpResp, reqUri) ) {
                return;
            }
        }

        if ( httpReq.isRequestedSessionIdFromURL() ) {
            log.warn("Request using a URL encoded session id, invalidating session"); //$NON-NLS-1$
            HttpSession session = httpReq.getSession();
            if ( session != null ) {
                session.invalidate();
            }
        }

        if ( this.config.isHSTSEnabled() ) {
            httpResp.setHeader(
                "Strict-Transport-Security", //$NON-NLS-1$
                String.format(
                    "max-age=%d%s%s", //$NON-NLS-1$
                    this.config.getHSTSMaxAge(),
                    this.config.isHSTSIncludeSubdomains() ? ";includeSubDomains" : StringUtils.EMPTY, //$NON-NLS-1$
                    this.config.isHSTSPreload() ? ";preload" : StringUtils.EMPTY //$NON-NLS-1$
                ));
        }

        chain.doFilter(req, wrapResponse(httpResp));

        if ( !StringUtils.isBlank(resp.getContentType()) && BAD_CONTENT_TYPES.contains(resp.getContentType()) ) {
            log.warn("Setting response type to application/binary as the current content type is dangerous: " + resp.getContentType()); //$NON-NLS-1$
            resp.setContentType(APPLICATION_BINARY);
        }
    }


    /**
     * @param resp
     * @return
     */
    private static ServletResponse wrapResponse ( HttpServletResponse resp ) {
        return new NoURLSesssionResponseWrapper(resp);
    }


    /**
     * @param httpReq
     * @param httpResp
     * @param reqUri
     * @return
     * @throws IOException
     */
    private static boolean handleNoIndex ( HttpServletRequest httpReq, HttpServletResponse httpResp, String reqUri ) throws IOException {
        httpResp.setHeader(X_ROBOTS_TAG, "noindex, nofollow"); //$NON-NLS-1$
        // robots.txt is now handled by guard handler
        return false;
    }


    /**
     * @param resp
     */
    private void setPolicyHeaders ( HttpServletRequest req, HttpServletResponse resp ) {
        // allow non-interpreting user agents to prevent all that cruft
        if ( req.getHeader("X-Non-Interactive") != null ) { //$NON-NLS-1$
            return;
        }
        ReadableUserAgent ua = getUserAgent(req);
        resp.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff"); //$NON-NLS-1$
        resp.setHeader(X_FRAME_OPTIONS, "SAMEORIGIN"); //$NON-NLS-1$
        resp.setHeader(X_PERMITTED_CROSS_DOMAIN_POLICIES, "none-this-response"); //$NON-NLS-1$
        resp.setHeader(X_XSS_PROTECTION, X_XSS_PROTECTION_ENABLE);
        setCSPHeader(req, resp, ua, getRawUA(req), this.config.getDefaultCSPHeader());
    }


    /**
     * @param ua
     * @param cspHeader
     */
    private void setCSPHeader ( HttpServletRequest req, HttpServletResponse resp, ReadableUserAgent ua, String rawUa, String cspHeader ) {

        String expanded = expandCSP(cspHeader, req);

        if ( this.hasCapability(UACapability.CSP10_STANDARD_HEADER, ua, rawUa) ) {
            resp.setHeader(CONTENT_SECURITY_POLICY, expanded);
        }
        else if ( this.hasCapability(UACapability.CSP10_EXPERIMENTAL_HEADER, ua, rawUa) ) {
            resp.setHeader(X_CONTENT_SECURITY_POLICY, expanded);
        }
        else if ( this.hasCapability(UACapability.CSP10_WEBKIT_HEADER, ua, rawUa) ) {
            resp.setHeader(X_WEB_KIT_CSP, expanded);
        }
        else {
            // unknown UA
            for ( String hdr : CSP_HEADERS ) {
                resp.setHeader(hdr, expanded);
            }
        }
    }


    /**
     * @param cspHeader
     * @return
     */
    private static String expandCSP ( String cspHeader, HttpServletRequest req ) {
        String portString = StringUtils.EMPTY;
        if ( ( req.isSecure() && req.getServerPort() != 443 ) || ( !req.isSecure() && req.getServerPort() != 80 ) ) {
            portString = ":" + req.getServerPort(); //$NON-NLS-1$
        }
        String base = String.format("//%s%s%s/", req.getServerName(), portString, req.getServletContext().getContextPath()); //$NON-NLS-1$
        return cspHeader.replaceAll(Pattern.quote("{base}"), base); //$NON-NLS-1$
    }
}

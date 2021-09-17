/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.init;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.security.terms.TermsFilter;
import eu.agno3.runtime.security.web.filter.SecurityHeadersFilter;
import eu.agno3.runtime.security.web.gui.terms.TermsBean;


/**
 * 
 * 
 * This should actually go to the eu.agno3.runtime.security.terms and be an
 * osgi service. Howevery this is currently not possible due to PAXCDI-229.
 * 
 * @author mbechler
 *
 */
@ApplicationScoped
public class TermsFilterImpl implements TermsFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 7606460513018993618L;

    /**
     * 
     */
    private static final String TERMS_PREFIX = "/terms/"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(TermsFilterImpl.class);

    @Inject
    private TermsBean terms;


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig arg0 ) throws ServletException {}


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {}


    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain ch ) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        String reqPath = WebUtils.getPathWithinApplication(httpReq);
        if ( reqPath.startsWith(TERMS_PREFIX) ) {
            String fname = reqPath.substring(TERMS_PREFIX.length());
            int sepDot = fname.lastIndexOf('.');
            if ( fname.indexOf('/') >= 0 || sepDot <= 0 || sepDot == fname.length() - 1 ) {
                httpResp.sendError(404);
                return;
            }

            String id = fname.substring(0, sepDot);
            String fmt = fname.substring(sepDot + 1);
            deliverTermsContent(id, fmt, httpReq, httpResp);
            return;
        }

        Collection<TermsDefinition> unaccepted = this.terms.getUnacceptedTerms();
        if ( !unaccepted.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Have unaccepted terms: " + unaccepted.stream().map(x -> x.getId()).collect(Collectors.toList())); //$NON-NLS-1$
            }
            handleUnacceptedTerms(httpReq, httpResp);
            return;
        }

        ch.doFilter(req, resp);
    }


    /**
     * @param httpResp
     * @throws IOException
     */
    private void handleUnacceptedTerms ( HttpServletRequest httpReq, HttpServletResponse httpResp ) throws IOException {
        String requestedWith = httpReq.getHeader("X-Requested-With"); //$NON-NLS-1$
        Session s = SecurityUtils.getSubject().getSession(false);
        String redirectTo = this.terms.getUnacceptedRedirect();
        // prevent unnecessary session creation, return http error for XMLHttpRequest
        if ( s == null || StringUtils.isBlank(redirectTo)
                || ( !StringUtils.isBlank(requestedWith) && "XMLHttpRequest".equalsIgnoreCase(requestedWith) ) ) { //$NON-NLS-1$
            httpResp.sendError(403, "Need to accept terms first"); //$NON-NLS-1$
            return;
        }

        String contextPath = httpReq.getContextPath();
        redirectTo = ( contextPath != null ? contextPath : StringUtils.EMPTY ) + redirectTo;

        if ( log.isDebugEnabled() ) {
            log.debug("Saving request and redirecting to " + redirectTo); //$NON-NLS-1$
        }

        WebUtils.saveRequest(httpReq);
        httpResp.sendRedirect(redirectTo);
    }


    /**
     * @param fmt
     * @param id
     * @param req
     * @param resp
     * @throws IOException
     */
    private void deliverTermsContent ( String id, String fmt, HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        try {
            Enumeration<Locale> locales = req.getLocales();
            Locale l = Locale.ROOT;
            if ( locales.hasMoreElements() ) {
                l = locales.nextElement();
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Delivering terms '%s' fmt %s locale %s", id, fmt, l)); //$NON-NLS-1$
            }
            URL contents = this.terms.getContents(id, fmt, l);
            if ( contents == null ) {
                try ( PrintWriter pw = resp.getWriter() ) {
                    resp.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
                    pw.write("<html><head></head><body>"); //$NON-NLS-1$
                    pw.write("<p class=\"invalid-terms\">The requested terms are not configured properly</p>"); //$NON-NLS-1$
                    pw.write("</body></html>"); //$NON-NLS-1$
                }
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // set sandboxing headers
            for ( String cspHeader : SecurityHeadersFilter.CSP_HEADERS ) {
                resp.setHeader(cspHeader, "default-src 'none'; sandbox;"); //$NON-NLS-1$
            }

            URLConnection conn = contents.openConnection();
            try ( InputStream is = conn.getInputStream() ) {
                resp.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
                resp.setContentLengthLong(conn.getContentLengthLong());
                IOUtils.copy(is, resp.getOutputStream());
            }
        }
        catch ( IOException e ) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}

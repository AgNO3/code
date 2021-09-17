/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2014 by mbechler
 */
package eu.agno3.runtime.jsf.filters;


import java.io.IOException;
import java.util.Locale;

import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author mbechler
 * 
 */
public class CachingFilter implements Filter {

    private static final String EXPIRES_HEADER = "Expires"; //$NON-NLS-1$
    private static final String PRAGMA_HEADER = "Pragma"; //$NON-NLS-1$
    private static final String NO_CACHE_CONTROL = "no-cache, no-store, must-revalidate, private"; //$NON-NLS-1$
    private static final String PUBLIC_CACHE_CONTROL = "public"; //$NON-NLS-1$
    private static final String CACHE_CONTROL_HEADER = "Cache-Control"; //$NON-NLS-1$
    private static final String GET = "get"; //$NON-NLS-1$

    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC() //$NON-NLS-1$
            .withLocale(Locale.US);


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {}


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {
        if ( req instanceof HttpServletRequest && resp instanceof HttpServletResponse ) {

            HttpServletRequest httpRequest = (HttpServletRequest) req;
            if ( !GET.equalsIgnoreCase(httpRequest.getMethod()) ) {
                chain.doFilter(req, resp);
                return;
            }

            HttpServletResponse httpResponse = (HttpServletResponse) resp;

            String requestURI = httpRequest.getRequestURI();
            if ( requestURI != null && requestURI.contains(ResourceHandler.RESOURCE_IDENTIFIER) ) {
                // resources are always publicly available
                httpResponse.setHeader(CACHE_CONTROL_HEADER, PUBLIC_CACHE_CONTROL);
                httpResponse.setHeader(EXPIRES_HEADER, RFC1123_DATE_TIME_FORMATTER.print(DateTime.now().plusMonths(1)));
            }
            else {
                httpResponse.setHeader(CACHE_CONTROL_HEADER, NO_CACHE_CONTROL);
                httpResponse.setHeader(PRAGMA_HEADER, "no-cache"); //$NON-NLS-1$
                httpResponse.setDateHeader(EXPIRES_HEADER, 0);
            }

        }

        chain.doFilter(req, resp);

    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig arg0 ) throws ServletException {}

}

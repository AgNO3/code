/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.filter;


import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    Filter.class
}, configurationPid = WelcomeRedirectFilter.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
@WebFilter ( displayName = "Welcome filter", urlPatterns = {
    "/*"
}, dispatcherTypes = {
    DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR
} )
@eu.agno3.runtime.http.service.filter.FilterConfig ( priority = Integer.MIN_VALUE )
public class WelcomeRedirectFilter implements Filter {

    private static final Logger log = Logger.getLogger(WelcomeRedirectFilter.class);

    /**
     * Configuration PID
     */
    public static final String PID = "httpservice.welcome"; //$NON-NLS-1$

    /**
     * Path to redirect to, appended to request URL
     */
    public static final String WELCOME_REDIRECT_TO = "redirectTo"; //$NON-NLS-1$

    private String redirectTo;


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {

        if ( log.isTraceEnabled() ) {
            log.trace("WelcomeRedirectFilter::doFilter called " + this.redirectTo); //$NON-NLS-1$
        }

        if ( this.redirectTo != null ) {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            String reqPath = httpReq.getServletPath();

            if ( log.isTraceEnabled() ) {
                log.trace("Request path: " + reqPath); //$NON-NLS-1$
            }

            if ( reqPath != null && reqPath.endsWith("/") ) { //$NON-NLS-1$
                String tgt = reqPath + this.redirectTo;
                if ( log.isDebugEnabled() ) {
                    log.debug("Redirecting to " + tgt); //$NON-NLS-1$
                }
                req.getRequestDispatcher(tgt).forward(req, resp);
            }
            else {
                chain.doFilter(req, resp);
            }

        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig config ) throws ServletException {
        this.redirectTo = config.getInitParameter(WELCOME_REDIRECT_TO);
        if ( log.isDebugEnabled() ) {
            log.debug("Initializing welcome redirect filter with " + this.redirectTo); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {}

}

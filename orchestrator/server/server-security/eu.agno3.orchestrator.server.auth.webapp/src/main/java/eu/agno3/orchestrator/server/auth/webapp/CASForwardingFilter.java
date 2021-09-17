/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class CASForwardingFilter implements Filter {

    private static final Logger log = Logger.getLogger(CASForwardingFilter.class);


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
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain next ) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;

        String pathInfo = httpReq.getRequestURI().substring(httpReq.getContextPath().length());

        if ( pathInfo == null ) {
            throw new ServletException("No PathInfo available"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Requested path is " + pathInfo); //$NON-NLS-1$
        }

        if ( pathInfo.indexOf('.') < 0 ) {
            RequestDispatcher dispatch = req.getRequestDispatcher(pathInfo.concat(".xhtml")); //$NON-NLS-1$
            if ( dispatch == null ) {
                throw new ServletException("Failed to obtain RequestDispatcher"); //$NON-NLS-1$
            }

            dispatch.forward(req, resp);
            return;
        }

        next.doFilter(req, resp);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig conf ) throws ServletException {
        log.debug("Initialiting CASForwardingFilter"); //$NON-NLS-1$
    }

}

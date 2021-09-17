/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web;


import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = SecurityFilter.class, property = "name=" + DenyFilter.FILTER_NAME )
public class DenyFilter extends AccessControlFilter implements SecurityFilter {

    private static final Logger log = Logger.getLogger(DenyFilter.class);

    /**
     * 
     */
    public static final String FILTER_NAME = "deny"; //$NON-NLS-1$

    private static final String ACCESS_DENIED = "Access denied"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.SecurityFilter#getFilterName()
     */
    @Override
    public String getFilterName () {
        return FILTER_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.filter.AccessControlFilter#isAccessAllowed(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, java.lang.Object)
     */
    @Override
    protected boolean isAccessAllowed ( ServletRequest request, ServletResponse response, Object mappedValue ) {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * 
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest req, ServletResponse resp ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Deny request to " + ( (HttpServletRequest) req ).getRequestURI()); //$NON-NLS-1$
        }
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ACCESS_DENIED);
        return false;
    }

}

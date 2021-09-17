/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2016 by mbechler
 */
package eu.agno3.runtime.security.web.filter;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;


/**
 * @author mbechler
 *
 */
public class ExtendedPassThruAuthenticationFilter extends PassThruAuthenticationFilter {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest request, ServletResponse response ) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // report proper error when this is an ajax request, also don't save request uri
        String requestedWith = req.getHeader("X-Requested-With"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(requestedWith) && "XMLHttpRequest".equalsIgnoreCase(requestedWith) ) { //$NON-NLS-1$
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication required"); //$NON-NLS-1$
            return false;
        }

        return super.onAccessDenied(request, response);
    }
}

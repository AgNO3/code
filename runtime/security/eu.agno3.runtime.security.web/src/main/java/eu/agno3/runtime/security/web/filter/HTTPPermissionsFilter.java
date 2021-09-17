/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.runtime.security.web.filter;


import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;


/**
 * @author mbechler
 *
 */
public class HTTPPermissionsFilter extends PermissionsAuthorizationFilter {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.authz.AuthorizationFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest req, ServletResponse resp ) throws IOException {
        WebUtils.toHttp(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}

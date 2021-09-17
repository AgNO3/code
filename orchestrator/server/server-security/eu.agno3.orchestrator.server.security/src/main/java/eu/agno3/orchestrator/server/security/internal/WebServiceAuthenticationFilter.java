/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.security.web.SecurityFilter;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    WebServiceAuthenticationFilter.class, SecurityFilter.class
} )
public class WebServiceAuthenticationFilter extends AuthenticationFilter implements SecurityFilter {

    /**
     * 
     */
    private static final String SESSION_SERVICE_PATH = "/services/session"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String WS_AUTH_FILTER = "wsAuthFilter"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(WebServiceAuthenticationFilter.class);


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.authc.AuthenticationFilter#isAccessAllowed(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, java.lang.Object)
     */
    @Override
    protected boolean isAccessAllowed ( ServletRequest request, ServletResponse response, Object mappedValue ) {
        boolean authenticated = super.isAccessAllowed(request, response, mappedValue);

        Session s = SecurityUtils.getSubject().getSession(false);

        if ( s == null ) {
            log.debug("No session available"); //$NON-NLS-1$
            return authenticated;
        }

        if ( !authenticated && log.isDebugEnabled() ) {
            log.debug("Called unauthenticated " + s.getId()); //$NON-NLS-1$
        }

        return authenticated;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest req, ServletResponse resp ) {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String path = WebUtils.getPathWithinApplication(httpReq);
        if ( SESSION_SERVICE_PATH.equals(path) ) {
            return true;
        }

        if ( "wsdl".equals(httpReq.getQueryString()) ) { //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Allow WSDL request " + httpReq.getRequestURI()); //$NON-NLS-1$
            }
            req.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
            return true;
        }

        log.info("Sending WWW-Authenticate CAS header"); //$NON-NLS-1$
        httpResp.setStatus(401);
        httpResp.setHeader(
            "WWW-Authenticate", //$NON-NLS-1$
            "CAS"); //$NON-NLS-1$
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityFilter#getFilterName()
     */
    @Override
    public String getFilterName () {
        return WS_AUTH_FILTER;
    }

}

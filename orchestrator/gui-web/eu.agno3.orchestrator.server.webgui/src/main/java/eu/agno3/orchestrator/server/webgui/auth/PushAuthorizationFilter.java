/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;


/**
 * @author mbechler
 *
 */
public class PushAuthorizationFilter extends AuthorizationFilter {

    /**
     * 
     */
    private static final String EVENTS_PREFIX = "/events/"; //$NON-NLS-1$
    private static final String COMET_SESSION_ID = "comet.sessionId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(PushAuthorizationFilter.class);


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#isAccessAllowed(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, java.lang.Object)
     */
    @Override
    protected boolean isAccessAllowed ( ServletRequest req, ServletResponse resp, Object arg2 ) {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        String pathInfo = httpReq.getPathInfo();
        if ( pathInfo == null || !pathInfo.startsWith(EVENTS_PREFIX) ) {
            log.warn("Invalid path requested " + pathInfo); //$NON-NLS-1$
            return false;
        }

        String passedCometSessionId = pathInfo.substring(EVENTS_PREFIX.length());
        HttpSession session = httpReq.getSession(false);

        if ( session == null ) {
            log.warn("No session available"); //$NON-NLS-1$
            return false;
        }

        return isAccessAllowed(passedCometSessionId, session);
    }


    /**
     * @param passedCometSessionId
     * @param session
     * @return
     */
    private static boolean isAccessAllowed ( String passedCometSessionId, HttpSession session ) {
        String userSessionId = (String) session.getAttribute(COMET_SESSION_ID);

        if ( userSessionId == null ) {
            log.debug("No comet session found in http session"); //$NON-NLS-1$
            return false;
        }

        if ( !userSessionId.equals(passedCometSessionId) ) {
            log.debug("Mismatch between comet session id and user session"); //$NON-NLS-1$

            if ( log.isDebugEnabled() ) {
                log.debug("Got session id " + passedCometSessionId); //$NON-NLS-1$
                log.debug("User session id " + userSessionId); //$NON-NLS-1$
            }

            return false;
        }

        return true;
    }
}

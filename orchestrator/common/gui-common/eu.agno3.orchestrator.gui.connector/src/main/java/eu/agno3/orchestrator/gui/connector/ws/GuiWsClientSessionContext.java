/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws;


import javax.security.auth.login.CredentialExpiredException;

import org.jasig.cas.client.authentication.AttributePrincipal;

import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.orchestrator.server.session.service.SessionService;


/**
 * @author mbechler
 *
 */
public interface GuiWsClientSessionContext {

    /**
     * @param service
     * @throws SessionException
     * @throws GuiWebServiceException
     */
    void logout ( SessionService service ) throws SessionException, GuiWebServiceException;


    /**
     * @param service
     * @param principal
     * @return session information
     * @throws CredentialExpiredException
     * @throws GuiWebServiceException
     * @throws SessionException
     */
    SessionInfo login ( SessionService service, AttributePrincipal principal ) throws CredentialExpiredException, SessionException,
            GuiWebServiceException;


    /**
     * @param service
     * @return the renewed session
     * @throws SessionException
     * @throws CredentialExpiredException
     * @throws GuiWebServiceException
     */
    SessionInfo renewSession ( SessionService service ) throws SessionException, CredentialExpiredException, GuiWebServiceException;


    /**
     * @return the current session info
     * @throws SessionException
     */
    SessionInfo getCurrentSessionInfo () throws SessionException;


    /**
     * @return the session cookie name
     */
    String getSessionCookieName ();

}

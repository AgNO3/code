/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws;


import javax.security.auth.login.CredentialExpiredException;

import org.jasig.cas.client.authentication.AttributePrincipal;

import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;


/**
 * @author mbechler
 * 
 */
public interface GuiWsClientFactory {

    /**
     * 
     * @param sei
     * @return a service instance
     * @throws GuiWebServiceException
     */
    <T> T createService ( Class<T> sei ) throws GuiWebServiceException;


    /**
     * Instantiates all available services, use for cache warming
     */
    void createAllServices ();


    /**
     * @param principal
     * @return session info
     * @throws CredentialExpiredException
     * @throws GuiWebServiceException
     * @throws SessionException
     */
    SessionInfo login ( AttributePrincipal principal ) throws CredentialExpiredException, GuiWebServiceException, SessionException;

}

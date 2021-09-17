/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.session.service;


import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.security.auth.login.CredentialExpiredException;

import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = SessionServiceDescriptor.NAMESPACE )
public interface SessionService extends SOAPWebService {

    /**
     * 
     * @param ticketCredential
     * @return a list of all known jobs
     * @throws SessionException
     * @throws CredentialExpiredException
     */
    @WebMethod ( action = "loginSSO" )
    @WebResult ( name = "info" )
    SessionInfo login ( @WebParam ( name = "ticketCredential" ) String ticketCredential) throws SessionException, CredentialExpiredException;


    /**
     * 
     * @throws SessionException
     */
    void logout () throws SessionException;


    /**
     * @throws SessionException
     * 
     */
    void keepAlive () throws SessionException;


    /**
     * @param hashMap
     * @return the saved preferences
     * @throws SessionException
     */
    @WebMethod ( action = "savePreferences" )
    @WebResult ( name = "prefs" )
    Map<String, String> savePreferences ( @WebParam ( name = "prefs" ) Map<String, String> hashMap) throws SessionException;


    /**
     * @return the user's preferences
     * @throws SessionException
     */
    @WebMethod ( action = "loadPreferences" )
    @WebResult ( name = "prefs" )
    Map<String, String> loadPreferences () throws SessionException;
}

/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2013 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import org.eclipse.jetty.server.session.SessionHandler;


/**
 * @author mbechler
 * 
 */
public interface SessionManagerFactory {

    /**
     * @param contextName
     * @return a new session handler instance
     */
    SessionHandler createSessionHandler ( String contextName );


    /**
     * @return the session cookie name
     */
    String getSessionCookieName ();

}

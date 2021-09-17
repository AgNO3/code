/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.proxy.ProxyHandler;


/**
 * @author mbechler
 *
 */
public interface ExtendedCASServer extends CentralAuthenticationService {

    /**
     * 
     * @return the proxy handler
     */
    ProxyHandler getProxyHandler ();


    /**
     * @return the services manager
     */
    ServicesManager getServicesManager ();


    /**
     * @return the authentication manager
     */
    AuthenticationManager getAuthenticationManager ();

}

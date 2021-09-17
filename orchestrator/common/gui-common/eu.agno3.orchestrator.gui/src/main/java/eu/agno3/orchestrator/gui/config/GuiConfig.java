/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.config;


import java.net.URI;
import java.security.PublicKey;

import eu.agno3.orchestrator.server.component.ComponentConfig;


/**
 * @author mbechler
 * 
 */
public interface GuiConfig extends ComponentConfig {

    /**
     * @return the auth server URL
     */
    URI getAuthServerURL ();


    /**
     * @param overrideServerName
     * @return the auth server URL
     */
    default URI getAuthServerURL ( String overrideServerName ) {
        return getAuthServerURL();
    }


    /**
     * @return the public key of the auth server, null if regular SSL validation should be used
     */
    PublicKey getAuthServerPubKey ();


    /**
     * @return the authentication session cookie name
     */
    String getSessionCookieName ();

}

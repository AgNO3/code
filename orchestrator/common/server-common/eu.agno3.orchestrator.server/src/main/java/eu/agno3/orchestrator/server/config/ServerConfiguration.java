/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.config;


import java.net.URI;
import java.security.PublicKey;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * 
 */
public interface ServerConfiguration {

    /**
     * 
     * @return the server id
     */
    @NonNull
    UUID getServerId ();


    /**
     * @return the primary authentication server URL
     */
    URI getAuthServerUrl ();


    /**
     * @return hostnames allowed for auth server
     */
    Set<String> getAllowedAuthServerNames ();


    /**
     * @return the authentication server public key, null if regular checking should be used
     */
    PublicKey getAuthServerPubKey ();


    /**
     * @return the auth session cookie name
     */
    String getSessionCookieName ();


    /**
     * @return whether authentication server is the same as server
     */
    boolean isLocalAuthServer ();

}

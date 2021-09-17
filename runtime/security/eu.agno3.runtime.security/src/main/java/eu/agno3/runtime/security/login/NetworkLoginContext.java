/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.login;


/**
 * @author mbechler
 *
 */
public interface NetworkLoginContext extends LoginContext {

    /**
     * 
     * @return the authenticating entities remote address
     */
    String getRemoteAddress ();


    /**
     * 
     * @return the local address
     */
    String getLocalAddress ();


    /**
     * 
     * @return whether the transport protocol is secure
     */
    boolean isTransportSecure ();


    /**
     * @return the local network port
     */
    int getLocalPort ();

}

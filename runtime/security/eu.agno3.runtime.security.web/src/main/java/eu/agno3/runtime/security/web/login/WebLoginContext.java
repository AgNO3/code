/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import java.util.Locale;

import eu.agno3.runtime.security.login.NetworkLoginContext;


/**
 * @author mbechler
 *
 */
public interface WebLoginContext extends NetworkLoginContext {

    /**
     * 
     * @return the authenticating entities user agent
     */
    String getUserAgent ();


    /**
     * @return the context path
     */
    String getLocalContextPath ();


    /**
     * @return the local hostname (VHOST)
     */
    String getLocalHostname ();


    /**
     * @return whether this is http authentication
     */
    boolean isHttpAuth ();


    /**
     * @return the users preferred locale
     */
    Locale getLocale ();


    /**
     * @return base directory for auth resources
     */
    String getAuthBase ();

}

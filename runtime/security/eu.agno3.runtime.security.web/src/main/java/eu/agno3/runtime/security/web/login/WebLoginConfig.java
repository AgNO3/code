/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


/**
 * @author mbechler
 *
 */
public interface WebLoginConfig {

    /**
     * @return the URL to which will be redirected after login success
     */
    String getSuccessUrl ();


    /**
     * @return the cookie which stores the users preferred login method
     */
    String getPreferredRealmCookieName ();


    /**
     * @return the base path where the authentication components reside (filter from redirection)
     */
    String getAuthBasePath ();


    /**
     * @return whether to perform a redirect to the full original url, or only success url
     */
    boolean isDoRedirectToOrigUrl ();


    /**
     * @return the url to redirect to when logged out
     */
    String getLogOutUrl ();


    /**
     * @return the cookie which stores the saved username
     */
    String getSavedUsernameCookieName ();

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.runtime.security.principal;


import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface UserInfo {

    /**
     * 
     * @return the associated user principal
     */
    UserPrincipal getUserPrincipal ();


    /**
     * 
     * @return the creation time
     */
    DateTime getCreated ();


    /**
     * 
     * @return the last password change time
     */
    DateTime getLastPwChange ();


    /**
     * 
     * @return the time this users password expires
     */
    DateTime getPwExpiry ();


    /**
     * @return the last successful login time
     */
    DateTime getLastSuccessfulLogin ();


    /**
     * @return the last failed login time
     * 
     */
    DateTime getLastFailedLogin ();


    /**
     * @return the number of failed login attempts since the last successful one
     * 
     */
    Integer getFailedLoginAttempts ();


    /**
     * 
     * @return whether this user is administratively disabled
     */
    Boolean getDisabled ();


    /**
     * @return the user expiration time
     */
    DateTime getExpires ();

}

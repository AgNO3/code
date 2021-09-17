/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface UserInfo extends SubjectInfo {

    /**
     * @return the user principal
     */
    UserPrincipal getPrincipal ();


    /**
     * 
     * @return the user display name
     */
    String getUserDisplayName ();

}

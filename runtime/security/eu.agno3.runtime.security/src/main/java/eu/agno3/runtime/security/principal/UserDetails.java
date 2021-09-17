/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2016 by mbechler
 */
package eu.agno3.runtime.security.principal;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public interface UserDetails extends Serializable {

    /**
     * 
     * @return the user display name
     */
    String getDisplayName ();


    /**
     * 
     * @return the user's primary mail address
     */
    String getMailAddress ();


    /**
     * 
     * @return the user name
     */
    String getUsername ();


    /**
     * 
     * @return the organization
     */
    String getOrganization ();


    /**
     * 
     * @return the organization unit
     */
    String getOrganizationUnit ();


    /**
     * 
     * @return the job title
     */
    String getJobTitle ();


    /**
     * 
     * @return the preferred language
     */
    String getPreferredLanguage ();


    /**
     * @return the user preferred timezone
     */
    String getTimezone ();
}

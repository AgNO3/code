/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.util.List;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface FileshareAdminConfigProvider {

    /**
     * @return the defined security labels
     */
    List<String> getDefinedSecurityLabels ();


    /**
     * @return the default quota
     */
    Long getGlobalDefaultQuota ();


    /**
     * @return the default user roles
     */
    Set<String> getUserDefaultRoles ();


    /**
     * 
     * @return whether the config contains multiple authentication realms
     */
    boolean isMultiRealm ();

}

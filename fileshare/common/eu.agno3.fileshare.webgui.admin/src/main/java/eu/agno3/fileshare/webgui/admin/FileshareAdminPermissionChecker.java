/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


/**
 * @author mbechler
 *
 */
public interface FileshareAdminPermissionChecker {

    /**
     * 
     * @param perm
     * @return whether the user has the given permission
     */
    public boolean hasPermission ( String perm );
}

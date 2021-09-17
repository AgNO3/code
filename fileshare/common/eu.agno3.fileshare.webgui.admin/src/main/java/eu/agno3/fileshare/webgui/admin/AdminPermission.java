/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_permission" )
public class AdminPermission {

    @Inject
    private FileshareAdminPermissionChecker permChecker;


    /**
     * 
     * @param perm
     * @return whether permission is available
     */
    public boolean hasPermission ( String perm ) {
        return this.permChecker.hasPermission(perm);
    }

}

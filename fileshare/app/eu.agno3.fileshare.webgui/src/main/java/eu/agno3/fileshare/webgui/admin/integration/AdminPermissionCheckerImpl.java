/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.integration;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.fileshare.webgui.admin.FileshareAdminPermissionChecker;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class AdminPermissionCheckerImpl implements FileshareAdminPermissionChecker {

    @Inject
    private CurrentUserBean currentUser;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminPermissionChecker#hasPermission(java.lang.String)
     */
    @Override
    public boolean hasPermission ( String perm ) {
        return this.currentUser.hasPermission(perm);
    }

}

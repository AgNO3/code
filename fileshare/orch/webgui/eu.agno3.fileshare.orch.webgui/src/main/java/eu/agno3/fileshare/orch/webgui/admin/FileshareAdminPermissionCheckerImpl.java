/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import javax.enterprise.context.ApplicationScoped;

import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.webgui.admin.FileshareAdminPermissionChecker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareAdminPermissionCheckerImpl implements FileshareAdminPermissionChecker {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminPermissionChecker#hasPermission(java.lang.String)
     */
    @Override
    public boolean hasPermission ( String perm ) {
        return SecurityUtils.getSubject().isPermitted("fileshare:manage:user"); //$NON-NLS-1$
    }

}

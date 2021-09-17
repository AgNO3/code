/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.roles;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_rolesBean" )
@ViewScoped
public class RolesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2810212804792801067L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private List<String> availableRoleCache;


    /**
     * @return the available roles model
     */
    public List<String> getAvailableRoles () {

        if ( this.availableRoleCache == null ) {
            this.availableRoleCache = makeRoleCache();
        }

        return this.availableRoleCache;
    }


    /**
     * @return
     */
    private List<String> makeRoleCache () {
        try {
            List<String> res = new ArrayList<>(this.fsp.getSubjectService().getAvailableRoles());
            Collections.sort(res);
            return res;
        }
        catch ( FileshareException e ) {
            this.exceptionHandler.handleException(e);
        }
        return Collections.EMPTY_LIST;
    }
}

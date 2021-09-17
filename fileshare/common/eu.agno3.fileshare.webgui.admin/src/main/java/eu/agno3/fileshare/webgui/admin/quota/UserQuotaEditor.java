/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.quota;


import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.user.UserSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_userQuotaEditor" )
public class UserQuotaEditor extends AbstractQuotaEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 8805712475489811893L;

    @Inject
    private UserSelectionBean us;

    @Inject
    private FileshareAdminServiceProvider fsp;


    @Override
    protected Long getCurrentQuota () {
        User u = this.us.getSingleSelection();

        if ( u == null ) {
            return null;
        }

        return u.getQuota();
    }


    @Override
    protected void setQuota ( Long quota ) throws FileshareException {
        this.fsp.getUserService().updateUserQuota(this.us.getSingleSelectionId(), quota);
        this.us.refreshSelection();
    }

}

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
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.group.GroupSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_groupQuotaEditor" )
public class GroupQuotaEditor extends AbstractQuotaEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 8805712475489811893L;

    @Inject
    private GroupSelectionBean gs;

    @Inject
    private FileshareAdminServiceProvider fsp;


    @Override
    protected Long getCurrentQuota () {
        Group g = this.gs.getSingleSelection();

        if ( g == null ) {
            return null;
        }

        return g.getQuota();
    }


    @Override
    protected void setQuota ( Long quota ) throws FileshareException {
        this.fsp.getGroupService().updateGroupQuota(this.gs.getSingleSelectionId(), quota);
        this.gs.refreshSelection();
    }

}

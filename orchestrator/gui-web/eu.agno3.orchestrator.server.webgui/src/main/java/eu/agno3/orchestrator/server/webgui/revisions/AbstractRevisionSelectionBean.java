/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.revisions;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.versioning.RevisionProvider;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractRevisionSelectionBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -201003059024805L;
    private List<SelectItem> selectModel;
    private Long selectedRevision;


    protected AbstractRevisionSelectionBean () {}


    protected abstract RevisionProvider getRevisionProvider ();


    public List<SelectItem> getSelectItems () {
        if ( this.selectModel == null ) {
            try {
                this.selectModel = createSelectModel(this.getRevisionProvider().fetchAllRevisions());
            }
            catch ( AbstractModelException e ) {
                ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_REVISIONS_LOAD_FAILED), e);
            }
        }
        return this.selectModel;
    }


    /**
     * @return the selectedRevision
     */
    public Long getSelectedRevision () {
        return this.selectedRevision;
    }


    /**
     * @param selectedRevision
     *            the selectedRevision to set
     */
    public void setSelectedRevision ( Long selectedRevision ) {
        this.selectedRevision = selectedRevision;
    }


    /**
     * @param fetchRevisions
     * @return
     */
    private static List<SelectItem> createSelectModel ( List<VersionInfo> versions ) {
        List<SelectItem> res = new LinkedList<>();

        for ( VersionInfo ver : versions ) {
            String label = GuiMessages.format(GuiMessages.CONFIG_REVISIONS_REV_LABEL, ver.getRevisionNumber());
            String description = GuiMessages.format(GuiMessages.CONFIG_REVISIONS_REV_DETAIL, ver.getRevisionType(), ver.getRevisionTime());
            SelectItem itm = new SelectItem(ver.getRevisionNumber(), label, description);
            res.add(itm);
        }

        if ( !res.isEmpty() ) {
            SelectItem currentRev = res.get(0);
            if ( currentRev != null ) {
                currentRev.setLabel(GuiMessages.format(GuiMessages.CONFIG_REVISIONS_REV_CUR_DETAIL, currentRev.getValue()));
                currentRev.setNoSelectionOption(true);
            }

        }

        return res;
    }
}

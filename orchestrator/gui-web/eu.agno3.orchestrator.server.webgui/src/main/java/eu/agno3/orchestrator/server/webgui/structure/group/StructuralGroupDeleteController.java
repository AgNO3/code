/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.menu.TreeMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralGroupDeleteController" )
@ApplicationScoped
public class StructuralGroupDeleteController {

    @Inject
    private TreeMenuStateBean menuState;

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private ServerServiceProvider ssp;


    public String delete () throws AbstractModelException, GuiWebServiceException {

        if ( !this.viewContext.isGroupSelected() ) {
            return null;
        }

        GroupStructuralObject selected = this.viewContext.getSelectedGroup();
        StructuralObject parent = this.viewContext.getParentForSelection();
        if ( parent == null ) {
            String errStr = "You cannot delete the root cluster"; //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errStr, errStr));
            return null;
        }

        this.ssp.getService(StructuralObjectService.class).delete(selected);
        this.menuState.reload();
        return StructureUtil.getOutcomeForObjectOverview(parent);
    }
}

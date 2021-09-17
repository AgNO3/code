/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
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
@Named ( "instanceDeleteController" )
@ApplicationScoped
public class InstanceDeleteController {

    @Inject
    private TreeMenuStateBean menuState;

    @Inject
    private StructureViewContextBean viewCtx;

    @Inject
    private ServerServiceProvider ssp;


    public String delete () throws AbstractModelException, GuiWebServiceException {
        if ( !this.viewCtx.isInstanceSelected() ) {
            return null;
        }
        InstanceStructuralObject selectedInstance = this.viewCtx.getSelectedInstance();
        StructuralObject parent = this.viewCtx.getParentForSelection();
        this.ssp.getService(StructuralObjectService.class).delete(selectedInstance);
        this.menuState.reload();

        return StructureUtil.getOutcomeForObjectOverview(parent);
    }
}

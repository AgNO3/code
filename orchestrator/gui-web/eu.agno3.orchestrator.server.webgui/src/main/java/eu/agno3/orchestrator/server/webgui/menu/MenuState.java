/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.ServiceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.structure.menu.SubMenuStateBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "menuState" )
public class MenuState {

    @Inject
    private SubMenuStateBean subMenu;

    @Inject
    private TreeMenuStateBean treeMenu;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private AgentStateTracker agentState;

    @Inject
    private ServiceStateTracker serviceState;


    public String reload () {
        try {
            StructuralObject anchor = this.structureContext.getSelectedAnchor();
            StructuralObject obj = this.structureContext.getSelectedObject();

            if ( anchor instanceof InstanceStructuralObject ) {
                this.agentState.forceRefresh((InstanceStructuralObject) anchor);
            }
            if ( anchor instanceof ServiceStructuralObject ) {
                this.serviceState.forceRefresh((ServiceStructuralObject) anchor);
            }
            if ( obj instanceof InstanceStructuralObject ) {
                this.agentState.forceRefresh((InstanceStructuralObject) obj);
            }
            if ( obj instanceof ServiceStructuralObject ) {
                this.serviceState.forceRefresh((ServiceStructuralObject) obj);
            }

            this.treeMenu.reload();
            this.subMenu.refreshContextModel();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}

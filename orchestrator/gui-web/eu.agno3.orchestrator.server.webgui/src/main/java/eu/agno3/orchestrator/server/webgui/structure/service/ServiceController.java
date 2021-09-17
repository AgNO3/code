/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.menu.TreeMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.structure.menu.SubMenuStateBean;
import eu.agno3.orchestrator.system.monitor.service.MonitoringService;


/**
 * @author mbechler
 * 
 */
@Named ( "serviceController" )
@ApplicationScoped
public class ServiceController {

    @Inject
    private TreeMenuStateBean menuState;

    @Inject
    private SubMenuStateBean subMenuState;

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private ServerServiceProvider ssp;


    public String delete () {

        if ( !this.viewContext.isServiceSelected() ) {
            return null;
        }
        try {
            StructuralObject parent = this.viewContext.getParentForSelection();
            ServiceStructuralObject selected = this.viewContext.getSelectedService();

            this.ssp.getService(StructuralObjectService.class).delete(selected);
            this.menuState.reload();
            this.subMenuState.refreshContextModel();
            return StructureUtil.getOutcomeForObjectOverview(parent, this.viewContext.getSelectedAnchor());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String enable () {
        if ( !this.viewContext.isServiceSelected() ) {
            return null;
        }
        try {
            ServiceStructuralObject selected = this.viewContext.getSelectedService();
            this.ssp.getService(MonitoringService.class).enableService(selected);
            return StructureUtil.getOutcomeForObjectOverview(selected, this.viewContext.getSelectedAnchor());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String disable () {
        if ( !this.viewContext.isServiceSelected() ) {
            return null;
        }
        try {
            ServiceStructuralObject selected = this.viewContext.getSelectedService();
            this.ssp.getService(MonitoringService.class).disableService(selected);
            return StructureUtil.getOutcomeForObjectOverview(selected, this.viewContext.getSelectedAnchor());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String restart () {
        if ( !this.viewContext.isServiceSelected() ) {
            return null;
        }
        try {
            ServiceStructuralObject selected = this.viewContext.getSelectedService();
            this.ssp.getService(MonitoringService.class).restartService(selected);
            return StructureUtil.getOutcomeForObjectOverview(selected, this.viewContext.getSelectedAnchor());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }
}

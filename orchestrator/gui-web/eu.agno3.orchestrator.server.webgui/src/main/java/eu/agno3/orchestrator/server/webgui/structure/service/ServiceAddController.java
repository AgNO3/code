/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.structure.menu.SubMenuStateBean;


/**
 * @author mbechler
 * 
 */
@Named ( "serviceAddController" )
@ApplicationScoped
public class ServiceAddController {

    private static final Logger log = Logger.getLogger(ServiceAddController.class);

    @Inject
    private ServiceAddContextBean addData;

    @Inject
    private SubMenuStateBean menu;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private StructureCacheBean structureCache;


    public Object add () throws AbstractModelException, GuiWebServiceException {
        log.debug("Adding service"); //$NON-NLS-1$

        this.ssp.getService(StructuralObjectService.class).create(this.structureContext.getSelectedInstance(), this.addData.getNewService());
        this.structureCache.flush();
        this.menu.refreshContextModel();
        return StructureUtil.getOutcomeForObjectOverview(this.structureContext.getSelectedInstance());
    }


    public String[] getServiceTypes () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.ssp.getService(ServiceService.class).getApplicableServiceTypes(this.structureContext.getSelectedInstance())
                .toArray(new String[] {});
    }
}

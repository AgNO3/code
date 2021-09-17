/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.menu.TreeMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralGroupAddController" )
@ApplicationScoped
public class StructuralGroupAddController {

    private static final Logger log = Logger.getLogger(StructuralGroupAddController.class);

    @Inject
    private StructuralGroupAddContextBean addData;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private TreeMenuStateBean menu;

    @Inject
    private ServerServiceProvider ssp;


    public Object add () throws AbstractModelException, GuiWebServiceException {
        log.debug("Adding group"); //$NON-NLS-1$

        GroupStructuralObject n = this.ssp.getService(StructuralObjectService.class).create(
            this.structureContext.getSelectedObject(),
            this.addData.getNewGroup());
        this.structureCache.flush();
        this.menu.reload();
        this.menu.setSelectedObject(n);
        return StructureUtil.getOutcomeForObjectOverview(n);
    }

}

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

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
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
@Named ( "instanceAddController" )
@ApplicationScoped
public class InstanceAddController {

    private static final Logger log = Logger.getLogger(InstanceAddController.class);

    @Inject
    private InstanceAddContextBean addData;

    @Inject
    private TreeMenuStateBean menu;

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;


    public Object add () throws AbstractModelException, GuiWebServiceException {
        InstanceStructuralObjectImpl i = this.addData.getNewInstance();

        AgentInfo selectedAgent = this.addData.getSelectedAgent();
        if ( selectedAgent != null ) {
            i.setAgentId(selectedAgent.getComponentId());
            if ( i.getImageType() == null ) {
                i.setImageType(selectedAgent.getImageType());
            }
        }
        i.setDisplayName(this.addData.getHostDisplayName());

        if ( log.isDebugEnabled() ) {
            log.debug("Adding instance " + i.getDisplayName()); //$NON-NLS-1$
        }
        i = this.ssp.getService(StructuralObjectService.class).create(this.structureContext.getSelectedObject(), i);
        this.structureCache.flush();
        this.menu.reload();
        this.menu.setSelectedObject(i);
        return StructureUtil.getOutcomeForObjectOverview(i);

    }

}

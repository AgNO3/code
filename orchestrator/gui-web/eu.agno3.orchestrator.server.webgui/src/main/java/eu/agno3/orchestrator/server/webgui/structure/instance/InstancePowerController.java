/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.service.InstancePowerService;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "instancePowerController" )
public class InstancePowerController {

    @Inject
    private StructureViewContextBean viewCtx;

    @Inject
    private ServerServiceProvider ssp;


    public String shutdown () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        InstanceStructuralObject selectedInstance = this.viewCtx.getSelectedInstance();
        this.ssp.getService(InstancePowerService.class).shutdown(selectedInstance);
        return StructureUtil.getOutcomeForObjectOverview(selectedInstance);
    }


    public String reboot () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        InstanceStructuralObject selectedInstance = this.viewCtx.getSelectedInstance();
        this.ssp.getService(InstancePowerService.class).reboot(selectedInstance);
        return StructureUtil.getOutcomeForObjectOverview(selectedInstance);
    }
}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.service.AgentSystemInformationService;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "agentSysInfo" )
public class AgentSysInfoBean {

    @Inject
    private ServerServiceProvider ssp;


    public void triggerRefresh ( InstanceStructuralObject host )
            throws ModelObjectNotFoundException, GuiWebServiceException, ModelServiceException, AgentOfflineException, AgentDetachedException {
        this.ssp.getService(AgentSystemInformationService.class).triggerRefresh(host);
    }


    public PlatformInformation getPlatformInformation ( InstanceStructuralObject host ) throws SystemInformationException, GuiWebServiceException,
            ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException {
        return this.ssp.getService(AgentSystemInformationService.class).getPlatformInformation(host);
    }


    public NetworkInformation getNetworkInformation ( InstanceStructuralObject host ) throws SystemInformationException, GuiWebServiceException,
            ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException {
        return this.ssp.getService(AgentSystemInformationService.class).getNetworkInformation(host);

    }


    public StorageInformation getStorageInformation ( InstanceStructuralObject host ) throws SystemInformationException, GuiWebServiceException,
            ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException {
        return this.ssp.getService(AgentSystemInformationService.class).getStorageInformation(host);
    }

}

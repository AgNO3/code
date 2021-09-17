/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.service;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = AgentSystemInformationServiceDescriptor.NAMESPACE )
public interface AgentSystemInformationService extends SOAPWebService {

    /**
     * Triggers a refresh of agent system information
     * 
     * @param host
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     */
    @WebMethod ( operationName = "refreshHost" )
    void triggerRefresh ( @WebParam ( name = "host" ) InstanceStructuralObject host)
            throws ModelObjectNotFoundException, ModelServiceException, AgentOfflineException, AgentDetachedException;


    /**
     * @param host
     * @return the specified agent's platform information
     * @throws ModelObjectNotFoundException
     * @throws SystemInformationException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    PlatformInformation getPlatformInformation ( @WebParam ( name = "host" ) InstanceStructuralObject host)
            throws ModelObjectNotFoundException, SystemInformationException, ModelServiceException, AgentDetachedException, AgentOfflineException;


    /**
     * @param host
     * @return the specified agent's network information
     * @throws ModelObjectNotFoundException
     * @throws SystemInformationException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    NetworkInformation getNetworkInformation ( @WebParam ( name = "host" ) InstanceStructuralObject host)
            throws ModelObjectNotFoundException, SystemInformationException, ModelServiceException, AgentDetachedException, AgentOfflineException;


    /**
     * @param host
     * @return the specified agent's storage information
     * @throws ModelObjectNotFoundException
     * @throws SystemInformationException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     */
    StorageInformation getStorageInformation ( @WebParam ( name = "host" ) InstanceStructuralObject host)
            throws ModelObjectNotFoundException, SystemInformationException, ModelServiceException, AgentDetachedException, AgentOfflineException;
}

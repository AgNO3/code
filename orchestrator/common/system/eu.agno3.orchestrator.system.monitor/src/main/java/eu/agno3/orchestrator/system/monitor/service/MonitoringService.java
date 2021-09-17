/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.monitor.service;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = MonitoringServiceDescriptor.NAMESPACE )
public interface MonitoringService extends SOAPWebService {

    /**
     * 
     * @param service
     * @return the service runtime status
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "serviceStatus" )
    String getServiceStatus ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param service
     * @return job info for the restart job
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "restartJob" )
    JobInfo restartService ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException;


    /**
     * 
     * @param service
     * @return job info for the disable job
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "disableJob" )
    JobInfo disableService ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException;


    /**
     * 
     * @param service
     * @return job info for the enablke job
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "enableJob" )
    JobInfo enableService ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException;

}

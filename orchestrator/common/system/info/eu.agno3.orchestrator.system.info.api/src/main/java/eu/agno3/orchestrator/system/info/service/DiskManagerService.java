/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.service;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.system.info.storage.VolumeCreationInformation;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = DiskManagerServiceDescriptor.NAMESPACE )
public interface DiskManagerService extends SOAPWebService {

    /**
     * 
     * @param instance
     * @return job info for the scan job
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "scanJob" )
    JobInfo rescanDevices ( @WebParam ( name = "instance" ) InstanceStructuralObject instance) throws ModelObjectNotFoundException,
            ModelServiceException, AgentOfflineException, AgentDetachedException, AgentCommunicationErrorException;


    /**
     * @param instance
     * @param drive
     * @param vol
     * @param v
     * @return job info for the expansion job
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "expandJob" )
    JobInfo expandVolume ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "driveId" ) String drive,
            @WebParam ( name = "volume" ) String vol) throws ModelObjectNotFoundException, ModelServiceException, AgentOfflineException,
                    AgentDetachedException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param info
     * @param drive
     * @param vol
     * @param force
     * @return job info for the initialization job
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "initializeJob" )
    JobInfo initialize ( @WebParam ( name = "instance" ) InstanceStructuralObject instance,
            @WebParam ( name = "volumeInfo" ) VolumeCreationInformation info) throws ModelObjectNotFoundException, ModelServiceException,
                    AgentOfflineException, AgentDetachedException, AgentCommunicationErrorException;

}

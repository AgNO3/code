/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.update.service;


import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.system.update.InstanceUpdateStatus;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = AgentUpdateServiceDescriptor.NAMESPACE )
public interface AgentUpdateService extends SOAPWebService {

    /**
     * @param extraStreams
     * @param instance
     * @return a job checking for updates
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "checkJob" )
    JobInfo checkForUpdates ( @WebParam ( name = "extraStreams" ) Set<String> extraStreams)
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException;


    /**
     * @param instance
     * @param stream
     * @param sequence
     * @return a job installing updates
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "installJob" )
    JobInfo installUpdates ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "fromStream" ) String stream,
            @WebParam ( name = "sequence" ) long sequence) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * @param inst
     * @param overrideStream
     * @return the instance update status
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "updateStatus" )
    InstanceUpdateStatus getUpdateStatus ( @WebParam ( name = "instance" ) InstanceStructuralObject inst,
            @WebParam ( name = "overrideStream" ) String overrideStream) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param instance
     * @param revertStream
     * @param revertSequence
     * @return a job reverting updates
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "revertJob" )
    JobInfo revert ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "revertStream" ) String revertStream,
            @WebParam ( name = "revertSequence" ) long revertSequence) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;
}

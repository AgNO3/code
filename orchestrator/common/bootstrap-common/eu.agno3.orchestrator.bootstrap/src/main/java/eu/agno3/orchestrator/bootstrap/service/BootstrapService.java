/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.service;


import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = BootstrapServiceDescriptor.NAMESPACE )
public interface BootstrapService extends SOAPWebService {

    /**
     * @return the instance that shall be bootstrapped
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "bootstrapContext" )
    BootstrapContext getBootstrapContext () throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param bootstrapContext
     * @param hostConfig
     * @param orchConfig
     * @param adminPassword
     * @return the bootstrap job info
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    JobInfo completeBootstrap ( BootstrapContext bootstrapContext ) throws ModelObjectNotFoundException, ModelServiceException,
            ModelObjectConflictException, ModelObjectValidationException, ModelObjectReferentialIntegrityException, ModelObjectException,
            AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * @return the bootstrap job info
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * 
     */
    JobInfo autoCompleteBootstrap () throws ModelObjectNotFoundException, ModelServiceException, ModelObjectConflictException,
            ModelObjectValidationException, ModelObjectReferentialIntegrityException, ModelObjectException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException;
}

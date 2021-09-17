/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = ConfigApplyServiceDescriptor.NAMESPACE )
public interface ConfigApplyService extends SOAPWebService {

    /**
     * 
     * @param service
     * @param revision
     * @return ctx
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     */
    @WebResult ( name = "applyContext" )
    ConfigApplyContext preApplyServiceConfiguration ( @WebParam ( name = "service" ) ServiceStructuralObject service,
            @WebParam ( name = "revision" ) Long revision)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException;;


    /**
     * Apply a single service configuration
     * 
     * @param service
     * @param revision
     * @param info
     * @return the configuration job applying the configuration
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebResult ( name = "applyJob" )
    JobInfo applyServiceConfiguration ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "revision" ) Long revision,
            @WebParam ( name = "info" ) ConfigApplyInfo info) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * 
     * @param instance
     * @param revision
     * @return ctx
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    @WebResult ( name = "applyContext" )
    ConfigApplyContext preApplyInstanceConfigurations ( @WebParam ( name = "host" ) InstanceStructuralObject instance,
            @WebParam ( name = "revision" ) Long revision)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException;


    /**
     * Apply all of the host's service configurations (with their most recent configs)
     * 
     * @param instance
     * @param revision
     * @param info
     * @param object
     * @return the configuration job applying the configuration
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    JobInfo applyInstanceConfigurations ( @WebParam ( name = "host" ) InstanceStructuralObject instance,
            @WebParam ( name = "revision" ) Long revision, @WebParam ( name = "info" ) ConfigApplyInfo info)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;
}

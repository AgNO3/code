/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 24, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import javax.jws.WebParam;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = ConfigTestServiceDescriptor.NAMESPACE )
public interface ConfigTestService extends SOAPWebService {

    /**
     * 
     * @param object
     * @param rootConfig
     * @param type
     * @param path
     * @param params
     * @return test run id
     * @throws ModelServiceException,
     *             ModelObjectNotFoundException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectException
     * @throws AgentDetachedException
     * @throws JobQueueException
     */
    ConfigTestResultImpl test ( @WebParam ( name = "at" ) StructuralObject object, @WebParam ( name = "config" ) ConfigurationObject rootConfig,
            @WebParam ( name = "configType" ) String type, @WebParam ( name = "configPath" ) String path,
            @WebParam ( name = "params" ) ConfigTestParams params) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectValidationException, ModelObjectException, AgentDetachedException, JobQueueException;


    /**
     * 
     * @param r
     * @return updated test result
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectException
     */
    ConfigTestResultImpl update ( @WebParam ( name = "last" ) ConfigTestResultImpl r)
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectException;
}

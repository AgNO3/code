/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = ServiceServiceDescriptor.NAMESPACE )
public interface ServiceService extends SOAPWebService {

    /**
     * 
     * @param service
     * @return the configuration instance for service
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "configuration" )
    ConfigurationObjectMutable getServiceConfiguration ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param service
     * @return the configuration state
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "configState" )
    ConfigurationState getConfigState ( @WebParam ( name = "service" ) ServiceStructuralObject service)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param service
     * @param serviceConfig
     * @param info
     * @return the updated configuration
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    @WebResult ( name = "updated" )
    <T extends ConfigurationObjectMutable> T updateServiceConfiguration ( @WebParam ( name = "service" ) ServiceStructuralObject service,
            @WebParam ( name = "config" ) T serviceConfig, @WebParam ( name = "info" ) ConfigUpdateInfo info)
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException;


    /**
     * @param instance
     * @return the set of service types that may be added to an instance
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "types" )
    @XmlElementWrapper
    Set<String> getApplicableServiceTypes ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param instance
     * @return the services on the given instance
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "services" )
    @XmlElementWrapper
    Set<ServiceStructuralObject> getServices ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param hostOrService
     * @param hostconfigServiceType
     * @return the effective configuration for the singleton service at host or below service
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "config" )
    ConfigurationObjectReference getServiceConfigurationLocation ( StructuralObject hostOrService, String hostconfigServiceType )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param selectedService
     * @return an edit context
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "context" )
    ConfigurationEditContext<ConfigurationObject, ConfigurationObject> getEditContext (
            @WebParam ( name = "service" ) ServiceStructuralObject selectedService) throws ModelObjectNotFoundException, ModelServiceException;

}

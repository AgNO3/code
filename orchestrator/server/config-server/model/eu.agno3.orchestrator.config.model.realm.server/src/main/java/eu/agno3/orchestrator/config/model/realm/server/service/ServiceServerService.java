/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;


/**
 * @author mbechler
 *
 */
public interface ServiceServerService extends ServiceService {

    /**
     * @param em
     * @param service
     * @param serviceConfig
     * @param info
     * @return the updated service
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    <T extends ConfigurationObjectMutable> T updateServiceConfiguration ( EntityManager em, ServiceStructuralObjectImpl service, T serviceConfig,
            @Nullable ConfigUpdateInfo info )
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException;


    /**
     * @param instance
     * @param em
     * @return the attached services
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    Set<ServiceStructuralObject> getServices ( InstanceStructuralObject instance, EntityManager em )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param em
     * @param instance
     * @param serviceType
     * @return services of the given instance and type
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    Set<ServiceStructuralObject> getServicesOfType ( EntityManager em, InstanceStructuralObjectImpl instance, String serviceType )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param em
     * @param service
     * @return the service configuration
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    ConfigurationObjectMutable getServiceConfiguration ( EntityManager em, ServiceStructuralObjectImpl service )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param instance
     * @param serviceId
     * @return the service
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    ServiceStructuralObject getServiceById ( InstanceStructuralObject instance, UUID serviceId )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException;


    /**
     * @param em
     * @param service
     * @return the actual service object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    ServiceStructuralObject fetch ( EntityManager em, ServiceStructuralObject service ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param em
     * @param instance
     * @param serviceType
     * @param knownId
     * @param knownService
     * @return new or existing service
     * @throws ModelObjectConflictException
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     */
    ServiceStructuralObjectImpl getOrCreateService ( EntityManager em, InstanceStructuralObjectImpl instance, String serviceType,
            @Nullable UUID knownId ) throws ModelServiceException, ModelObjectNotFoundException, ModelObjectConflictException,
                    ModelObjectReferentialIntegrityException, ModelObjectValidationException;


    /**
     * @param em
     * @param persistentAnchor
     * @return map of context services for the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    Map<ServiceStructuralObject, ConfigurationInstance> getEffectiveContextConfigs ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor )
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param em
     * @param pinst
     * @param service
     * @param applying
     * @param updateInstanceState
     */
    void setConfigurationState ( EntityManager em, @Nullable InstanceStructuralObjectImpl pinst, ServiceStructuralObjectImpl service,
            ConfigurationState applying, boolean updateInstanceState );

}

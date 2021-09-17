/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 * 
 */
public interface ConfigRepository extends SystemService {

    /**
     * 
     * @param service
     * @param cfgClass
     * @return the currently active configuration
     * @throws ConfigRepositoryException
     */
    <T extends ConfigurationInstance> Optional<T> getActiveConfiguration ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * 
     * @param service
     * @param cfgClass
     * @return the last active configuration
     * @throws ConfigRepositoryException
     */
    <T extends ConfigurationInstance> Optional<T> getFailsafeConfiguration ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * @param service
     * @param config
     * @throws ConfigRepositoryException
     */
    <T extends ConfigurationInstance> void setActiveConfiguration ( ServiceStructuralObject service, T config ) throws ConfigRepositoryException;


    /**
     * @param service
     * @throws ConfigRepositoryException
     */
    void removeService ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * @param service
     * @throws ConfigRepositoryException
     */
    void registerService ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * @return the services active on this host
     * @throws ConfigRepositoryException
     */
    Collection<ServiceStructuralObject> getServices () throws ConfigRepositoryException;


    /**
     * 
     * @param serviceType
     * @return all active services by the type
     * @throws ConfigRepositoryException
     */
    Collection<ServiceStructuralObject> getServicesByType ( String serviceType ) throws ConfigRepositoryException;


    /**
     * @return references to the services active on this host
     * @throws ConfigRepositoryException
     */
    Collection<StructuralObjectReference> getServiceReferences () throws ConfigRepositoryException;


    /**
     * @param service
     * @return the stored service info
     * @throws ConfigRepositoryException
     */
    ServiceStructuralObject ensureServiceRegistered ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * @param service
     * @return the service type descriptor
     * @throws ConfigRepositoryException
     */
    ServiceTypeDescriptor<@NonNull ?, @NonNull ?> getServiceTypeDescriptor ( ServiceStructuralObject service ) throws ConfigRepositoryException;


    /**
     * @param serviceType
     * @return thte found service, or null if it does not exist
     * @throws ConfigRepositoryException
     */
    ServiceStructuralObject getSingletonServiceByType ( String serviceType ) throws ConfigRepositoryException;


    /**
     * @param service
     * @return the service object
     * @throws ConfigRepositoryException
     */
    ServiceStructuralObject getService ( StructuralObjectReference service ) throws ConfigRepositoryException;

}

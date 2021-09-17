/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;


/**
 * @author mbechler
 *
 */
public interface ConfigurationJobServiceContext {

    /**
     * @return the service
     */
    @NonNull
    ServiceStructuralObject getService ();


    /**
     * @return anchor at which the config is applied
     */
    @NonNull
    StructuralObject getAnchor ();


    /**
     * @return the serviceTypeDescriptor
     */
    ServiceTypeDescriptor<@NonNull ?, @NonNull ?> getServiceTypeDescriptor ();


    /**
     * @param serviceType
     * @return the context service configuration or null
     */
    ConfigurationInstance getContextService ( String serviceType );


    /**
     * @param serviceConfigClass
     * @return the context service configuration or null
     */
    <T extends ConfigurationInstance> T getContextService ( Class<T> serviceConfigClass );


    /**
     * @param type
     * @param serviceManager
     * @return the service manager for the service under configuration
     * @throws ServiceManagementException
     */
    <T extends BaseServiceManager> T getServiceManager ( Class<T> type, ServiceManager serviceManager ) throws ServiceManagementException;

}
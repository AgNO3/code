/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;


/**
 * @author mbechler
 *
 */
public class ConfigurationJobServiceContextImpl implements ConfigurationJobServiceContext {

    @NonNull
    private final ServiceStructuralObject service;
    @NonNull
    private final StructuralObject anchor;

    @NonNull
    private final ServiceTypeDescriptor<@NonNull ?, @NonNull ?> serviceTypeDescriptor;
    private final Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes;
    private final Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices;


    /**
     * @param anchor
     * @param service
     * @param serviceTypeDescriptor
     * @param contextServiceTypes
     * @param contextServices
     */
    public ConfigurationJobServiceContextImpl ( @NonNull StructuralObject anchor, @NonNull ServiceStructuralObject service,
            @NonNull ServiceTypeDescriptor<@NonNull ?, @NonNull ?> serviceTypeDescriptor,
            Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes,
            Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices ) {
        this.anchor = anchor;
        this.service = service;
        this.serviceTypeDescriptor = serviceTypeDescriptor;
        this.contextServiceTypes = contextServiceTypes;
        this.contextServices = contextServices;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getService()
     */
    @Override
    public @NonNull ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getAnchor()
     */
    @Override
    public @NonNull StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getServiceTypeDescriptor()
     */
    @Override
    public ServiceTypeDescriptor<@NonNull ?, @NonNull ?> getServiceTypeDescriptor () {
        return this.serviceTypeDescriptor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getContextService(java.lang.String)
     */
    @Override
    public ConfigurationInstance getContextService ( String serviceType ) {
        Class<? extends ConfigurationInstance> serviceConfigClass = this.contextServiceTypes.get(serviceType);

        if ( serviceConfigClass == null ) {
            return null;
        }

        return getContextService(serviceConfigClass);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getContextService(java.lang.Class)
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends ConfigurationInstance> T getContextService ( Class<T> serviceConfigClass ) {
        return (T) this.contextServices.get(serviceConfigClass);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobServiceContext#getServiceManager(java.lang.Class,
     *      eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
    @Override
    public <T extends BaseServiceManager> T getServiceManager ( Class<T> type, ServiceManager serviceManager ) throws ServiceManagementException {
        return serviceManager.getServiceManager(StructuralObjectReferenceImpl.fromObject(this.getService()), type);
    }

}

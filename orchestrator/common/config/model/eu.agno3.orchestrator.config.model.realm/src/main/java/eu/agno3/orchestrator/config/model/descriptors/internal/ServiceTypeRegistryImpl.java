/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServiceTypeRegistry.class )
public class ServiceTypeRegistryImpl implements ServiceTypeRegistry {

    private Map<String, ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance>> typesByName = new HashMap<>();
    private Map<Class<?>, ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance>> typesByConfigClass = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindDescriptor ( ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> desc ) {
        this.typesByName.put(desc.getTypeName(), desc);
        this.typesByConfigClass.put(desc.getConfigurationType(), desc);
    }


    protected synchronized void unbindDescriptor ( ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> desc ) {
        this.typesByName.remove(desc.getTypeName());
        this.typesByConfigClass.remove(desc.getConfigurationType());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.String)
     */
    @Override
    public @NonNull ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> getDescriptor ( String type )
            throws ModelServiceException {
        ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.typesByName.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No service type found for name " + type); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.Class)
     */
    @Override
    public <T extends ConfigurationInstance> @NonNull ServiceTypeDescriptor<T, T> getDescriptor ( Class<T> type ) throws ModelServiceException {
        @SuppressWarnings ( "unchecked" )
        ServiceTypeDescriptor<T, T> descriptor = (ServiceTypeDescriptor<T, T>) this.typesByConfigClass.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No service type found for config type " + type.getName()); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getServiceTypes()
     */
    @Override
    public Set<String> getServiceTypes () {
        return this.typesByName.keySet();
    }
}

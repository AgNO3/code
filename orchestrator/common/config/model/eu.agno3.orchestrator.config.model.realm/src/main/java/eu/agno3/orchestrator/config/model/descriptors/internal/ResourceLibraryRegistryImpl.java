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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryRegistry;


/**
 * @author mbechler
 * 
 */
@Component ( service = ResourceLibraryRegistry.class )
public class ResourceLibraryRegistryImpl implements ResourceLibraryRegistry {

    private Map<String, ResourceLibraryDescriptor> typesByName = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindDescriptor ( ResourceLibraryDescriptor desc ) {
        this.typesByName.put(desc.getLibraryType(), desc);
    }


    protected synchronized void unbindDescriptor ( ResourceLibraryDescriptor desc ) {
        this.typesByName.remove(desc.getLibraryType());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.String)
     */
    @Override
    public ResourceLibraryDescriptor getDescriptor ( String type ) throws ModelServiceException {
        ResourceLibraryDescriptor descriptor = this.typesByName.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No resource library type found for name " + type); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryRegistry#getResourceLibraryTypes()
     */
    @Override
    public Set<String> getResourceLibraryTypes () {
        return this.typesByName.keySet();
    }
}

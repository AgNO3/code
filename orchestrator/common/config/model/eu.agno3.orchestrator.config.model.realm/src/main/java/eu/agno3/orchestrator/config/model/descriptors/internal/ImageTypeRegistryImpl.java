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
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeRegistry;


/**
 * @author mbechler
 * 
 */
@Component ( service = ImageTypeRegistry.class )
public class ImageTypeRegistryImpl implements ImageTypeRegistry {

    private Map<String, ImageTypeDescriptor> typesByName = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindDescriptor ( ImageTypeDescriptor desc ) {
        this.typesByName.put(desc.getId(), desc);
    }


    protected synchronized void unbindDescriptor ( ImageTypeDescriptor desc ) {
        this.typesByName.remove(desc.getId());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.String)
     */
    @Override
    public ImageTypeDescriptor getDescriptor ( String type ) throws ModelServiceException {
        ImageTypeDescriptor descriptor = this.typesByName.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No image type found for name " + type); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getServiceTypes()
     */
    @Override
    public Set<String> getImageTypes () {
        return this.typesByName.keySet();
    }
}

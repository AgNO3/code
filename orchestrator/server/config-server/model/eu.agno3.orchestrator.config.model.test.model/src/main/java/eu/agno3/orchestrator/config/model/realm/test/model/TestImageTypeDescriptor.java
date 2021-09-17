/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;


/**
 * @author mbechler
 *
 */
@Component ( service = ImageTypeDescriptor.class )
public class TestImageTypeDescriptor implements ImageTypeDescriptor {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getId()
     */
    @Override
    public String getId () {
        return "test"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getForcedServiceTypes()
     */
    @Override
    public Set<String> getForcedServiceTypes () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getApplicableServiceTypes()
     */
    @Override
    public Set<String> getApplicableServiceTypes () {
        return new HashSet<>(Arrays.asList("fileshare")); //$NON-NLS-1$
    }

}

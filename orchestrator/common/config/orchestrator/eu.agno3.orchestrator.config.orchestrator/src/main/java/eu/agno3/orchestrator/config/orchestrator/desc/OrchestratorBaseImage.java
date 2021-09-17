/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.desc;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.i18n.OrchestratorConfigurationMessages;


/**
 * @author mbechler
 *
 */
@Component ( service = ImageTypeDescriptor.class )
public class OrchestratorBaseImage implements ImageTypeDescriptor {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getId()
     */
    @Override
    public String getId () {
        return "urn:agno3:images:1.0:base"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return OrchestratorConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getForcedServiceTypes()
     */
    @Override
    public Set<String> getForcedServiceTypes () {
        return new HashSet<>(Arrays.asList(
            HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE,
            OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getApplicableServiceTypes()
     */
    @Override
    public Set<String> getApplicableServiceTypes () {
        return Collections.EMPTY_SET;
    }

}

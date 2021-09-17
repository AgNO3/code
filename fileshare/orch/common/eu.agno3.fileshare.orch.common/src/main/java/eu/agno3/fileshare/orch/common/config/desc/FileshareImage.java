/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config.desc;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;


/**
 * @author mbechler
 *
 */
@Component ( service = ImageTypeDescriptor.class )
public class FileshareImage implements ImageTypeDescriptor {

    /**
     * 
     */
    public static final String IMAGE_ID = "urn:agno3:images:1.0:fileshare"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getId()
     */
    @Override
    public String getId () {
        return IMAGE_ID;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
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
            FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor#getApplicableServiceTypes()
     */
    @Override
    public Set<String> getApplicableServiceTypes () {
        return new HashSet<>(Arrays.asList(OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE));
    }
}

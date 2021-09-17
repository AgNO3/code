/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config.desc;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareConfigurationMutable;
import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.fileshare.orch.common.jobs.FileshareConfigurationJob;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.terms.TermsResourceLibraryDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServiceTypeDescriptor.class )
public class FileshareServiceTypeDescriptor implements ServiceTypeDescriptor<FileshareConfiguration, FileshareConfigurationMutable> {

    /**
     * 
     */
    @NonNull
    public static final String FILESHARE_SERVICE_TYPE = "urn:agno3:1.0:fileshare"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getTypeName()
     */
    @Override
    public @NonNull String getTypeName () {
        return FILESHARE_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationType()
     */
    @Override
    public @NonNull Class<FileshareConfiguration> getConfigurationType () {
        return FileshareConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getRequiredServices()
     */
    @Override
    public Set<@NonNull String> getRequiredServices () {
        return new HashSet<>(Arrays.asList(HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getReferencedResourceLibraries(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public @NonNull Set<@NonNull ResourceLibraryReference> getReferencedResourceLibraries ( @NonNull FileshareConfiguration config ) {
        Set<@NonNull ResourceLibraryReference> res = new HashSet<>();

        if ( config.getNotificationConfiguration() != null ) {
            String templateLibrary = config.getNotificationConfiguration().getTemplateLibrary();
            if ( templateLibrary != null ) {
                res.add(new ResourceLibraryReference(FileshareMailResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE, templateLibrary));
            }
        }

        if ( config.getWebConfiguration() != null ) {
            String themeLibrary = config.getWebConfiguration().getThemeLibrary();
            if ( themeLibrary != null ) {
                res.add(new ResourceLibraryReference(FileshareWebResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE, themeLibrary));
            }
        }

        if ( config.getUserConfiguration() != null && config.getUserConfiguration().getTermsConfig() != null
                && !config.getUserConfiguration().getTermsConfig().getTerms().isEmpty() ) {
            res.add(
                new ResourceLibraryReference(
                    TermsResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE,
                    config.getUserConfiguration().getTermsConfig().getTermsLibrary())); // $NON-NLS-1$
        }

        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getUsedServices()
     */
    @Override
    public Set<@NonNull String> getUsedServices () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#isMultiInstance()
     */
    @Override
    public boolean isMultiInstance () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationJobClass()
     */
    @Override
    public Class<? extends ConfigurationJob> getConfigurationJobClass () {
        return FileshareConfigurationJob.class;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#makeConfigurationJob(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public @NonNull ConfigurationJob makeConfigurationJob ( @NonNull FileshareConfiguration config ) {
        FileshareConfigurationJob j = new FileshareConfigurationJob();
        j.setFileshareConfig(config);
        return j;
    }

}

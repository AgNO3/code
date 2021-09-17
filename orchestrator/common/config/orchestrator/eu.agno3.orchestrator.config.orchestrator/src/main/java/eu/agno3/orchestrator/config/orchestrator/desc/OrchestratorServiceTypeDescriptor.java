/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.desc;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationMutable;
import eu.agno3.orchestrator.config.orchestrator.i18n.OrchestratorConfigurationMessages;
import eu.agno3.orchestrator.config.orchestrator.jobs.OrchestratorConfigurationJob;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServiceTypeDescriptor.class )
public class OrchestratorServiceTypeDescriptor
        implements ServiceTypeDescriptor<@NonNull OrchestratorConfiguration, @NonNull OrchestratorConfigurationMutable> {

    /**
     * 
     */
    public static final @NonNull String ORCHESTRATOR_SERVICE_TYPE = "urn:agno3:1.0:orchestrator"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getTypeName()
     */
    @Override
    public @NonNull String getTypeName () {
        return ORCHESTRATOR_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return OrchestratorConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationType()
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Class<@NonNull OrchestratorConfiguration> getConfigurationType () {
        return OrchestratorConfiguration.class;
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
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getReferencedResourceLibraries(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Set<@NonNull ResourceLibraryReference> getReferencedResourceLibraries ( @NonNull OrchestratorConfiguration config ) {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationJobClass()
     */
    @Override
    public Class<? extends ConfigurationJob> getConfigurationJobClass () {
        return OrchestratorConfigurationJob.class;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#makeConfigurationJob(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public @NonNull ConfigurationJob makeConfigurationJob ( @NonNull OrchestratorConfiguration config ) {
        OrchestratorConfigurationJob j = new OrchestratorConfigurationJob();
        j.setOrchestratorConfig(config);
        return j;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.desc;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServiceTypeDescriptor.class )
public class HostConfigServiceTypeDescriptor implements ServiceTypeDescriptor<@NonNull HostConfiguration, @NonNull HostConfigurationMutable> {

    /**
     * 
     */
    @NonNull
    public static final String HOSTCONFIG_SERVICE_TYPE = "urn:agno3:1.0:hostconfig"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getTypeName()
     */
    @Override
    public @NonNull String getTypeName () {
        return HOSTCONFIG_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return HostConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationType()
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Class<@NonNull HostConfiguration> getConfigurationType () {
        return HostConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getRequiredServices()
     */
    @Override
    public Set<@NonNull String> getRequiredServices () {
        return Collections.EMPTY_SET;
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
    @Override
    public @NonNull Set<@NonNull ResourceLibraryReference> getReferencedResourceLibraries ( @NonNull HostConfiguration config ) {
        Set<@NonNull ResourceLibraryReference> refs = new HashSet<>();
        if ( config.getTrustConfiguration() != null ) {
            TruststoresConfig tcs = config.getTrustConfiguration();
            for ( TruststoreConfig tc : tcs.getTruststores() ) {
                if ( tc.getTrustLibrary() != null && tc.getAlias() != null ) {
                    refs.add(
                        new ResourceLibraryReference(TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE, tc.getTrustLibrary(), tc.getAlias()));
                }
            }
        }
        return refs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationJobClass()
     */
    @Override
    public Class<? extends ConfigurationJob> getConfigurationJobClass () {
        return HostConfigurationJob.class;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#makeConfigurationJob(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public @NonNull ConfigurationJob makeConfigurationJob ( @NonNull HostConfiguration config ) {
        HostConfigurationJob j = new HostConfigurationJob();
        j.setHostConfig(config);
        return j;
    }

}

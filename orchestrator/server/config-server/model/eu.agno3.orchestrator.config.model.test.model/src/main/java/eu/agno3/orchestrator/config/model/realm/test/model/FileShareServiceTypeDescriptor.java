/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServiceTypeDescriptor.class )
public class FileShareServiceTypeDescriptor
        implements ServiceTypeDescriptor<@NonNull FileShareConfiguration, @NonNull FileShareConfigurationMutable> {

    /**
    * 
    */
    @NonNull
    public static final String FILESHARE_SERVICE_TYPE = "fileshare"; //$NON-NLS-1$


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
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getConfigurationType()
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Class<@NonNull FileShareConfiguration> getConfigurationType () {
        return FileShareConfiguration.class;
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
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#getReferencedResourceLibraries(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Set<@NonNull ResourceLibraryReference> getReferencedResourceLibraries ( @NonNull FileShareConfiguration config ) {
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
        return ConfigurationJob.class;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor#makeConfigurationJob(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public @NonNull ConfigurationJob makeConfigurationJob ( @NonNull FileShareConfiguration config ) {
        // TODO:
        return new ConfigurationJob();
    }
}

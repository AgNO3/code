/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;


/**
 * @author mbechler
 * @param <T>
 * @param <TMutable>
 * 
 */
public interface ServiceTypeDescriptor <T extends ConfigurationInstance, TMutable extends T> {

    /**
     * @return the type name
     */
    @NonNull
    String getTypeName ();


    /**
     * @return the type class
     */
    @NonNull
    Class<T> getConfigurationType ();


    /**
     * 
     * @return the localization base
     */
    String getLocalizationBase ();


    /**
     * 
     * @return the services that are required on the same instance
     */
    Set<@NonNull String> getRequiredServices ();


    /**
     * 
     * @return the services that are used by this service (apart from required services)
     */
    Set<@NonNull String> getUsedServices ();


    /**
     * 
     * @param config
     * @return resource libraries referenced by the configuration
     */
    @NonNull
    Set<@NonNull ResourceLibraryReference> getReferencedResourceLibraries ( @NonNull T config );


    /**
     * 
     * @return whether multiple services of this type may be configured for a instance
     */
    boolean isMultiInstance ();


    /**
     * @param id
     *            service instance id
     * @param config
     * @return a configuration job
     */
    @NonNull
    ConfigurationJob makeConfigurationJob ( @NonNull T config );


    /**
     * 
     * @return the configuration job class
     */
    Class<? extends ConfigurationJob> getConfigurationJobClass ();
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.api;


import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public interface ServiceStorageUtil {

    /**
     * 
     * @param b
     * @param ctx
     * @param sc
     * @param alias
     * @param overridePath
     * @param serviceUser
     * @return the storage context
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws InvalidUnitConfigurationException
     */
    StorageContext ensureStorageAccess ( JobBuilder b, ConfigurationJobContext<?, ?> ctx, StorageConfiguration sc, String alias, Path overridePath,
            String serviceUser ) throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException;


    /**
     * @param storageAlias
     * @param oldAlias
     * @param overridePath
     * @param oldOverridePath
     * @return whether storage migration is required
     */
    boolean checkMigrationNeeded ( String storageAlias, String oldAlias, Path overridePath, Path oldOverridePath );


    /**
     * @param b
     * @param octx
     * @param sc
     * @param storageAlias
     * @param oldAlias
     * @param overridePath
     * @param oldOverridePath
     * @param userName
     * @return storage context for the migrated storage
     * @throws JobBuilderException
     * @throws InvalidUnitConfigurationException
     * @throws UnitInitializationFailedException
     */
    StorageContext migrateStorage ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<?, ?> octx, StorageConfiguration sc, String storageAlias,
            String oldAlias, Path overridePath, Path oldOverridePath, String userName )
                    throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException;

}
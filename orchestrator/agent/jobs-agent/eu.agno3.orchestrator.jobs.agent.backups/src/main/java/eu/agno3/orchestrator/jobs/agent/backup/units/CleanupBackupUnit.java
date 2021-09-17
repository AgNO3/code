/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.nio.file.Path;

import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;


/**
 * @author mbechler
 *
 */
public class CleanupBackupUnit implements BackupUnit {

    private String id;
    private String storageAlias;
    private boolean shared;
    private Path overrideMountpoint;
    private String relativePath;


    /**
     * @param id
     * @param storageAlias
     * @param shared
     * @param overrideMountpoint
     * 
     */
    public CleanupBackupUnit ( String id, String storageAlias, boolean shared, Path overrideMountpoint ) {
        this.id = id;
        this.storageAlias = storageAlias;
        this.shared = shared;
        this.overrideMountpoint = overrideMountpoint;
    }


    /**
     * @param id
     * @param storageAlias
     * @param relativePath
     * @param shared
     * @param overrideMountpoint
     * 
     */
    public CleanupBackupUnit ( String id, String storageAlias, String relativePath, boolean shared, Path overrideMountpoint ) {
        this.id = id;
        this.storageAlias = storageAlias;
        this.relativePath = relativePath;
        this.shared = shared;
        this.overrideMountpoint = overrideMountpoint;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnit#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * @return the storageAlias
     */
    public String getStorageAlias () {
        return this.storageAlias;
    }


    /**
     * @return the relativePath
     */
    public String getRelativePath () {
        return this.relativePath;
    }


    /**
     * @return the shared
     */
    public boolean isShared () {
        return this.shared;
    }


    /**
     * @return override mountpoint for special storage locations (system,dev)
     * 
     */
    public Path getOverrideMountpoint () {
        return this.overrideMountpoint;
    }
}

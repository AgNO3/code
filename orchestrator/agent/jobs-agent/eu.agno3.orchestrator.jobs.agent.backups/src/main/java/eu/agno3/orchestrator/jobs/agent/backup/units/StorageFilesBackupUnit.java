/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.nio.file.Path;


/**
 * @author mbechler
 *
 */
public class StorageFilesBackupUnit extends AbstractFileBackupUnit {

    private String storageAlias;
    private boolean shared;
    private String relativePath;
    private Path overrideMountpoint;


    /**
     * @param id
     * @param storageAlias
     * @param shared
     * @param overrideMountpoint
     */
    public StorageFilesBackupUnit ( String id, String storageAlias, boolean shared, Path overrideMountpoint ) {
        super(id);
        this.overrideMountpoint = overrideMountpoint;
        this.relativePath = null;
        this.storageAlias = storageAlias;
        this.shared = shared;
    }


    /**
     * @param id
     * @param relativePath
     * @param storageAlias
     * @param shared
     * @param overrideMountpoint
     */
    public StorageFilesBackupUnit ( String id, String storageAlias, String relativePath, boolean shared, Path overrideMountpoint ) {
        super(id);
        this.relativePath = relativePath;
        this.storageAlias = storageAlias;
        this.shared = shared;
        this.overrideMountpoint = overrideMountpoint;
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

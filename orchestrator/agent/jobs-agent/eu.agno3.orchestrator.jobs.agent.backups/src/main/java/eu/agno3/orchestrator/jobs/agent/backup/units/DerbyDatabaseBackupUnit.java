/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;


/**
 * @author mbechler
 *
 */
public class DerbyDatabaseBackupUnit implements BackupUnit {

    private String dataSource;

    private RuntimeServiceManager runtimeServiceManager;

    private String databaseStorageAlias;
    private String databaseStoragePath = "db/"; //$NON-NLS-1$
    private boolean databaseStorageShared = false;


    /**
     * @param ds
     * @param dbStorage
     * @param rsm
     */
    public DerbyDatabaseBackupUnit ( String ds, String dbStorage, RuntimeServiceManager rsm ) {
        this.dataSource = ds;
        this.databaseStorageAlias = dbStorage;
        this.runtimeServiceManager = rsm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnit#getId()
     */
    @Override
    public String getId () {
        return "ds-" + this.dataSource; //$NON-NLS-1$
    }


    /**
     * @return the datasource
     */
    public String getDataSource () {
        return this.dataSource;
    }


    /**
     * @return the runtimeServiceManager
     */
    public RuntimeServiceManager getRuntimeServiceManager () {
        return this.runtimeServiceManager;
    }


    /**
     * @return the storage alias the database is stored on
     * 
     */
    public String getDatabaseStorageAlias () {
        return this.databaseStorageAlias;
    }


    /**
     * 
     * @return whether the database storage is shared
     */
    public boolean isDatabaseStorageShared () {
        return this.databaseStorageShared;
    }


    /**
     * @return the databaseStoragePath
     */
    public String getDatabaseStoragePath () {
        return this.databaseStoragePath;
    }
}

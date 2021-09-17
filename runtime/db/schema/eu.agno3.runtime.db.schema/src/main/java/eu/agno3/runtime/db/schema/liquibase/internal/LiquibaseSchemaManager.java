/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.net.URL;
import java.sql.SQLException;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.AdministrativeDataSourceUtil;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.DatabaseException;
import eu.agno3.runtime.db.schema.ChangeFileListener;
import eu.agno3.runtime.db.schema.ChangeFileProvider;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.SchemaManager;
import eu.agno3.runtime.db.schema.SchemaRegistration;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseChangeLogFactory;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseServiceLocator;

import liquibase.Contexts;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.ChangeLogSyncVisitor;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockServiceFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = SchemaManager.class, configurationPid = LiquibaseSchemaManager.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LiquibaseSchemaManager implements SchemaManager, ChangeFileListener {

    /**
     * 
     */
    public static final String PID = "eu.agno3.runtime.db.schema.liquibase.internal.LiquibaseSchemaManager"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(LiquibaseSchemaManager.class);

    private Object lock = new Object();

    private LiquibaseSchemaManagerConfig config;

    private AdministrativeDataSource adminDataSource;
    private DataSourceUtil dsUtil;
    private LiquibaseDatabaseFactory databaseFactory;
    private DatabaseChangeLog changeLog;

    private ChangeFileProvider changeFileProvider;
    private LiquibaseChangeLogFactory changeLogFactory;

    private Database db;
    private boolean upToDate = false;
    private boolean emptyChangeLog;
    private long changeLogLoaded;


    @Reference
    protected synchronized void setConfig ( LiquibaseSchemaManagerConfig conf ) {
        this.config = conf;
    }


    protected synchronized void unsetConfig ( LiquibaseSchemaManagerConfig conf ) {
        if ( this.config == conf ) {
            this.config = null;
        }
    }


    @Reference
    protected synchronized void setAdminDataSource ( AdministrativeDataSource ds ) {
        this.adminDataSource = ds;
    }


    protected synchronized void unsetAdminDataSource ( AdministrativeDataSource ds ) {
        if ( this.adminDataSource == ds ) {
            this.adminDataSource = null;
        }
    }


    @Reference
    protected synchronized void setAdminDsUtil ( AdministrativeDataSourceUtil util ) {
        this.dsUtil = util;
    }


    protected synchronized void unsetAdminDsUtil ( AdministrativeDataSourceUtil util ) {
        if ( this.dsUtil == util ) {
            this.dsUtil = null;
        }
    }


    @Reference
    protected synchronized void setChangeFileProvider ( ChangeFileProvider provider ) {
        this.changeFileProvider = provider;
    }


    protected synchronized void unsetChangeFileProvider ( ChangeFileProvider provider ) {
        if ( this.changeFileProvider == provider ) {
            this.changeFileProvider = null;
        }
    }


    @Reference
    protected synchronized void bindDatabaseFactory ( LiquibaseDatabaseFactory factory ) {
        this.databaseFactory = factory;
    }


    protected synchronized void unbindDatabaseFactory ( LiquibaseDatabaseFactory factory ) {

    }


    @Reference
    protected synchronized void setChangeLogFactory ( LiquibaseChangeLogFactory logFactory ) {
        this.changeLogFactory = logFactory;
    }


    protected synchronized void unsetChangeLogFactory ( LiquibaseChangeLogFactory logFactory ) {
        if ( this.changeLogFactory == logFactory ) {
            this.changeLogFactory = null;
        }
    }


    // BEGIN: Dependencies only
    @Reference
    protected synchronized void setServiceLocator ( LiquibaseServiceLocator locator ) {
        // dependency only
    }


    protected synchronized void unsetServiceLocator ( LiquibaseServiceLocator locator ) {
        // dependency only
    }


    @Reference
    protected synchronized void setLiquibaseLogger ( liquibase.logging.Logger logger ) {
        // dependency only
    }


    protected synchronized void unsetLiquibaseLogger ( liquibase.logging.Logger logger ) {
        // dependency only
    }


    // END: Dependencies only

    @Activate
    protected void activate ( ComponentContext ctx ) throws SQLException, SchemaException {
        if ( log.isDebugEnabled() ) {
            log.debug("Starting schema manager for DataSource " + this.config.getDataSourceName()); //$NON-NLS-1$
        }

        String managementSchema = this.config.getManagementSchema();
        try {
            this.dsUtil.ensureSchemaExists(this.dsUtil.createMetadata().getDefaultCatalog(), managementSchema);
        }
        catch ( Exception e ) {
            log.warn("Failed to ensure schema exists", e); //$NON-NLS-1$
        }

        Database dbDriver = this.databaseFactory.buildDatabase(this.adminDataSource);
        dbDriver.setLiquibaseSchemaName(managementSchema);
        this.db = dbDriver;
        updateChangelog();
    }


    protected void updateChangelog () throws SchemaException {
        long time = System.currentTimeMillis();
        SortedMap<URL, SchemaRegistration> changeFiles = this.changeFileProvider.getChangeFiles(this.config.getDataSourceName(), true);
        if ( log.isDebugEnabled() ) {
            log.debug("Found change files " + changeFiles.size()); //$NON-NLS-1$
        }
        ChangeLogParameters params = new ChangeLogParameters(this.db);
        DatabaseChangeLog newChangelog = this.changeLogFactory.parseChangeLogs(changeFiles, params);

        synchronized ( this.lock ) {
            this.changeLogLoaded = time;
            this.changeLog = newChangelog;
            this.emptyChangeLog = changeFiles.isEmpty();
        }
    }


    /**
     * @return the db
     */
    public Database getDatabase () {
        return this.db;
    }


    /**
     * @return the database changelog
     */
    public DatabaseChangeLog getChangeLog () {
        return this.changeLog;
    }


    /**
     * 
     */
    public void resetState () {
        this.upToDate = false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaManager#ensureUpToDate()
     */
    @Override
    public synchronized void ensureUpToDate () throws SchemaException {
        if ( !this.upToDate ) {
            try {
                if ( this.emptyChangeLog ) {
                    return;
                }
                long start = System.currentTimeMillis();
                if ( log.isDebugEnabled() ) {
                    log.debug("Checking for database changes " + this.config.getDataSourceName()); //$NON-NLS-1$
                }
                ChangeLogIterator unappliedChanges = getUnappliedChanges();

                if ( log.isDebugEnabled() ) {
                    log.debug("Determined changes"); //$NON-NLS-1$

                }

                unappliedChanges.run(new UpdateVisitor(this.db, new LoggingChangeExecListener()), this.getEnvironment());
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Updated schema in %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
                }
                this.changeFileProvider.trackApplied(this.config.getDataSourceName(), this.changeLogLoaded);
                this.upToDate = true;
            }
            catch (
                SchemaException |
                LiquibaseException e ) {
                log.error("Failed to update schema", e); //$NON-NLS-1$
                throw new SchemaException("Failed to update schema:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.SchemaManager#setChangeLogApplied()
     */
    @Override
    public synchronized void setChangeLogApplied () throws SchemaException {
        try {
            long start = System.currentTimeMillis();
            this.getUnappliedChanges().run(new ChangeLogSyncVisitor(this.db), this.getEnvironment());
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Synced schema in %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
            }
        }
        catch (
            SchemaException |
            LiquibaseException e ) {
            throw new SchemaException("Failed to sync schema:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the used environment
     */
    public RuntimeEnvironment getEnvironment () {
        return new RuntimeEnvironment(this.db, this.config.getContexts(), this.config.getLabelExpression());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaManager#getContexts()
     */
    @Override
    public Set<String> getContexts () {
        return this.config.getContexts().getContexts();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaManager#getUnappliedChanges()
     */
    @Override
    public ChangeLogIterator getUnappliedChanges () throws SchemaException {
        try {
            synchronized ( this.lock ) {
                this.checkLiquibaseTables(true, this.changeLog, this.config.getContexts());

                try {
                    this.dsUtil.ensureSchemaExists(this.db.getDefaultCatalogName(), this.db.getDefaultSchemaName());
                }
                catch ( DatabaseException e ) {
                    throw new SchemaException("Failed to ensure schema exists " + this.db.getDefaultSchemaName(), e); //$NON-NLS-1$
                }

                this.changeLog.validate(this.db, this.config.getContexts(), this.config.getLabelExpression());
                return new ChangeLogIterator(
                    this.changeLog,
                    new ShouldRunChangeSetFilter(this.db),
                    new ContextChangeSetFilter(this.config.getContexts()),
                    new DbmsChangeSetFilter(this.db));
            }

        }
        catch ( LiquibaseException e ) {
            throw new SchemaException("Failed to determine changes that need to be applied:", e); //$NON-NLS-1$
        }
    }


    private void checkLiquibaseTables ( boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts )
            throws LiquibaseException {
        try {
            String defaultCatalog = this.dsUtil.createMetadata().getDefaultCatalog();
            String managementSchema = this.config.getManagementSchema();
            this.dsUtil.ensureSchemaExists(defaultCatalog, managementSchema);
        }
        catch ( DatabaseException e ) {
            throw new LiquibaseException("Failed to ensure that the management schema exists", e); //$NON-NLS-1$
        }
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this.db);
        changeLogHistoryService.init();
        if ( updateExistingNullChecksums ) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, this.config.getLabelExpression());
        }
        LockServiceFactory.getInstance().getLockService(this.db).init();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaManager#getAllChanges()
     */
    @Override
    public ChangeLogIterator getAllChanges () {
        synchronized ( this.lock ) {
            return new ChangeLogIterator(this.changeLog, new ContextChangeSetFilter(this.config.getContexts()));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.ChangeFileListener#changeFilesModified()
     */
    @Override
    public void changeFilesModified () {
        try {
            this.updateChangelog();
        }
        catch ( SchemaException e ) {
            log.error("Failed to update database changelog:", e); //$NON-NLS-1$
        }
        this.upToDate = false;
    }
}

/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.autoapply.internal;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationListener;
import eu.agno3.runtime.db.schema.diff.SchemaDiffException;
import eu.agno3.runtime.db.schema.diff.SchemaDiffService;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateSnapshotGenerator;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;
import eu.agno3.runtime.util.log.LogOutputStream;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.snapshot.DatabaseSnapshot;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SchemaAutoApplicator.class, HibernateConfigurationListener.class
}, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = SchemaAutoApplicator.PID )
public class SchemaAutoApplicator implements HibernateConfigurationListener {

    /**
     * 
     */
    public static final String PID = "schema.autoapply"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SchemaAutoApplicator.class);

    private ComponentContext context;
    private HibernateSnapshotGenerator hibernateSnapshotGenerator;
    private HibernateOwnershipStrategyFactory ownershipStrategy;
    private SchemaDiffService schemaDiffService;
    private LiquibaseDatabaseFactory databaseFactory;

    private boolean active;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx;
        String actProp = (String) ctx.getProperties().get("active"); //$NON-NLS-1$
        this.active = actProp != null && Boolean.parseBoolean(actProp);
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    @Reference
    protected synchronized void setHibernateSnapshotGenerator ( HibernateSnapshotGenerator snapshotGen ) {
        this.hibernateSnapshotGenerator = snapshotGen;
    }


    protected synchronized void unsetHibernateSnapshotGenerator ( HibernateSnapshotGenerator snapshotGen ) {
        if ( this.hibernateSnapshotGenerator == snapshotGen ) {
            this.hibernateSnapshotGenerator = null;
        }
    }


    @Reference
    protected synchronized void setDiffService ( SchemaDiffService diffService ) {
        this.schemaDiffService = diffService;
    }


    protected synchronized void unsetDiffService ( SchemaDiffService diffService ) {
        if ( this.schemaDiffService == diffService ) {
            this.schemaDiffService = null;
        }
    }


    @Reference
    protected synchronized void setDatabaseFactory ( LiquibaseDatabaseFactory dbf ) {
        this.databaseFactory = dbf;
    }


    protected synchronized void unsetDatabaseFactory ( LiquibaseDatabaseFactory dbf ) {
        if ( this.databaseFactory == dbf ) {
            this.databaseFactory = null;
        }
    }


    @Reference
    protected synchronized void setOwnershipStrategy ( HibernateOwnershipStrategyFactory osf ) {
        this.ownershipStrategy = osf;
    }


    protected synchronized void unsetOwnershipStrategy ( HibernateOwnershipStrategyFactory osf ) {
        if ( this.ownershipStrategy == osf ) {
            this.ownershipStrategy = null;
        }
    }


    /**
     * @return the schemaDiffService
     */
    protected SchemaDiffService getSchemaDiffService () {
        return this.schemaDiffService;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationListener#generatedConfiguation(eu.agno3.runtime.db.orm.PersistenceUnitDescriptor,
     *      org.hibernate.boot.Metadata)
     */
    @Override
    public void generatedConfiguation ( PersistenceUnitDescriptor puDesc, Metadata cfg ) {
        if ( !this.active ) {
            log.debug("Not active"); //$NON-NLS-1$
            return;
        }
        log.debug("New schema configuration, apply changes..."); //$NON-NLS-1$
        Contexts contexts = new Contexts();
        LabelExpression label = new LabelExpression();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("PersistenceUnit: %s DataSource: %s", puDesc.getPersistenceUnitName(), puDesc.getDataSourceName())); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("PU descriptor " + puDesc.getClass().getName()); //$NON-NLS-1$
        }

        if ( !puDesc.isAutoApply() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Persistence unit %s is excluded from schema auto-update", puDesc.getPersistenceUnitName())); //$NON-NLS-1$
            }
            return;
        }

        try {
            DatabaseSnapshot snapshot = makeHibernateSnapshot(cfg);
            diffAndApply(contexts, label, puDesc.getDataSourceName(), snapshot);
        }
        catch ( HibernateIndexingException e ) {
            log.error("Failed to create schema snapshot:", e); //$NON-NLS-1$
            return;
        }
    }


    /**
     * @param cfg
     * @return
     * @throws HibernateIndexingException
     */
    protected DatabaseSnapshot makeHibernateSnapshot ( Metadata cfg ) throws HibernateIndexingException {
        return this.hibernateSnapshotGenerator.snapshot(cfg, this.ownershipStrategy);
    }


    /**
     * @param contexts
     * @param label
     * @param dataSourceName
     * @param snapshot
     */
    private void diffAndApply ( Contexts contexts, LabelExpression label, String dataSourceName, DatabaseSnapshot snapshot ) {
        Database db;
        AdministrativeDataSource ds;
        try {
            ds = getAdminDataSource(dataSourceName);
            db = buildDatabase(ds);
        }
        catch (
            InvalidSyntaxException |
            SQLException e ) {
            log.error("Failed to obtain administrative datasource:", e); //$NON-NLS-1$
            return;
        }

        DiffToChangeLog dtc = buildChangelog(snapshot, ds);

        if ( dtc == null ) {
            return;
        }

        applyChangelog(contexts, label, db, dtc);
    }


    /**
     * @param ds
     * @return
     * @throws SQLException
     */
    protected Database buildDatabase ( AdministrativeDataSource ds ) throws SQLException {
        return this.databaseFactory.buildDatabase(ds);
    }


    /**
     * @param contexts
     * @param db
     * @param dtc
     */
    private static void applyChangelog ( Contexts contexts, LabelExpression label, Database db, DiffToChangeLog dtc ) {
        try {
            List<ChangeSet> changes = dtc.generateChangeSets();
            if ( !changes.isEmpty() ) {
                debugChangelog(dtc, changes);
                applyChangeLog(contexts, label, db, changes);
                log.debug("Applied schema changes successfully"); //$NON-NLS-1$
            }
        }
        catch ( LiquibaseException e ) {
            log.error("Failed to apply database changes:", e); //$NON-NLS-1$
        }
        finally {

            try {
                db.close();
            }
            catch ( DatabaseException e ) {
                log.warn("Failed to close database:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param snapshot
     * @param ds
     * @return
     */
    protected DiffToChangeLog buildChangelog ( DatabaseSnapshot snapshot, AdministrativeDataSource ds ) {
        DiffToChangeLog dtc;
        try {
            DiffResult diff = this.schemaDiffService.diff(ds, snapshot);
            diff = new NoRemoveDiffWrapper(diff);
            DiffOutputControl control = new DiffOutputControl(true, true, true, diff.getCompareControl().getSchemaComparisons());
            dtc = new DiffToChangeLog(diff, control);

        }
        catch ( SchemaDiffException e ) {
            log.error("Failed to generate database changes:", e); //$NON-NLS-1$
            return null;
        }
        return dtc;
    }


    /**
     * @param dtc
     * @param changes
     */
    private static void debugChangelog ( DiffToChangeLog dtc, List<ChangeSet> changes ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Changelog to apply: (%d changes)", changes.size())); //$NON-NLS-1$

            try {
                dtc.print(LogOutputStream.makePrintStream(log, Level.DEBUG));
            }
            catch (
                ParserConfigurationException |
                IOException |
                LiquibaseException e ) {
                log.warn("Failed to generate changeLog output", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param contexts
     * @param db
     * @param changes
     * @throws LiquibaseException
     */
    private static void applyChangeLog ( Contexts contexts, LabelExpression label, Database db, List<ChangeSet> changes ) throws LiquibaseException {
        log.info("Applying database schema changes... "); //$NON-NLS-1$
        DatabaseChangeLog changeLog = new DatabaseChangeLog();

        String filename = String.format("autoupdate-%d.log", System.currentTimeMillis()); //$NON-NLS-1$
        changeLog.setPhysicalFilePath(filename);
        changeLog.setLogicalFilePath(filename);

        for ( ChangeSet change : changes ) {
            changeLog.addChangeSet(new DummyChangeSetDecorator(change, filename));
        }

        checkLiquibaseTables(true, changeLog, contexts, label, db);

        changeLog.validate(db, contexts, label);

        ChangeLogIterator updatesIterator = new ChangeLogIterator(
            changeLog,
            new ShouldRunChangeSetFilter(db),
            new ContextChangeSetFilter(contexts),
            new DbmsChangeSetFilter(db));

        updatesIterator.run(new UpdateVisitor(db, null), new RuntimeEnvironment(db, contexts, label));
    }


    private static void checkLiquibaseTables ( boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts,
            LabelExpression label, Database db ) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(db);
        changeLogHistoryService.init();
        if ( updateExistingNullChecksums ) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, label);
        }
        LockServiceFactory.getInstance().getLockService(db).init();
    }


    /**
     * @param pu
     * @throws InvalidSyntaxException
     * @throws eu.agno3.runtime.db.DatabaseException
     */
    protected AdministrativeDataSource getAdminDataSource ( String dataSourceName )
            throws InvalidSyntaxException, eu.agno3.runtime.db.DatabaseException {

        if ( log.isDebugEnabled() ) {
            log.debug("Resolved datasource name " + dataSourceName); //$NON-NLS-1$
        }

        FilterExpression dsFilter = FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSourceName);

        Collection<ServiceReference<AdministrativeDataSource>> dsRefs = this.context.getBundleContext()
                .getServiceReferences(AdministrativeDataSource.class, dsFilter.toString());

        if ( dsRefs.isEmpty() ) {
            throw new eu.agno3.runtime.db.DatabaseException("Failed to lookup administrative datasource for " + dataSourceName); //$NON-NLS-1$

        }

        return this.context.getBundleContext().getService(dsRefs.iterator().next());
    }

}

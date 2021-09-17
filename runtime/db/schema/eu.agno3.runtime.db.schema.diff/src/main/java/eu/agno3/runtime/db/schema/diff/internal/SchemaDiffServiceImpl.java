/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff.internal;


import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.derby.jdbc.BasicEmbeddedDataSource40;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.derby.server.DerbyConfigProperties;
import eu.agno3.runtime.db.schema.ChangeFileProvider;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.SchemaRegistration;
import eu.agno3.runtime.db.schema.diff.DiffPostProcessor;
import eu.agno3.runtime.db.schema.diff.DiffPostProcessorComparator;
import eu.agno3.runtime.db.schema.diff.SchemaDiffException;
import eu.agno3.runtime.db.schema.diff.SchemaDiffService;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseChangeLogFactory;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.CompareControl.SchemaComparison;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SchemaDiffService.class
} )
public class SchemaDiffServiceImpl implements SchemaDiffService {

    private static final String FAILED_TO_CLOSE_DATABASE = "Failed to close database:"; //$NON-NLS-1$
    private static final String FAILED_TO_DIFF_DATABASE = "Failed to diff database"; //$NON-NLS-1$

    @SuppressWarnings ( "unchecked" )
    private static final Class<DatabaseObject>[] OBJECT_TYPES = new Class[] {
        Catalog.class, Schema.class, Table.class, View.class, Sequence.class, Column.class, PrimaryKey.class, Index.class, ForeignKey.class
    };

    private static final Logger log = Logger.getLogger(SchemaDiffServiceImpl.class);
    private LiquibaseDatabaseFactory dbFactory;
    private SortedSet<DiffPostProcessor> postProcessors = new TreeSet<>(new DiffPostProcessorComparator());
    private DataSourceFactory derbyEmbeddedDsFactory;
    private ChangeFileProvider changeFileProvider;
    private LiquibaseChangeLogFactory changeLogFactory;


    @Reference
    protected synchronized void setDatabaseFactory ( LiquibaseDatabaseFactory dbf ) {
        this.dbFactory = dbf;
    }


    protected synchronized void unsetDatabaseFactory ( LiquibaseDatabaseFactory dbf ) {
        if ( this.dbFactory == dbf ) {
            this.dbFactory = null;
        }
    }


    @Reference
    protected synchronized void setChangeFileProvider ( ChangeFileProvider cfp ) {
        this.changeFileProvider = cfp;
    }


    protected synchronized void unsetChangeFileProvider ( ChangeFileProvider cfp ) {
        if ( this.changeFileProvider == cfp ) {
            this.changeFileProvider = null;
        }
    }


    @Reference
    protected synchronized void setChangeLogFactory ( LiquibaseChangeLogFactory clf ) {
        this.changeLogFactory = clf;
    }


    protected synchronized void unsetChangeLogFactory ( LiquibaseChangeLogFactory clf ) {
        if ( this.changeLogFactory == clf ) {
            this.changeLogFactory = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindPostProcessor ( DiffPostProcessor proc ) {
        this.postProcessors.add(proc);
    }


    protected synchronized void unbindPostProcessor ( DiffPostProcessor proc ) {
        this.postProcessors.remove(proc);
    }


    @Reference ( target = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.EmbeddedDriver)" )
    protected synchronized void setDerbyEmbeddedDataSourceFactory ( DataSourceFactory dsf ) {
        this.derbyEmbeddedDsFactory = dsf;
    }


    protected synchronized void unsetDerbyEmbeddedDataSourceFactory ( DataSourceFactory dsf ) {
        if ( this.derbyEmbeddedDsFactory == dsf ) {
            this.derbyEmbeddedDsFactory = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.diff.SchemaDiffService#diff(javax.sql.DataSource, liquibase.database.Database,
     *      java.util.Set)
     */
    @Override
    public DiffResult diff ( DataSource reference, Database target, Set<DatabaseObject> roots ) throws SchemaDiffException {
        Database referenceDb = openReferenceDatabase(reference);

        try {
            DatabaseObject[] objs;
            if ( roots == null ) {
                // this matches only the default schema
                // TODO: may be better to enumerate the schemas
                objs = new DatabaseObject[] {
                    new Schema((String) null, null)
                };
            }
            else {
                objs = roots.toArray(new DatabaseObject[] {});
            }
            DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(objs, referenceDb, new SnapshotControl(referenceDb));
            DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(objs, target, new SnapshotControl(target));

            return this.diff(referenceSnapshot, targetSnapshot);
        }
        catch (
            DatabaseException |
            InvalidExampleException e ) {
            throw new SchemaDiffException(FAILED_TO_DIFF_DATABASE, e);
        }
        finally {
            closeReferenceDatabase(referenceDb);
        }

    }


    /**
     * @param referenceDb
     * @throws SchemaDiffException
     */
    private static void closeReferenceDatabase ( Database referenceDb ) throws SchemaDiffException {
        try {
            referenceDb.close();
        }
        catch ( DatabaseException e ) {
            log.warn("Failed to close reference database:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param reference
     * @return
     * @throws SchemaDiffException
     */
    private Database openReferenceDatabase ( DataSource reference ) throws SchemaDiffException {
        Database referenceDb;
        try {
            referenceDb = this.dbFactory.buildDatabase(reference);
        }
        catch ( SQLException e ) {
            throw new SchemaDiffException("Failed to open reference database:", e); //$NON-NLS-1$
        }
        return referenceDb;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.SchemaDiffService#diff(javax.sql.DataSource,
     *      liquibase.snapshot.DatabaseSnapshot)
     */
    @Override
    public DiffResult diff ( DataSource reference, DatabaseSnapshot target ) throws SchemaDiffException {
        log.debug("Running database diff"); //$NON-NLS-1$
        Database referenceDb = this.openReferenceDatabase(reference);

        try {
            Set<DatabaseObject> dbo = makeSnapshotTargets(target);

            DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(dbo.toArray(new DatabaseObject[] {}), referenceDb, new SnapshotControl(referenceDb));

            DiffResult r = this.diff(referenceSnapshot, target);
            log.debug("Database diff finished"); //$NON-NLS-1$

            return r;
        }
        catch (
            DatabaseException |
            InvalidExampleException e ) {
            throw new SchemaDiffException(FAILED_TO_DIFF_DATABASE, e);
        }
        finally {
            closeReferenceDatabase(referenceDb);
        }
    }


    /**
     * @param target
     * @return
     */
    private static Set<DatabaseObject> makeSnapshotTargets ( DatabaseSnapshot target ) {
        Set<DatabaseObject> dbo = new HashSet<>();
        dbo.addAll(target.get(Catalog.class));
        dbo.addAll(target.get(Schema.class));
        return dbo;
    }


    private DiffResult diff ( DatabaseSnapshot reference, DatabaseSnapshot target ) throws DatabaseException {

        List<SchemaComparison> comparisons = new ArrayList<>();
        addSchemas(reference, comparisons);
        addSchemas(target, comparisons);

        CompareControl control = new CompareControl(
            comparisons.toArray(new SchemaComparison[comparisons.size()]),
            (Set<Class<? extends DatabaseObject>>) null);

        if ( log.isTraceEnabled() ) {
            log.trace("Reference snapshot: " + reference.getDatabase().getClass().getName()); //$NON-NLS-1$
            dumpSnapshot(reference);

            log.trace("Target snapshot:" + target.getDatabase().getClass().getName()); //$NON-NLS-1$
            dumpSnapshot(target);
        }

        DiffResult r = DiffGeneratorFactory.getInstance().compare(target, reference, control);

        for ( DiffPostProcessor p : this.postProcessors ) {
            r = p.process(r);
        }

        return r;
    }


    /**
     * @param snapshot
     * @param comp
     */
    void addSchemas ( DatabaseSnapshot snapshot, List<SchemaComparison> comp ) {
        CatalogAndSchema liquibaseSchema = new CatalogAndSchema(
            snapshot.getDatabase().getLiquibaseCatalogName(),
            snapshot.getDatabase().getLiquibaseSchemaName()).standardize(snapshot.getDatabase());
        Set<Schema> schemas = snapshot.get(Schema.class);
        for ( Schema schema : schemas ) {
            CatalogAndSchema cs = mapSchema(snapshot, schema);
            if ( liquibaseSchema.equals(cs, snapshot.getDatabase()) ) {
                continue;
            }
            log.debug(cs);

            boolean found = false;
            for ( SchemaComparison schemaComparison : comp ) {
                if ( schemaComparison.getReferenceSchema().equals(cs, snapshot.getDatabase()) ) {
                    found = true;
                    break;
                }
            }

            if ( !found ) {
                comp.add(new SchemaComparison(cs, cs));
            }
        }
    }


    /**
     * @param snapshot
     * @param schema
     * @return
     */
    CatalogAndSchema mapSchema ( DatabaseSnapshot snapshot, Schema schema ) {
        return new CatalogAndSchema(schema.getCatalogName(), schema.getName()).standardize(snapshot.getDatabase());
    }


    /**
     * @param reference
     */
    private static void dumpSnapshot ( DatabaseSnapshot reference ) {
        for ( Class<DatabaseObject> type : OBJECT_TYPES ) {
            dumpObjects(reference, type);
        }

    }


    /**
     * @param reference
     * @param type
     */
    private static void dumpObjects ( DatabaseSnapshot reference, Class<DatabaseObject> type ) {
        for ( DatabaseObject obj : reference.get(type) ) {
            dumpDatabaseObject(obj);
        }
    }


    /**
     * @param obj
     */
    private static void dumpDatabaseObject ( DatabaseObject obj ) {
        log.trace(String.format(" %s - %s", obj.getClass().getName(), obj.getName())); //$NON-NLS-1$
        if ( obj.getAttributes() != null ) {
            dumpObjectAttributes(obj);
        }
    }


    /**
     * @param obj
     */
    private static void dumpObjectAttributes ( DatabaseObject obj ) {
        for ( String attr : obj.getAttributes() ) {
            try {
                log.trace(String.format("  %s: %s", attr, obj.getAttribute(attr, Object.class))); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.trace("Exception getting attribute:", e); //$NON-NLS-1$
                log.warn(String.format("  %s: ERROR", attr)); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.SchemaDiffService#diffToCurrentChangeSet(java.lang.String,
     *      liquibase.snapshot.DatabaseSnapshot)
     */
    @Override
    public DiffResult diffToCurrentChangeSet ( String dsName, Database target, Set<DatabaseObject> roots ) throws SchemaDiffException {
        Database embedDb = null;
        String tempDbName = getEmbeddedDatabaseName(dsName);
        try {
            DataSource ds = this.setupEmbeddedDatabase(tempDbName);
            embedDb = this.dbFactory.buildDatabase(ds);
            embedDb.dropDatabaseObjects(new CatalogAndSchema(null, null));
            applyChangelog(dsName, ds);
            return this.diff(ds, target, roots);
        }
        catch (
            SQLException |
            SchemaException |
            LiquibaseException e ) {
            throw new SchemaDiffException(FAILED_TO_DIFF_DATABASE, e);
        }
        finally {
            if ( embedDb != null ) {
                try {
                    embedDb.close();
                }
                catch ( DatabaseException e ) {
                    log.warn(FAILED_TO_CLOSE_DATABASE, e);
                }
            }
            this.tearDownEmbeddedDatabase(dsName, tempDbName);
        }
    }


    /**
     * @param dsName
     * @return
     */
    protected String getEmbeddedDatabaseName ( String dsName ) {
        byte[] random = new byte[8];
        new Random().nextBytes(random);
        return dsName + "_diff_" + Hex.encodeHexString(random); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.SchemaDiffService#diffToCurrentChangeSet(java.lang.String,
     *      liquibase.snapshot.DatabaseSnapshot)
     */
    @Override
    public DiffResult diffToCurrentChangeSet ( String dsName, DatabaseSnapshot target ) throws SchemaDiffException {
        Database embedDb = null;
        Database referenceDb = null;

        String tempDbName = getEmbeddedDatabaseName(dsName);
        try {
            DataSource ds = this.setupEmbeddedDatabase(tempDbName);
            embedDb = this.dbFactory.buildDatabase(ds);
            embedDb.dropDatabaseObjects(new CatalogAndSchema(null, null));

            applyChangelog(dsName, ds);
            referenceDb = this.dbFactory.buildDatabase(ds);

            Set<DatabaseObject> dbo = makeSnapshotTargets(target);

            DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(dbo.toArray(new DatabaseObject[] {}), referenceDb, new SnapshotControl(referenceDb));

            return this.diff(referenceSnapshot, target);
        }
        catch (
            SQLException |
            SchemaException |
            LiquibaseException e ) {
            throw new SchemaDiffException(FAILED_TO_DIFF_DATABASE, e);
        }
        finally {
            if ( embedDb != null ) {
                try {
                    embedDb.close();
                }
                catch ( DatabaseException e ) {
                    log.warn(FAILED_TO_CLOSE_DATABASE, e);
                }
            }
            if ( referenceDb != null ) {
                try {
                    referenceDb.close();
                }
                catch ( DatabaseException e ) {
                    log.warn(FAILED_TO_CLOSE_DATABASE, e);
                }
            }
            this.tearDownEmbeddedDatabase(dsName, tempDbName);
        }
    }


    /**
     * @param dsName
     * @param ds
     * @throws SQLException
     * @throws SchemaException
     * @throws DatabaseException
     * @throws LiquibaseException
     */
    protected void applyChangelog ( String dsName, DataSource ds ) throws SQLException, SchemaException, LiquibaseException {
        Contexts contexts = new Contexts();
        LabelExpression label = new LabelExpression();

        Database db = this.dbFactory.buildDatabase(ds);
        SortedMap<URL, SchemaRegistration> changeFiles = this.changeFileProvider.getChangeFiles(dsName, false);
        ChangeLogParameters params = new ChangeLogParameters(db);
        DatabaseChangeLog changeLog = this.changeLogFactory.parseChangeLogs(changeFiles, params);
        checkLiquibaseTables(true, changeLog, contexts, label, db);
        changeLog.validate(db, contexts, label);

        ChangeLogIterator it = new ChangeLogIterator(
            changeLog,
            new ShouldRunChangeSetFilter(db),
            new ContextChangeSetFilter(contexts),
            new DbmsChangeSetFilter(db));

        it.run(new UpdateVisitor(db, null), new RuntimeEnvironment(db, contexts, label));
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
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private DataSource setupEmbeddedDatabase ( String dbName ) throws SQLException {
        BasicEmbeddedDataSource40 ds = this.getEmbeddedDataSource(dbName);
        ds.setCreateDatabase("create"); //$NON-NLS-1$
        log.debug("Trying to create embedded derby database"); //$NON-NLS-1$
        ds.getConnection();
        return ds;
    }


    /**
     * @return
     * @throws SQLException
     */
    private BasicEmbeddedDataSource40 getEmbeddedDataSource ( String dbName ) throws SQLException {
        Properties dsProps = getEmbeddedDsProperties(dbName);
        return this.derbyEmbeddedDsFactory.createDataSource(dsProps).unwrap(BasicEmbeddedDataSource40.class);
    }


    /**
     * @return
     */
    private static Properties getEmbeddedDsProperties ( String dbName ) {
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, dbName);
        props.put(DataSourceFactory.JDBC_DATASOURCE_NAME, dbName);
        props.put(DataSourceFactory.JDBC_USER, "diff"); //$NON-NLS-1$
        return props;
    }


    /**
     * @param tempDbName
     * 
     */
    private void tearDownEmbeddedDatabase ( String dbName, String tempDbName ) {
        try {
            BasicEmbeddedDataSource40 ds = this.getEmbeddedDataSource(dbName);
            ds.setShutdownDatabase("true"); //$NON-NLS-1$
            ds.getConnection();
        }
        catch ( SQLException e ) {
            log.trace("Exception during shutdown, possibly expected:", e); //$NON-NLS-1$
        }
        finally {
            deleteTempDatabase(tempDbName);
        }
    }


    /**
     * @param tempDbName
     */
    void deleteTempDatabase ( String tempDbName ) {
        String dbBase = System.getProperty(DerbyConfigProperties.SYSTEM_HOME);
        if ( StringUtils.isBlank(dbBase) ) {
            log.warn("No database home set"); //$NON-NLS-1$
            return;
        }
        Path dbPath = Paths.get(dbBase).resolve(tempDbName);
        if ( !Files.exists(dbPath, LinkOption.NOFOLLOW_LINKS) ) {
            log.warn("Database to cleanup was not found " + dbPath); //$NON-NLS-1$
            return;
        }

        log.debug("Removing " + dbPath); //$NON-NLS-1$

        try {
            Files.walkFileTree(dbPath, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                    return FileVisitResult.CONTINUE;
                }


                @Override
                public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                    if ( attrs.isRegularFile() ) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }


                @Override
                public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                    throw exc;
                }


                @Override
                public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
        catch ( IOException e ) {
            log.error("Failed to remove temporary database " + dbPath, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.diff.SchemaDiffService#diff(javax.sql.DataSource, javax.sql.DataSource,
     *      java.util.Set)
     */
    @Override
    public DiffResult diff ( DataSource reference, DataSource target, Set<DatabaseObject> roots ) throws SchemaDiffException {
        Database db = null;
        try {
            db = this.dbFactory.buildDatabase(target);
            return this.diff(reference, db, roots);
        }
        catch ( SQLException e ) {
            throw new SchemaDiffException(FAILED_TO_DIFF_DATABASE, e);
        }
        finally {
            if ( db != null ) {
                try {
                    db.close();
                }
                catch ( DatabaseException e ) {
                    log.warn(FAILED_TO_CLOSE_DATABASE, e);
                }
            }
        }
    }
}

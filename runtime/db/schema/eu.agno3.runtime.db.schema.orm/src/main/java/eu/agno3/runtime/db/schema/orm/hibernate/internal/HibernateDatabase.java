/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Namespace.Name;
import org.hibernate.dialect.Dialect;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory;
import eu.agno3.runtime.db.schema.orm.hibernate.database.HibernateConnection;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;


/**
 * @author mbechler
 * 
 */
public class HibernateDatabase extends AbstractJdbcDatabase {

    /**
     * 
     */
    private static final String HIBERNATE = "Hibernate"; //$NON-NLS-1$
    private Metadata config;
    private HibernateIndexer indexer;
    private Dialect dialect;


    /**
     * Create database instance based on hibernate mapping
     * 
     * @param config
     * @param ownershipStrategyFactory
     */
    public HibernateDatabase ( Metadata config, HibernateOwnershipStrategyFactory ownershipStrategyFactory ) {
        this.config = config;
        this.indexer = new HibernateIndexer(this, ownershipStrategyFactory);
        this.dialect = config.getDatabase().getDialect();
        Name def = config.getDatabase().getDefaultNamespace().getName();
        if ( def.getCatalog() != null ) {
            this.setDefaultCatalogName(def.getCatalog().getText());
        }
        if ( def.getSchema() != null ) {
            this.setDefaultSchemaName(def.getSchema().getText());
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#supportsSchemas()
     */
    @Override
    public boolean supportsSchemas () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#supportsCatalogs()
     */
    @Override
    public boolean supportsCatalogs () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#jdbcCallsCatalogsSchemas()
     */
    @Override
    public boolean jdbcCallsCatalogsSchemas () {
        return false;
    }


    /**
     * @return the config
     */
    public Metadata getConfiguration () {
        return this.config;
    }


    /**
     * @return the indexer
     */
    public HibernateIndexer getIndexer () {
        return this.indexer;
    }


    /**
     * 
     * @return the dialect in use by hibernate
     */
    public Dialect getDialect () {
        return this.dialect;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.servicelocator.PrioritizedService#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#close()
     */
    @Override
    public void close () {
        // nothing to do here
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#commit()
     */
    @Override
    public void commit () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#dataTypeIsNotModifiable(java.lang.String)
     */
    @Override
    public boolean dataTypeIsNotModifiable ( String arg0 ) {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#disableForeignKeyChecks()
     */
    @Override
    public boolean disableForeignKeyChecks () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#doesTagExist(java.lang.String)
     */
    @Override
    public boolean doesTagExist ( String arg0 ) {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#dropDatabaseObjects(liquibase.CatalogAndSchema)
     */
    @Override
    public void dropDatabaseObjects ( CatalogAndSchema arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#enableForeignKeyChecks()
     */
    @Override
    public void enableForeignKeyChecks () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeColumnName(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String escapeColumnName ( String arg0, String arg1, String arg2, String arg3 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeColumnNameList(java.lang.String)
     */
    @Override
    public String escapeColumnNameList ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeConstraintName(java.lang.String)
     */
    @Override
    public String escapeConstraintName ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeIndexName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String escapeIndexName ( String arg0, String arg1, String arg2 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeObjectName(java.lang.String, java.lang.Class)
     */
    @Override
    public String escapeObjectName ( String arg0, Class<? extends DatabaseObject> arg1 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeObjectName(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.Class)
     */
    @Override
    public String escapeObjectName ( String arg0, String arg1, String arg2, Class<? extends DatabaseObject> arg3 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeSequenceName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String escapeSequenceName ( String arg0, String arg1, String arg2 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeStringForDatabase(java.lang.String)
     */
    @Override
    public String escapeStringForDatabase ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeTableName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String escapeTableName ( String arg0, String arg1, String arg2 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#escapeViewName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String escapeViewName ( String arg0, String arg1, String arg2 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#execute(liquibase.statement.SqlStatement[], java.util.List)
     */
    @Override
    public void execute ( SqlStatement[] arg0, List<SqlVisitor> arg1 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#executeRollbackStatements(liquibase.change.Change, java.util.List)
     */
    @Override
    public void executeRollbackStatements ( Change arg0, List<SqlVisitor> arg1 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#executeStatements(liquibase.change.Change,
     *      liquibase.changelog.DatabaseChangeLog, java.util.List)
     */
    @Override
    public void executeStatements ( Change arg0, DatabaseChangeLog arg1, List<SqlVisitor> arg2 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#generateDatabaseFunctionValue(liquibase.statement.DatabaseFunction)
     */
    @Override
    public String generateDatabaseFunctionValue ( DatabaseFunction func ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#generatePrimaryKeyName(java.lang.String)
     */
    @Override
    public String generatePrimaryKeyName ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getAutoCommitMode()
     */
    @Override
    public boolean getAutoCommitMode () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getAutoIncrementClause(java.math.BigInteger, java.math.BigInteger)
     */
    @Override
    public String getAutoIncrementClause ( BigInteger arg0, BigInteger arg1 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getConcatSql(java.lang.String[])
     */
    @Override
    public String getConcatSql ( String... arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getConnection()
     */
    @Override
    public DatabaseConnection getConnection () {
        return new HibernateConnection(this.config);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getCurrentDateTimeFunction()
     */
    @Override
    public String getCurrentDateTimeFunction () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDataTypeMaxParameters(java.lang.String)
     */
    @Override
    public int getDataTypeMaxParameters ( String arg0 ) {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    @Override
    public String getDatabaseChangeLogLockTableName () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    @Override
    public String getDatabaseChangeLogTableName () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseMajorVersion()
     */
    @Override
    public int getDatabaseMajorVersion () {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseMinorVersion()
     */
    @Override
    public int getDatabaseMinorVersion () {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseProductName()
     */
    @Override
    public String getDatabaseProductName () {
        return HIBERNATE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDatabaseProductVersion()
     */
    @Override
    public String getDatabaseProductVersion () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDateFunctions()
     */
    @Override
    public List<DatabaseFunction> getDateFunctions () {
        return new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#isAutoCommit()
     */
    @Override
    public boolean isAutoCommit () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#isCaseSensitive()
     */
    @Override
    public boolean isCaseSensitive () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
     */
    @Override
    public String getDefaultDriver ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getDefaultPort()
     */
    @Override
    public Integer getDefaultPort () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#getShortName()
     */
    @Override
    public String getShortName () {
        return HIBERNATE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#isCorrectDatabaseImplementation(liquibase.database.DatabaseConnection)
     */
    @Override
    public boolean isCorrectDatabaseImplementation ( DatabaseConnection arg0 ) {
        return arg0 instanceof HibernateConnection;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#supportsInitiallyDeferrableColumns()
     */
    @Override
    public boolean supportsInitiallyDeferrableColumns () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.Database#supportsTablespaces()
     */
    @Override
    public boolean supportsTablespaces () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#getDefaultDatabaseProductName()
     */
    @Override
    protected String getDefaultDatabaseProductName () {
        return HIBERNATE;
    }

}

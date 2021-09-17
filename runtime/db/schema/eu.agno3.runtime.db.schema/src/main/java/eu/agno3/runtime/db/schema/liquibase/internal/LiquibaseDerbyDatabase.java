/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;


/**
 * @author mbechler
 * 
 */
@Component ( service = Database.class )
public class LiquibaseDerbyDatabase extends DerbyDatabase {

    private static final Logger log = Logger.getLogger(LiquibaseDerbyDatabase.class);

    private boolean closed = false;


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#toString()
     */
    @Override
    public String toString () {
        if ( this.closed ) {
            return this.getShortName() + " Database"; //$NON-NLS-1$
        }

        return super.toString();
    }


    /**
     * 
     */
    public LiquibaseDerbyDatabase () {
        this.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        this.setDefaultCatalogName("APP"); //$NON-NLS-1$
        this.setDatabaseChangeLogTableName("DATABASECHANGELOG"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#getLiquibaseSchemaName()
     */
    @Override
    public String getLiquibaseSchemaName () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#supportsDDLInTransaction()
     */
    @Override
    public boolean supportsDDLInTransaction () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#getDefaultSchemaName()
     */
    @Override
    public String getDefaultSchemaName () {
        return "APP"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#getLiquibaseCatalogName()
     */
    @Override
    public String getLiquibaseCatalogName () {
        return "APP_SCHEMA"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.core.DerbyDatabase#determineDriverVersion()
     */
    @Override
    protected void determineDriverVersion () {
        this.driverVersionMajor = 10;
        this.driverVersionMinor = 11;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMajorVersion()
     */
    @Override
    public int getDatabaseMajorVersion () throws DatabaseException {
        return 10;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMinorVersion()
     */
    @Override
    public int getDatabaseMinorVersion () throws DatabaseException {
        return 11;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#isCaseSensitive()
     */
    @Override
    public boolean isCaseSensitive () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.core.DerbyDatabase#correctObjectName(java.lang.String, java.lang.Class)
     */
    @Override
    public String correctObjectName ( String objectName, Class<? extends DatabaseObject> objectType ) {

        if ( Sequence.class.isAssignableFrom(objectType) ) {
            return objectName.toUpperCase();
        }

        return objectName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#escapeObjectName(java.lang.String, java.lang.Class)
     */
    @Override
    public String escapeObjectName ( String objectName, Class<? extends DatabaseObject> objectType ) {

        if ( Table.class.isAssignableFrom(objectType) ) {
            return String.format("\"%s\"", objectName); //$NON-NLS-1$
        }
        else if ( Schema.class.isAssignableFrom(objectType) ) {
            return objectName.toUpperCase();
        }

        return super.escapeObjectName(objectName, objectType);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.AbstractJdbcDatabase#getConnection()
     */
    @Override
    public DatabaseConnection getConnection () {
        log.trace("Returning connection"); //$NON-NLS-1$
        return super.getConnection();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.core.DerbyDatabase#close()
     */
    @Override
    public void close () throws DatabaseException {
        log.trace("Closing connection"); //$NON-NLS-1$
        this.closed = true;
        if ( super.getConnection() != null ) {
            super.getConnection().close();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.core.DerbyDatabase#supportsSequences()
     */
    @Override
    public boolean supportsSequences () {
        return true;
    }
}

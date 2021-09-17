/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.database;


import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
public class HibernateConnection implements DatabaseConnection {

    private Metadata config;


    /**
     * @param config
     */
    public HibernateConnection ( Metadata config ) {
        this.config = config;
    }


    /**
     * @return the hibernate mapping config
     */
    public Metadata getConfiguration () {
        return this.config;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#close()
     */
    @Override
    public void close () throws DatabaseException {
        // nothing to do here
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#commit()
     */
    @Override
    public void commit () throws DatabaseException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getAutoCommit()
     */
    @Override
    public boolean getAutoCommit () throws DatabaseException {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getCatalog()
     */
    @Override
    public String getCatalog () throws DatabaseException {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getConnectionUserName()
     */
    @Override
    public String getConnectionUserName () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getDatabaseMajorVersion()
     */
    @Override
    public int getDatabaseMajorVersion () throws DatabaseException {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getDatabaseMinorVersion()
     */
    @Override
    public int getDatabaseMinorVersion () throws DatabaseException {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getDatabaseProductName()
     */
    @Override
    public String getDatabaseProductName () throws DatabaseException {
        return "Hibernate"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getDatabaseProductVersion()
     */
    @Override
    public String getDatabaseProductVersion () throws DatabaseException {
        return "0.0"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#getURL()
     */
    @Override
    public String getURL () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#isClosed()
     */
    @Override
    public boolean isClosed () throws DatabaseException {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#nativeSQL(java.lang.String)
     */
    @Override
    public String nativeSQL ( String arg0 ) throws DatabaseException {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#rollback()
     */
    @Override
    public void rollback () throws DatabaseException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.DatabaseConnection#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit ( boolean arg0 ) throws DatabaseException {
        throw new UnsupportedOperationException();
    }


    @Override
    public void attached ( Database db ) {
        // ignore
    }

}

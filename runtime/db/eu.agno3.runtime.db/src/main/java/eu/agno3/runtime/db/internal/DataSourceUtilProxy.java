/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2014 by mbechler
 */
package eu.agno3.runtime.db.internal;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.DataSourceMetaData;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.db.DatabaseException;


/**
 * @author mbechler
 * 
 */
@Component ( service = DataSourceUtil.class, configurationPid = DataSourceUtilProxy.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DataSourceUtilProxy implements DataSourceUtil {

    /**
     * 
     */
    public static final String PID = "eu.agno3.runtime.db.DataSourceUtilFactory"; //$NON-NLS-1$

    private DatabaseDriverUtil driverUtil;
    private DataSource dataSource;


    @Reference
    protected synchronized void setDatabaseDriverUtil ( DatabaseDriverUtil util ) {
        this.driverUtil = util;
    }


    protected synchronized void unsetDatabaseDriverUtil ( DatabaseDriverUtil util ) {
        if ( this.driverUtil == util ) {
            this.driverUtil = null;
        }
    }


    @Reference
    protected synchronized void setDataSource ( DataSource ds ) {
        this.dataSource = ds;
    }


    protected synchronized void unsetDataSource ( DataSource ds ) {
        if ( this.dataSource == ds ) {
            this.dataSource = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#createMetadata()
     */
    @Override
    public DataSourceMetaData createMetadata () {
        return this.driverUtil.createMetaDataFor(this.dataSource);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#schemaExists(java.lang.String, java.lang.String)
     */
    @Override
    public boolean schemaExists ( String catalog, String schema ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            return this.driverUtil.schemaExists(conn, catalog, schema);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#ensureSchemaExists(java.lang.String, java.lang.String)
     */
    @Override
    public void ensureSchemaExists ( String catalog, String schema ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.ensureSchemaExists(conn, catalog, schema);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#createSchema(java.lang.String, java.lang.String)
     */
    @Override
    public void createSchema ( String catalog, String schema ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.createSchema(conn, catalog, schema);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#dropSchema(java.lang.String, java.lang.String)
     */
    @Override
    public void dropSchema ( String catalog, String schemaName ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.dropSchema(conn, catalog, schemaName);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#setConnectionDefaultSchema(java.sql.Connection, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setConnectionDefaultSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException {
        this.driverUtil.setConnectionDefaultSchema(conn, catalog, schemaName);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#clearSchema(java.lang.String, java.lang.String)
     */
    @Override
    public void clearSchema ( String catalog, String schema ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.clearSchema(conn, catalog, schema);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#clearDatabase(java.lang.String)
     */
    @Override
    public void clearDatabase ( String catalog ) throws DatabaseException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.clearDatabase(conn, catalog);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceUtil#lockDatabase()
     */
    @Override
    public boolean lockDatabase () throws DatabaseException {
        if ( !this.driverUtil.isLockSupported() ) {
            return false;
        }
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.lockDatabase(conn);
            return true;
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceUtil#unlockDatabase()
     */
    @Override
    public void unlockDatabase () throws DatabaseException {
        if ( !this.driverUtil.isLockSupported() ) {
            return;
        }
        try ( Connection conn = this.dataSource.getConnection() ) {
            this.driverUtil.unlockDatabase(conn);
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceUtil#quoteIdentifier(java.sql.Connection, java.lang.String)
     */
    @Override
    public String quoteIdentifier ( Connection conn, String schema ) throws DatabaseException {
        return this.driverUtil.quoteIdentifier(conn, schema);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceUtil#getValidationQuery()
     */
    @Override
    public String getValidationQuery () {
        return this.driverUtil.getValidationQuery();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SQLException
     *
     * @see eu.agno3.runtime.db.DataSourceUtil#setParameter(java.sql.PreparedStatement, int, java.util.UUID)
     */
    @Override
    public void setParameter ( PreparedStatement ps, int i, UUID uuid ) throws SQLException {
        this.driverUtil.setParameter(ps, i, uuid);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SQLException
     *
     * @see eu.agno3.runtime.db.DataSourceUtil#extractUUID(java.sql.ResultSet, int)
     */
    @Override
    public UUID extractUUID ( ResultSet rs, int i ) throws SQLException {
        return this.driverUtil.extractUUID(rs, i);
    }
}

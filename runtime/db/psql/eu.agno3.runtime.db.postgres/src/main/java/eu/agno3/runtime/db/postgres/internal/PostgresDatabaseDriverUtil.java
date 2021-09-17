/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 20, 2017 by mbechler
 */
package eu.agno3.runtime.db.postgres.internal;


import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.core.BaseConnection;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.xa.PGXAConnection;

import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.DataSourceMetaData;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.db.DatabaseException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    DatabaseDriverUtil.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver"
} )
public class PostgresDatabaseDriverUtil implements DatabaseDriverUtil {

    private static final Logger log = Logger.getLogger(PostgresDatabaseDriverUtil.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#createMetaDataFor(javax.sql.DataSource)
     */
    @Override
    public DataSourceMetaData createMetaDataFor ( DataSource ds ) {
        return new PostgresDataSourceMetaData(ds, getDefaultSchema(ds));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isUseXAWeakCompare()
     */
    @Override
    public boolean isUseXAWeakCompare () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isAlive(javax.transaction.xa.XAResource)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public boolean isAlive ( XAResource delegate ) {
        if ( ! ( delegate instanceof PGXAConnection ) ) {
            log.debug("Not a PGXAConnection"); //$NON-NLS-1$
            return true;
        }
        PGXAConnection conn = (PGXAConnection) delegate;
        try {
            Field connF = PGXAConnection.class.getDeclaredField("conn"); //$NON-NLS-1$
            connF.setAccessible(true);
            BaseConnection bc = (BaseConnection) connF.get(conn);
            if ( bc.isClosed() ) {
                log.debug("Refreshing closed connection"); //$NON-NLS-1$
                return false;
            }
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            SQLException e ) {
            log.warn("Failed to determine connection state", e); //$NON-NLS-1$
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#getDefaultSchema(javax.sql.DataSource)
     */
    @Override
    public String getDefaultSchema ( DataSource ds ) {
        CommonDataSource unwrapped = ds;

        if ( unwrapped instanceof AdministrativeDataSource ) {
            unwrapped = ( (AdministrativeDataSource) unwrapped ).getBacking();
        }

        if ( unwrapped instanceof ManagedDataSource ) {
            try {
                Field poolF = PoolingDataSource.class.getDeclaredField("_pool"); //$NON-NLS-1$
                poolF.setAccessible(true);

                @SuppressWarnings ( "resource" )
                GenericObjectPool<?> pool = (GenericObjectPool<?>) poolF.get(unwrapped);

                PooledObjectFactory<?> factory = pool.getFactory();

                Field connFactoryF = PoolableConnectionFactory.class.getDeclaredField("_connFactory"); //$NON-NLS-1$
                connFactoryF.setAccessible(true);
                Object connFactory = connFactoryF.get(factory);

                Field dataSourceF = DataSourceXAConnectionFactory.class.getDeclaredField("xaDataSource"); //$NON-NLS-1$
                dataSourceF.setAccessible(true);
                unwrapped = (CommonDataSource) dataSourceF.get(connFactory);
            }
            catch (
                NoSuchFieldException |
                SecurityException |
                IllegalArgumentException |
                IllegalAccessException e ) {
                log.error("Failed to determined wrapped data source default schema", e); //$NON-NLS-1$
            }

        }

        if ( unwrapped instanceof BaseDataSource ) {
            try {
                return ( (BaseDataSource) unwrapped ).getProperty("currentSchema"); //$NON-NLS-1$
            }
            catch ( SQLException e ) {
                log.error("Failed to retrieve default schema", e); //$NON-NLS-1$
            }
        }

        return "public"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#schemaExists(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public boolean schemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException {
        try ( PreparedStatement stmt = conn.prepareStatement("SELECT nspname FROM pg_catalog.pg_namespace WHERE nspname = ?;") ) { //$NON-NLS-1$
            stmt.setString(1, schema);
            try ( ResultSet r = stmt.executeQuery() ) {
                return r.next();
            }
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to check whether schema exists " + schema, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#ensureSchemaExists(java.sql.Connection, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void ensureSchemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException {
        if ( schemaExists(conn, catalog, schema) ) {
            return;
        }
        try ( PreparedStatement prepareStatement = conn.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + quoteIdentifier(conn, schema)) ) { //$NON-NLS-1$
            prepareStatement.executeUpdate();
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to ensure schema exists " + schema, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#createSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void createSchema ( Connection conn, String catalog, String schema ) throws DatabaseException {
        try ( PreparedStatement prepareStatement = conn.prepareStatement("CREATE SCHEMA " + quoteIdentifier(conn, schema)) ) { //$NON-NLS-1$
            prepareStatement.executeUpdate();
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to create schema " + schema, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#dropSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void dropSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException {
        try ( PreparedStatement prepareStatement = conn.prepareStatement("DROP SCHEMA " + quoteIdentifier(conn, schemaName)) ) { //$NON-NLS-1$
            prepareStatement.executeUpdate();
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to drop schema " + schemaName, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#setConnectionDefaultSchema(java.sql.Connection, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setConnectionDefaultSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#clearSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void clearSchema ( Connection conn, String catalog, String schema ) throws DatabaseException {

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#clearDatabase(java.sql.Connection, java.lang.String)
     */
    @Override
    public void clearDatabase ( Connection conn, String catalog ) throws DatabaseException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#quoteIdentifier(java.sql.Connection, java.lang.String)
     */
    @Override
    public String quoteIdentifier ( Connection conn, String identifier ) throws DatabaseException {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String quote = meta.getIdentifierQuoteString();
            if ( quote.length() > 0 && identifier.contains(quote) ) {
                throw new DatabaseException(String.format("Identifier may not contain '%s'", quote)); //$NON-NLS-1$
            }

            return quote + identifier + quote;
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isLockSupported()
     */
    @Override
    public boolean isLockSupported () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#lockDatabase(java.sql.Connection)
     */
    @Override
    public void lockDatabase ( Connection conn ) throws DatabaseException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#unlockDatabase(java.sql.Connection)
     */
    @Override
    public void unlockDatabase ( Connection conn ) throws DatabaseException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#getValidationQuery()
     */
    @Override
    public String getValidationQuery () {
        return "SELECT 1;"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#setParameter(java.sql.PreparedStatement, int, java.util.UUID)
     */
    @Override
    public void setParameter ( PreparedStatement ps, int i, UUID uuid ) throws SQLException {
        ps.setObject(i, uuid);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#extractUUID(java.sql.ResultSet, int)
     */
    @Override
    public UUID extractUUID ( ResultSet rs, int i ) throws SQLException {
        return rs.getObject(i, UUID.class);
    }
}

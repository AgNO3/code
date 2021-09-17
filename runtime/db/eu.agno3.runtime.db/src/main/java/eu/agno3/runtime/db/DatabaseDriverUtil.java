/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;


/**
 * Driver specific database utilities
 * 
 * @author mbechler
 * 
 */
public interface DatabaseDriverUtil {

    /**
     * 
     * @param ds
     * @return data source specific default schema
     */
    String getDefaultSchema ( DataSource ds );


    /**
     * @param ds
     * @return metadata for the specified datasource
     */
    DataSourceMetaData createMetaDataFor ( DataSource ds );


    /**
     * Check whether a schema exists
     * 
     * @param conn
     * @param catalog
     * @param schema
     * @return whether given schema exists
     * @throws DatabaseException
     */
    boolean schemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException;


    /**
     * Make sure a database schema exists
     * 
     * @param conn
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void ensureSchemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException;


    /**
     * Create a schema
     * 
     * @param conn
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void createSchema ( Connection conn, String catalog, String schema ) throws DatabaseException;


    /**
     * Drop a schema
     * 
     * Contained objects must be dropped first, you may use {@link #clearSchema(Connection, String, String)} for this.
     * 
     * @param conn
     * @param catalog
     * @param schemaName
     * @throws DatabaseException
     */
    void dropSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException;


    /**
     * Set the connection default schema
     * 
     * @param conn
     * @param catalog
     * @param schemaName
     * @throws DatabaseException
     */
    void setConnectionDefaultSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException;


    /**
     * Remove all objects from the given schema
     * 
     * @param conn
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void clearSchema ( Connection conn, String catalog, String schema ) throws DatabaseException;


    /**
     * Remove all objects from the database
     * 
     * @param conn
     * @param catalog
     * @throws DatabaseException
     */
    void clearDatabase ( Connection conn, String catalog ) throws DatabaseException;


    /**
     * Escapes a database identifier (e.g. schema, table name) for use in a query
     * 
     * @param conn
     * @param schema
     * @return the escaped identifier
     * @throws DatabaseException
     */
    String quoteIdentifier ( Connection conn, String schema ) throws DatabaseException;


    /**
     * @return whether locking the database is supported
     */
    boolean isLockSupported ();


    /**
     * @return whether XA resources should be weakly compared
     */
    boolean isUseXAWeakCompare ();


    /**
     * @param conn
     * @throws DatabaseException
     */
    void lockDatabase ( Connection conn ) throws DatabaseException;


    /**
     * @param conn
     * @throws DatabaseException
     */
    void unlockDatabase ( Connection conn ) throws DatabaseException;


    /**
     * @return the validation query
     */
    String getValidationQuery ();


    /**
     * @param ps
     * @param i
     * @param uuid
     * @throws SQLException
     */
    void setParameter ( PreparedStatement ps, int i, UUID uuid ) throws SQLException;


    /**
     * @param rs
     * @param i
     * @return uuid
     * @throws SQLException
     */
    UUID extractUUID ( ResultSet rs, int i ) throws SQLException;


    /**
     * @param delegate
     * @return whether the resource/associated connection is alive
     */
    boolean isAlive ( XAResource delegate );

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2014 by mbechler
 */
package eu.agno3.runtime.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public interface DataSourceUtil {

    /**
     * @return metadata for the datasource
     */
    DataSourceMetaData createMetadata ();


    /**
     * Check whether a schema exists
     * 
     * @param catalog
     * @param schema
     * @return whether given schema exists
     * @throws DatabaseException
     */
    boolean schemaExists ( String catalog, String schema ) throws DatabaseException;


    /**
     * Make sure a database schema exists
     * 
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void ensureSchemaExists ( String catalog, String schema ) throws DatabaseException;


    /**
     * Create a schema
     * 
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void createSchema ( String catalog, String schema ) throws DatabaseException;


    /**
     * Drop a schema
     * 
     * Contained objects must be dropped first, you may use {@link #clearSchema(String, String)} for this.
     * 
     * @param catalog
     * @param schemaName
     * @throws DatabaseException
     */
    void dropSchema ( String catalog, String schemaName ) throws DatabaseException;


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
     * @param catalog
     * @param schema
     * @throws DatabaseException
     */
    void clearSchema ( String catalog, String schema ) throws DatabaseException;


    /**
     * Remove all objects from the database
     * 
     * @param catalog
     * @throws DatabaseException
     */
    void clearDatabase ( String catalog ) throws DatabaseException;


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
     * 
     * @return whether locking is supported
     * @throws DatabaseException
     */
    boolean lockDatabase () throws DatabaseException;


    /**
     * 
     * @throws DatabaseException
     */
    void unlockDatabase () throws DatabaseException;


    /**
     * @return the validation query to use
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

}

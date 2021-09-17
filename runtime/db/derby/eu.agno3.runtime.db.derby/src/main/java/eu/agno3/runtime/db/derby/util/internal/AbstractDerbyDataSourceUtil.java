/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.util.internal;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.db.DatabaseException;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractDerbyDataSourceUtil implements DatabaseDriverUtil {

    /**
     * 
     */
    private static final String ID_WILDCARD = "%"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(DerbyEmbeddedDataSourceUtil.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#getDefaultSchema(javax.sql.DataSource)
     */
    @Override
    public String getDefaultSchema ( DataSource ds ) {
        return "APP"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isUseXAWeakCompare()
     */
    @Override
    public boolean isUseXAWeakCompare () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isAlive(javax.transaction.xa.XAResource)
     */
    @Override
    public boolean isAlive ( XAResource delegate ) {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#schemaExists(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public boolean schemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try ( ResultSet r = meta.getSchemas(catalog, schema) ) {

                if ( !r.next() ) {
                    return false;
                }

                return true;
            }
        }
        catch ( SQLException e ) {
            log.error("Failed to check schema existance:", e); //$NON-NLS-1$
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#ensureSchemaExists(java.sql.Connection, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void ensureSchemaExists ( Connection conn, String catalog, String schema ) throws DatabaseException {
        if ( !this.schemaExists(conn, catalog, schema) ) {
            this.createSchema(conn, catalog, schema);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#createSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void createSchema ( Connection conn, String catalog, String schema ) throws DatabaseException {
        try {
            try ( PreparedStatement s = conn.prepareStatement(String.format("CREATE SCHEMA %s", this.quoteIdentifier(conn, schema))) ) { //$NON-NLS-1$
                s.executeUpdate();
            }
        }
        catch ( SQLException e ) {
            log.error("Failed to create schema:", e); //$NON-NLS-1$
            throw new DatabaseException(e);
        }

    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#dropSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void dropSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException {
        try {
            try ( PreparedStatement s = conn.prepareStatement(String.format("DROP SCHEMA %s RESTRICT", this.quoteIdentifier(conn, schemaName))) ) { //$NON-NLS-1$
                s.executeUpdate();
            }
        }
        catch ( SQLException e ) {
            log.error("Failed to drop schema:", e); //$NON-NLS-1$
            throw new DatabaseException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#setConnectionDefaultSchema(java.sql.Connection, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setConnectionDefaultSchema ( Connection conn, String catalog, String schemaName ) throws DatabaseException {
        try {
            log.trace(String.format("Setting connection schema to %s:%s", catalog, schemaName)); //$NON-NLS-1$

            if ( !conn.isReadOnly() ) {
                this.ensureSchemaExists(conn, catalog, schemaName);
            }

            try ( PreparedStatement s = conn.prepareStatement(String.format("SET SCHEMA %s", this.quoteIdentifier(conn, schemaName))) ) { //$NON-NLS-1$
                s.executeUpdate();
            }
        }
        catch ( SQLException e ) {
            log.error("Failed to set connection schema:", e); //$NON-NLS-1$
            throw new DatabaseException(e);
        }
    }


    /**
     * 
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
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#clearSchema(java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public void clearSchema ( Connection conn, String catalog, String schema ) throws DatabaseException {

        try {
            DatabaseMetaData meta = conn.getMetaData();

            if ( !this.schemaExists(conn, catalog, schema) ) {
                log.debug("Schema does not exist"); //$NON-NLS-1$
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Clearing schema " + schema); //$NON-NLS-1$
            }

            // Tables
            try ( ResultSet r = meta.getTables(catalog, schema, ID_WILDCARD, null) ) {
                while ( r.next() ) {
                    String tableName = r.getString("TABLE_NAME"); //$NON-NLS-1$
                    if ( log.isDebugEnabled() ) {
                        log.debug("Dropping table " + tableName); //$NON-NLS-1$
                    }
                    dropTableSafe(conn, meta, catalog, schema, tableName);
                }
            }

            String sql = "select SEQUENCENAME from SYS.SYSSEQUENCES where SCHEMAID = (select SCHEMAID from SYS.SYSSCHEMAS where SCHEMANAME = ?)"; //$NON-NLS-1$
            try ( PreparedStatement sequenceStatement = conn.prepareStatement(sql) ) {
                sequenceStatement.setString(1, schema);
                try ( ResultSet r = sequenceStatement.executeQuery() ) {
                    while ( r.next() ) {
                        String sequenceName = r.getString("SEQUENCENAME"); //$NON-NLS-1$
                        if ( log.isDebugEnabled() ) {
                            log.debug("Dropping sequence " + sequenceName); //$NON-NLS-1$
                        }
                        String q = String.format("DROP SEQUENCE %s.%s RESTRICT", quoteIdentifier(conn, schema), quoteIdentifier(conn, sequenceName)); //$NON-NLS-1$
                        try ( PreparedStatement delete = conn.prepareStatement(q) ) {
                            if ( delete.executeUpdate() != 1 ) {
                                log.error("Failed to remove sequence " + q); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }

        }
        catch ( SQLException e ) {
            log.error("Failed to clear schema " + schema, e); //$NON-NLS-1$
            throw new DatabaseException("Failed to clear schema", e); //$NON-NLS-1$
        }

    }


    /**
     * @param conn
     * @param meta
     * @param catalog
     * @param schema
     * @param tableName
     * @throws SQLException
     */
    private void dropTableSafe ( Connection conn, DatabaseMetaData meta, String catalog, String schema, String tableName ) throws SQLException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Dropping table %s.%s", schema, tableName)); //$NON-NLS-1$
        }
        dropForeignKeys(conn, meta, catalog, schema, tableName);
        try ( PreparedStatement s = conn
                .prepareStatement(String.format("DROP TABLE %s.%s", this.quoteIdentifier(conn, schema), this.quoteIdentifier(conn, tableName))) ) { //$NON-NLS-1$
            s.executeUpdate();
        }
    }


    /**
     * @throws SQLException
     */
    private void dropForeignKeys ( Connection conn, DatabaseMetaData meta, String catalog, String schema, String tableName ) throws SQLException {
        try ( ResultSet fkr = meta.getExportedKeys(catalog, schema, tableName) ) {
            while ( fkr.next() ) {
                String foreignKeySchema = fkr.getString("FKTABLE_SCHEM"); //$NON-NLS-1$
                String foreignKeyTable = fkr.getString("FKTABLE_NAME"); //$NON-NLS-1$
                String foreignKeyName = fkr.getString("FK_NAME"); //$NON-NLS-1$

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "Found foreign key %s.%s:%s, dropping", //$NON-NLS-1$
                        foreignKeySchema,
                        foreignKeyTable,
                        foreignKeyName));
                }

                try ( PreparedStatement s = conn.prepareStatement(String.format(
                    "ALTER TABLE %s.%s DROP CONSTRAINT %s", //$NON-NLS-1$
                    this.quoteIdentifier(conn, foreignKeySchema),
                    this.quoteIdentifier(conn, foreignKeyTable),
                    this.quoteIdentifier(conn, foreignKeyName))) ) {
                    s.executeUpdate();
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#clearDatabase(java.sql.Connection, java.lang.String)
     */
    @Override
    public void clearDatabase ( Connection conn, String catalog ) throws DatabaseException {
        try {
            DatabaseMetaData meta = conn.getMetaData();

            try ( ResultSet r = meta.getSchemas(catalog, null) ) {
                while ( r.next() ) {
                    String schemaName = r.getString("TABLE_SCHEM"); //$NON-NLS-1$

                    dropSchemaSafe(conn, catalog, schemaName);
                }
            }

        }
        catch ( SQLException e ) {
            log.error("Failed to clear database " + catalog, e); //$NON-NLS-1$
            throw new DatabaseException("Failed to clear database:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param catalog
     * @param schemaName
     * @throws DatabaseException
     */
    private void dropSchemaSafe ( Connection conn, String catalog, String schemaName ) throws DatabaseException {
        if ( schemaName.startsWith("SYS") //$NON-NLS-1$
                || "NULLID".equals(schemaName) //$NON-NLS-1$
                || schemaName.startsWith("SQL") ) { //$NON-NLS-1$
            return;
        }

        this.clearSchema(conn, catalog, schemaName);
        this.dropSchema(conn, catalog, schemaName);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#isLockSupported()
     */
    @Override
    public boolean isLockSupported () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#lockDatabase(java.sql.Connection)
     */
    @Override
    public void lockDatabase ( Connection conn ) throws DatabaseException {
        try ( Statement s = conn.createStatement() ) {
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_FREEZE_DATABASE()"); //$NON-NLS-1$
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to lock database", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DatabaseException
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#unlockDatabase(java.sql.Connection)
     */
    @Override
    public void unlockDatabase ( Connection conn ) throws DatabaseException {
        try ( Statement s = conn.createStatement() ) {
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_UNFREEZE_DATABASE()"); //$NON-NLS-1$
        }
        catch ( SQLException e ) {
            throw new DatabaseException("Failed to lock database", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#getValidationQuery()
     */
    @Override
    public String getValidationQuery () {
        return "values 1"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SQLException
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#setParameter(java.sql.PreparedStatement, int, java.util.UUID)
     */
    @Override
    public void setParameter ( PreparedStatement ps, int i, UUID uuid ) throws SQLException {
        ps.setBytes(i, UUIDUtil.toBytes(uuid));
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SQLException
     *
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#extractUUID(java.sql.ResultSet, int)
     */
    @Override
    public UUID extractUUID ( ResultSet rs, int i ) throws SQLException {
        return UUIDUtil.fromBytes(rs.getBytes(i));
    }
}

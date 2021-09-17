/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;


/**
 * @author mbechler
 * 
 */
public class DefaultSchemaSettingDataSourceProxy implements DataSource {

    private DataSource delegate;
    private DatabaseDriverUtil databaseUtil;
    private String defaultCatalog;
    private String defaultSchema;


    /**
     * @param delegate
     *            Backing DataSource
     * @param databaseUtil
     *            DatabaseUtil instance for backing database type
     * @param defaultCatalog
     *            Default catalog to set for each connection
     * @param defaultSchema
     *            Default schema to set for each connection
     */
    public DefaultSchemaSettingDataSourceProxy ( DataSource delegate, DatabaseDriverUtil databaseUtil, String defaultCatalog, String defaultSchema ) {
        super();
        this.delegate = delegate;
        this.databaseUtil = databaseUtil;
        this.defaultCatalog = defaultCatalog;
        this.defaultSchema = defaultSchema;
    }


    @Override
    public Connection getConnection () throws SQLException {
        Connection c = this.delegate.getConnection();
        try {
            this.databaseUtil.setConnectionDefaultSchema(c, this.defaultCatalog, this.defaultSchema);
            return c;
        }
        catch ( SQLException e ) {
            c.close();
            throw e;
        }
    }


    @Override
    public Connection getConnection ( String username, String password ) throws SQLException {
        Connection c = this.delegate.getConnection(username, password);
        try {
            this.databaseUtil.setConnectionDefaultSchema(c, this.defaultCatalog, this.defaultSchema);
            return c;
        }
        catch ( SQLException e ) {
            c.close();
            throw e;
        }
    }


    @Override
    public PrintWriter getLogWriter () throws SQLException {
        return this.delegate.getLogWriter();
    }


    @Override
    public int getLoginTimeout () throws SQLException {
        return this.delegate.getLoginTimeout();
    }


    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException {
        return this.delegate.getParentLogger();
    }


    @Override
    public boolean isWrapperFor ( Class<?> iface ) throws SQLException {
        return this.delegate.isWrapperFor(iface);
    }


    @Override
    public void setLogWriter ( PrintWriter out ) throws SQLException {
        this.delegate.setLogWriter(out);
    }


    @Override
    public void setLoginTimeout ( int seconds ) throws SQLException {
        this.delegate.setLoginTimeout(seconds);
    }


    @Override
    public <T> T unwrap ( Class<T> iface ) throws SQLException {
        return this.delegate.unwrap(iface);
    }

}

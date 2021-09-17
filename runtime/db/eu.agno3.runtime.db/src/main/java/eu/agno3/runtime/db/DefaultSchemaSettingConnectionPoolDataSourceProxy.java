/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;


/**
 * @author mbechler
 * 
 */
public class DefaultSchemaSettingConnectionPoolDataSourceProxy implements ConnectionPoolDataSource {

    private ConnectionPoolDataSource delegate;
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
    public DefaultSchemaSettingConnectionPoolDataSourceProxy ( ConnectionPoolDataSource delegate, DatabaseDriverUtil databaseUtil, String defaultCatalog,
            String defaultSchema ) {
        super();
        this.delegate = delegate;
        this.databaseUtil = databaseUtil;
        this.defaultCatalog = defaultCatalog;
        this.defaultSchema = defaultSchema;
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
    public PooledConnection getPooledConnection () throws SQLException {
        PooledConnection c = this.delegate.getPooledConnection();
        return new DefaultSchemaPooledConnectionProxy(c, this.databaseUtil, this.defaultCatalog, this.defaultSchema);
    }


    @Override
    public PooledConnection getPooledConnection ( String username, String password ) throws SQLException {
        PooledConnection c = this.delegate.getPooledConnection(username, password);
        return new DefaultSchemaPooledConnectionProxy(c, this.databaseUtil, this.defaultCatalog, this.defaultSchema);
    }


    @Override
    public void setLogWriter ( PrintWriter out ) throws SQLException {
        this.delegate.setLogWriter(out);
    }


    @Override
    public void setLoginTimeout ( int seconds ) throws SQLException {
        this.delegate.setLoginTimeout(seconds);
    }

}

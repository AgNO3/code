/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.DataSource;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public class AdministrativeDataSource implements DataSource {

    private DataSource backing;


    /**
     * 
     * @param backing
     *            wrapped datasource
     */
    public AdministrativeDataSource ( DataSource backing ) {
        this.backing = backing;
        Objects.requireNonNull(backing);
    }


    /**
     * @return the backing
     */
    public DataSource getBacking () {
        return this.backing;
    }


    /**
     * @return
     * @throws SQLException
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection () throws SQLException {
        return this.backing.getConnection();
    }


    /**
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    @Override
    public Connection getConnection ( String username, String password ) throws SQLException {
        return this.backing.getConnection(username, password);
    }


    /**
     * @return
     * @throws SQLException
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter () throws SQLException {
        return this.backing.getLogWriter();
    }


    /**
     * @return
     * @throws SQLException
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout () throws SQLException {
        return this.backing.getLoginTimeout();
    }


    /**
     * @return
     * @throws SQLFeatureNotSupportedException
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException {
        return this.backing.getParentLogger();
    }


    /**
     * @param arg0
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor ( Class<?> arg0 ) throws SQLException {
        return this.backing.isWrapperFor(arg0);
    }


    /**
     * @param arg0
     * @throws SQLException
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter ( PrintWriter arg0 ) throws SQLException {
        this.backing.setLogWriter(arg0);
    }


    /**
     * @param arg0
     * @throws SQLException
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout ( int arg0 ) throws SQLException {
        this.backing.setLoginTimeout(arg0);
    }


    /**
     * @param arg0
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap ( Class<T> arg0 ) throws SQLException {
        return this.backing.unwrap(arg0);
    }

}

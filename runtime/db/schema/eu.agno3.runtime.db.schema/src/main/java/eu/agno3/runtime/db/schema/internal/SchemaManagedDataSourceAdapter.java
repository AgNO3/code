/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.internal;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.db.schema.SchemaManager;


/**
 * @author mbechler
 * 
 */
@Component (
    service = SchemaManagedDataSource.class,
    configurationPid = SchemaManagedDataSourceAdapter.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SchemaManagedDataSourceAdapter implements SchemaManagedDataSource {

    /**
     * 
     */
    private static final String FAILED_TO_UPDATE_SCHEMA = "Failed to update schema:"; //$NON-NLS-1$

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchemaManagedDataSourceAdapter.class);

    /**
     * 
     */
    public static final String PID = "eu.agno3.runtime.db.schema.internal.SchemaManagedDataSourceAdapter"; //$NON-NLS-1$

    private DataSource delegate;
    private DataSourceUtil dsUtil;
    private SchemaManager manager;
    private String defaultSchema;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.defaultSchema = this.dsUtil.createMetadata().getDefaultSchema();
        if ( log.isDebugEnabled() ) {
            log.debug("Activated SchemaManagedDataSourceAdapter for " + ctx.getProperties().get(DataSourceFactory.JDBC_DATASOURCE_NAME)); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setDataSource ( DataSource ds ) {
        this.delegate = ds;
    }


    protected synchronized void unsetDataSource ( DataSource ds ) {
        if ( this.delegate == ds ) {
            this.delegate = null;
        }
    }


    @Reference
    protected synchronized void setDsUtil ( DataSourceUtil util ) {
        this.dsUtil = util;
    }


    protected synchronized void unsetDsUtil ( DataSourceUtil util ) {
        if ( this.dsUtil == util ) {
            this.dsUtil = null;
        }
    }


    @Reference
    protected synchronized void setSchemaManager ( SchemaManager sm ) {
        this.manager = sm;
    }


    protected synchronized void unsetSchemaManager ( SchemaManager sm ) {
        if ( this.manager == sm ) {
            this.manager = null;
        }
    }


    /**
     * @return the manager
     */
    @Override
    public SchemaManager getSchemaManager () {
        return this.manager;
    }


    /**
     * @return the dsUtil
     */
    @Override
    public DataSourceUtil getDataSourceUtil () {
        return this.dsUtil;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection () throws SQLException {
        try {
            this.manager.ensureUpToDate();
        }
        catch ( SchemaException e ) {
            throw new SQLException(FAILED_TO_UPDATE_SCHEMA, e);
        }

        Connection c = this.delegate.getConnection();
        try {
            setDefaultSchema(c);
            return c;
        }
        catch ( SQLException e ) {
            log.error("Failed to set schema", e); //$NON-NLS-1$
            c.close();
            throw e;
        }
    }


    /**
     * @throws SQLException
     */
    private void setDefaultSchema ( Connection c ) throws SQLException {
        if ( c instanceof DelegatingConnection && ( (DelegatingConnection<?>) c ).getInnermostDelegate() != null ) {
            ( (DelegatingConnection<?>) c ).getInnermostDelegate().setSchema(this.defaultSchema);
            return;
        }

        this.dsUtil.setConnectionDefaultSchema(c, null, this.defaultSchema);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    @Override
    public Connection getConnection ( String username, String password ) throws SQLException {
        try {
            this.manager.ensureUpToDate();
        }
        catch ( SchemaException e ) {
            throw new SQLException(FAILED_TO_UPDATE_SCHEMA, e);
        }

        Connection c = this.delegate.getConnection(username, password);
        try {
            setDefaultSchema(c);
            return c;
        }
        catch ( SQLException e ) {
            c.close();
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter () throws SQLException {
        return this.delegate.getLogWriter();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout () throws SQLException {
        return this.delegate.getLoginTimeout();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException {
        return this.delegate.getParentLogger();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter ( PrintWriter out ) throws SQLException {
        this.delegate.setLogWriter(out);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout ( int seconds ) throws SQLException {
        this.delegate.setLoginTimeout(seconds);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor ( Class<?> clazz ) throws SQLException {
        return this.delegate.isWrapperFor(clazz);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap ( Class<T> clazz ) throws SQLException {
        return this.delegate.unwrap(clazz);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaManagedDataSource#ensureUpToDate()
     */
    @Override
    public void ensureUpToDate () throws SchemaException {
        this.manager.ensureUpToDate();
    }

}

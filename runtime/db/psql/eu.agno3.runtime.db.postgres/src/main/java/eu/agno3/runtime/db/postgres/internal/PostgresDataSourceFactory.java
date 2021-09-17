/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 20, 2017 by mbechler
 */
package eu.agno3.runtime.db.postgres.internal;


import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.xa.PGXADataSource;

import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.util.log.LogWriter;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    DataSourceFactory.class, PostgresDataSourceFactory.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver"
}, immediate = true )
public class PostgresDataSourceFactory implements DataSourceFactory {

    /**
     * 
     */
    public static final String DEFAULT_SCHEMA = "defaultSchema"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SSL = "ssl"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(PostgresDataSourceFactory.class);

    /**
     * Extra connection attributes
     */
    public static final String EXTRA_ATTRIBUTES = "extraAttributes"; //$NON-NLS-1$


    @Reference ( target = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver)" )
    protected synchronized void setDriverUtil ( DatabaseDriverUtil util ) {
        // dependency only
    }


    protected synchronized void unsetDriverUtil ( DatabaseDriverUtil util ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.jdbc.DataSourceFactory#createConnectionPoolDataSource(java.util.Properties)
     */
    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource ( Properties props ) throws SQLException {
        log.debug("Creating pooled postgres data source"); //$NON-NLS-1$
        PGConnectionPoolDataSource ds = new org.postgresql.ds.PGConnectionPoolDataSource();
        configureDataSource(ds, props);
        return ds;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.jdbc.DataSourceFactory#createDataSource(java.util.Properties)
     */
    @Override
    public DataSource createDataSource ( Properties props ) throws SQLException {
        log.debug("Creating simple postgres data source"); //$NON-NLS-1$
        PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
        configureDataSource(ds, props);
        return ds;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.jdbc.DataSourceFactory#createXADataSource(java.util.Properties)
     */
    @Override
    public XADataSource createXADataSource ( Properties props ) throws SQLException {
        log.debug("Creating XA postgres data source"); //$NON-NLS-1$
        PGXADataSource ds = new org.postgresql.xa.PGXADataSource();
        configureDataSource(ds, props);
        return ds;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.jdbc.DataSourceFactory#createDriver(java.util.Properties)
     */
    @Override
    public Driver createDriver ( Properties props ) throws SQLException {
        return new org.postgresql.Driver();
    }


    protected static void configureDataSource ( org.postgresql.ds.common.BaseDataSource ds, Properties props ) throws SQLException {

        if ( props.getProperty(JDBC_DATASOURCE_NAME) == null ) {
            throw new DatabaseConfigurationException("Datasource name is required"); //$NON-NLS-1$
        }

        if ( props.getProperty(JDBC_URL) != null ) {
            ds.setUrl(props.getProperty(JDBC_URL));
        }
        else if ( props.getProperty(JDBC_SERVER_NAME) != null ) {
            ds.setServerName(props.getProperty(JDBC_SERVER_NAME));
            if ( props.getProperty(JDBC_DATABASE_NAME) == null ) {
                throw new DatabaseConfigurationException("Database name is required"); //$NON-NLS-1$
            }
            ds.setDatabaseName(props.getProperty(JDBC_DATABASE_NAME));
            String portSpec = props.getProperty(JDBC_PORT_NUMBER);
            if ( !StringUtils.isBlank(portSpec) ) {
                ds.setPortNumber(Integer.parseInt(portSpec));
            }
            String sslSpec = props.getProperty(SSL);
            if ( !StringUtils.isBlank(sslSpec) ) {
                ds.setSsl(Boolean.parseBoolean(sslSpec));
            }
        }
        else {
            throw new DatabaseConfigurationException("Datasource specification is required"); //$NON-NLS-1$
        }

        ds.setUser(props.getProperty(JDBC_USER));
        ds.setPassword(props.getProperty(JDBC_PASSWORD));

        if ( props.get(DEFAULT_SCHEMA) != null ) {
            ds.setCurrentSchema((String) props.get(DEFAULT_SCHEMA));
        }

        Logger logger = Logger.getLogger(Driver.class);
        if ( logger.isDebugEnabled() ) {
            ds.setLogWriter(LogWriter.createWriter(logger, Level.DEBUG));
        }
    }
}

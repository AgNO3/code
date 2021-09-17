/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.client;


import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.BasicClientConnectionPoolDataSource40;
import org.apache.derby.jdbc.BasicClientDataSource40;
import org.apache.derby.jdbc.BasicClientXADataSource40;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.db.DefaultSchemaSettingConnectionPoolDataSourceProxy;
import eu.agno3.runtime.db.DefaultSchemaSettingDataSourceProxy;
import eu.agno3.runtime.db.DefaultSchemaSettingXADataSourceProxy;
import eu.agno3.runtime.db.derby.DerbyConfiguration;
import eu.agno3.runtime.db.derby.util.internal.DerbyNetworkDataSourceUtil;
import eu.agno3.runtime.util.log.LogWriter;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DataSourceFactory.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.ClientDriver"
}, immediate = true )
public class DerbyNetworkDataSourceFactory implements DataSourceFactory {

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby.network"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DerbyNetworkDataSourceFactory.class);

    /**
     * Extra connection attributes
     */
    public static final String EXTRA_ATTRIBUTES = "extraAttributes"; //$NON-NLS-1$

    private static final String DEFAULT_SCHEMA = "APP"; //$NON-NLS-1$


    @Reference ( target = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.EmbeddedDriver)" )
    protected synchronized void setDriverUtil ( DatabaseDriverUtil util ) {
        // dependency only
    }


    protected synchronized void unsetDriverUtil ( DatabaseDriverUtil util ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createDataSource(java.util.Properties)
     */
    @Override
    public DataSource createDataSource ( Properties props ) throws SQLException {
        log.debug("Creating simple networked derby data source"); //$NON-NLS-1$
        BasicClientDataSource40 ds = new BasicClientDataSource40();
        configureDataSource(ds, props);
        return new DefaultSchemaSettingDataSourceProxy(ds, new DerbyNetworkDataSourceUtil(), null, props.getProperty(
            DerbyConfiguration.DEFAULT_SCHEMA_ATTR,
            DEFAULT_SCHEMA));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createConnectionPoolDataSource(java.util.Properties)
     */
    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource ( Properties props ) throws SQLException {
        log.debug("Creating pooled networked derby data source"); //$NON-NLS-1$
        BasicClientConnectionPoolDataSource40 ds = new BasicClientConnectionPoolDataSource40();
        configureDataSource(ds, props);
        return new DefaultSchemaSettingConnectionPoolDataSourceProxy(ds, new DerbyNetworkDataSourceUtil(), null, props.getProperty(
            DerbyConfiguration.DEFAULT_SCHEMA_ATTR,
            DEFAULT_SCHEMA));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createXADataSource(java.util.Properties)
     */
    @Override
    public XADataSource createXADataSource ( Properties props ) throws SQLException {
        log.debug("Creating transactional networked derby data source"); //$NON-NLS-1$
        BasicClientXADataSource40 ds = new BasicClientXADataSource40();
        configureDataSource(ds, props);
        return new DefaultSchemaSettingXADataSourceProxy(ds, new DerbyNetworkDataSourceUtil(), null, props.getProperty(
            DerbyConfiguration.DEFAULT_SCHEMA_ATTR,
            DEFAULT_SCHEMA));
    }


    protected static void configureDataSource ( BasicClientDataSource40 ds, Properties props ) throws SQLException {

        if ( props.getProperty(JDBC_DATABASE_NAME) == null ) {
            throw new DatabaseConfigurationException("Database name is required"); //$NON-NLS-1$
        }
        ds.setDatabaseName(props.getProperty(JDBC_DATABASE_NAME));

        if ( props.getProperty(JDBC_DATASOURCE_NAME) == null ) {
            throw new DatabaseConfigurationException("Datasource name is required"); //$NON-NLS-1$
        }
        ds.setDataSourceName(props.getProperty(JDBC_DATASOURCE_NAME));

        ds.setDescription(props.getProperty(JDBC_DESCRIPTION));

        ds.setServerName(props.getProperty(JDBC_SERVER_NAME));

        if ( props.getProperty(JDBC_PORT_NUMBER) != null ) {
            ds.setPortNumber(Integer.parseInt(props.getProperty(JDBC_PORT_NUMBER)));
        }

        ds.setUser(props.getProperty(JDBC_USER));
        ds.setPassword(props.getProperty(JDBC_PASSWORD));

        ds.setConnectionAttributes(props.getProperty(EXTRA_ATTRIBUTES));

        Logger logger = Logger.getLogger(ClientDriver.class);
        if ( logger.isDebugEnabled() ) {
            ds.setLogWriter(LogWriter.createWriter(logger, Level.DEBUG));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createDriver(java.util.Properties)
     */
    @Override
    public Driver createDriver ( Properties props ) throws SQLException {
        log.debug("Creating Derby ClientDriver"); //$NON-NLS-1$
        return new ClientDriver();
    }

}

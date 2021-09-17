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

import org.apache.derby.jdbc.BasicEmbeddedConnectionPoolDataSource40;
import org.apache.derby.jdbc.BasicEmbeddedDataSource40;
import org.apache.derby.jdbc.BasicEmbeddedXADataSource40;
import org.apache.derby.jdbc.EmbeddedDriver;
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
import eu.agno3.runtime.db.derby.util.internal.DerbyEmbeddedDataSourceUtil;
import eu.agno3.runtime.util.log.LogWriter;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DataSourceFactory.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.EmbeddedDriver"
}, immediate = true )
public class DerbyEmbeddedDataSourceFactory implements DataSourceFactory {

    private static final String DEFAULT_SCHEMA = "APP"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DerbyEmbeddedDataSourceFactory.class);

    /**
     * Extra connection attributes
     */
    public static final String EXTRA_ATTRIBUTES = "extraAttributes"; //$NON-NLS-1$


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
        log.debug("Creating simple embedded derby data source"); //$NON-NLS-1$
        BasicEmbeddedXADataSource40 ds = new BasicEmbeddedXADataSource40();
        configureDataSource(ds, props);
        return new DefaultSchemaSettingDataSourceProxy(ds, new DerbyEmbeddedDataSourceUtil(), null, props.getProperty(
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
        log.debug("Creating pooled embedded derby data source"); //$NON-NLS-1$
        BasicEmbeddedConnectionPoolDataSource40 ds = new BasicEmbeddedConnectionPoolDataSource40();
        configureDataSource(ds, props);
        return new DefaultSchemaSettingConnectionPoolDataSourceProxy(ds, new DerbyEmbeddedDataSourceUtil(), null, props.getProperty(
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
        log.debug("Creating transactional embedded derby data source"); //$NON-NLS-1$
        BasicEmbeddedXADataSource40 ds = new BasicEmbeddedXADataSource40();
        configureDataSource(ds, props);

        return new DefaultSchemaSettingXADataSourceProxy(ds, new DerbyEmbeddedDataSourceUtil(), null, props.getProperty(
            DerbyConfiguration.DEFAULT_SCHEMA_ATTR,
            DEFAULT_SCHEMA));
    }


    protected static void configureDataSource ( BasicEmbeddedDataSource40 ds, Properties props ) throws SQLException {

        if ( props.getProperty(JDBC_DATABASE_NAME) == null ) {
            throw new DatabaseConfigurationException("Database name is required"); //$NON-NLS-1$
        }

        ds.setDatabaseName(props.getProperty(JDBC_DATABASE_NAME));

        if ( props.getProperty(JDBC_DATASOURCE_NAME) == null ) {
            throw new DatabaseConfigurationException("Datasource name is required"); //$NON-NLS-1$
        }

        ds.setDataSourceName(props.getProperty(JDBC_DATASOURCE_NAME));

        ds.setDescription(props.getProperty(JDBC_DESCRIPTION));

        ds.setUser(props.getProperty(JDBC_USER));
        ds.setPassword(props.getProperty(JDBC_PASSWORD));

        ds.setConnectionAttributes(props.getProperty(EXTRA_ATTRIBUTES));

        Logger logger = Logger.getLogger(EmbeddedDriver.class);
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
        log.debug("Creating Derby EmbeddedDriver"); //$NON-NLS-1$
        return new EmbeddedDriver();
    }

}

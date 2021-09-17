/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.db.embedded;


import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.db.DatabaseConfigurationException;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractEmbeddedDBServer implements EmbeddedDBServer {

    private static final Logger log = Logger.getLogger(AbstractEmbeddedDBServer.class);

    private static final String[] COPY_PROPERTIES = {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_DESCRIPTION,
        DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_URL, DataSourceFactory.JDBC_PORT_NUMBER, DataSourceFactory.JDBC_USER,
        DataSourceFactory.JDBC_PASSWORD
    };

    private ComponentContext componentContext;
    private DataSourceFactory assocDataSourceFactory;


    /**
     * @return the assocDataSourceFactory
     */
    protected DataSourceFactory getAssocDataSourceFactory () {
        if ( this.assocDataSourceFactory == null ) {
            throw new IllegalStateException("Associated data source factory is unavailable"); //$NON-NLS-1$
        }
        return this.assocDataSourceFactory;
    }


    protected void setAssocDataSourceFactory ( DataSourceFactory dsf ) {
        this.assocDataSourceFactory = dsf;
    }


    protected void unsetAssocDataSourceFactory ( DataSourceFactory dsf ) {
        if ( this.assocDataSourceFactory == dsf ) {
            this.assocDataSourceFactory = null;
        }
    }


    /**
     * @return the componentContext
     */
    public ComponentContext getComponentContext () {
        if ( this.componentContext == null ) {
            throw new IllegalStateException("DB Server used while uninitialized"); //$NON-NLS-1$
        }
        return this.componentContext;
    }


    @Activate
    protected void activate ( ComponentContext context ) throws SQLException, CryptoException {
        this.componentContext = context;

        log.debug("Activating DB server"); //$NON-NLS-1$

        String driverClass = (String) context.getProperties().get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        if ( driverClass == null ) {
            throw new DatabaseConfigurationException("No driver class specified for server"); //$NON-NLS-1$
        }

        this.start();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) throws CryptoException {
        log.debug("Deactivating DB server"); //$NON-NLS-1$
        try {
            this.stop();
        }
        catch ( SQLException e ) {
            log.error("Failed to stop server:", e); //$NON-NLS-1$
            return;
        }

        this.componentContext = null;
    }


    protected Properties initDataSourceProperties () {
        Properties props = new Properties();

        for ( String property : COPY_PROPERTIES ) {
            String value = (String) this.getComponentContext().getProperties().get(property);

            if ( value != null ) {
                props.setProperty(property, value);
            }
        }

        return props;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createConnectionPoolDataSource(java.util.Properties)
     */
    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource ( Properties extraProperties ) throws SQLException {
        Properties dsProperties = this.initDataSourceProperties();
        dsProperties.putAll(extraProperties);
        return this.getAssocDataSourceFactory().createConnectionPoolDataSource(dsProperties);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createDataSource(java.util.Properties)
     */
    @Override
    public DataSource createDataSource ( Properties extraProperties ) throws SQLException {
        Properties dsProperties = this.initDataSourceProperties();
        dsProperties.putAll(extraProperties);
        return this.getAssocDataSourceFactory().createDataSource(dsProperties);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createXADataSource(java.util.Properties)
     */
    @Override
    public XADataSource createXADataSource ( Properties extraProperties ) throws SQLException {
        Properties dsProperties = this.initDataSourceProperties();
        dsProperties.putAll(extraProperties);
        return this.getAssocDataSourceFactory().createXADataSource(dsProperties);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.jdbc.DataSourceFactory#createDriver(java.util.Properties)
     */
    @Override
    public Driver createDriver ( Properties props ) throws SQLException {
        return this.getAssocDataSourceFactory().createDriver(props);
    }
}

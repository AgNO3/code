/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 20, 2017 by mbechler
 */
package eu.agno3.runtime.db.postgres.internal;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true, configurationPid = "db.postgres", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class PostgresDataSource {

    private static final Logger log = Logger.getLogger(PostgresDataSource.class);

    static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_DESCRIPTION,
        DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_PORT_NUMBER, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, DataSourceFactory.JDBC_URL, DataSourceFactory.JDBC_USER,
        PostgresDataSourceFactory.DEFAULT_SCHEMA, PostgresDataSourceFactory.SSL
    };

    private PostgresDataSourceFactory dataSourceFactory;
    private ServiceRegistration<XADataSource> xaRegistration;
    private ServiceRegistration<AdministrativeDataSource> adminRegistration;
    private ServiceRegistration<DataSource> simpleRegistration;


    @Reference
    protected synchronized void setDataSourceFactory ( PostgresDataSourceFactory df ) {
        this.dataSourceFactory = df;
    }


    protected synchronized void unsetDataSourceFactory ( PostgresDataSourceFactory df ) {
        if ( this.dataSourceFactory == df ) {
            this.dataSourceFactory = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> props = ctx.getProperties();
        String dataSourceName = (String) props.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        String url = (String) props.get(DataSourceFactory.JDBC_URL);
        XADataSource xaDs = null;
        DataSource simpleDs = null;
        AdministrativeDataSource adminDs = null;

        Properties dsProperties = new Properties();
        for ( String prop : COPY_PROPERTIES ) {
            if ( props.get(prop) != null ) {
                dsProperties.put(prop, props.get(prop));
            }
        }

        try {
            String pw = ConfigUtil.parseSecret(props, DataSourceFactory.JDBC_PASSWORD, null);
            if ( pw != null ) {
                dsProperties.put(DataSourceFactory.JDBC_PASSWORD, pw);
            }
        }
        catch ( IOException e ) {
            log.error("Failed to get database password", e); //$NON-NLS-1$
            return;
        }

        try {
            simpleDs = this.dataSourceFactory.createDataSource(dsProperties);
            adminDs = new AdministrativeDataSource(simpleDs);
            xaDs = this.dataSourceFactory.createXADataSource(dsProperties);
        }
        catch ( SQLException e ) {
            log.error("Failed to create datasources:", e); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering datasources %s for %s", dataSourceName, url)); //$NON-NLS-1$
        }

        this.simpleRegistration = registerDs(ctx, props, DataSource.class, simpleDs);
        this.xaRegistration = registerDs(ctx, props, XADataSource.class, xaDs);
        this.adminRegistration = registerDs(ctx, props, AdministrativeDataSource.class, adminDs);
    }


    private static <T> ServiceRegistration<T> registerDs ( ComponentContext ctx, Dictionary<String, Object> props, Class<T> type, T adminDs ) {
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        for ( String prop : COPY_PROPERTIES ) {
            if ( props.get(prop) != null ) {
                serviceProperties.put(prop, props.get(prop));
            }
        }
        serviceProperties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.postgresql.Driver"); //$NON-NLS-1$
        serviceProperties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_PLAIN);
        return DsUtil.registerSafe(ctx, type, adminDs, serviceProperties);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        ServiceRegistration<XADataSource> xaReg = this.xaRegistration;
        if ( xaReg != null ) {
            this.xaRegistration = null;
            xaReg.unregister();
        }

        ServiceRegistration<AdministrativeDataSource> adminReg = this.adminRegistration;
        if ( adminReg != null ) {
            this.adminRegistration = null;
            adminReg.unregister();
        }

        ServiceRegistration<DataSource> simpleReg = this.simpleRegistration;
        if ( simpleReg != null ) {
            this.simpleRegistration = null;
            simpleReg.unregister();
        }

    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.internal;


import java.io.PrintWriter;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true, configurationPid = "db.pool.plain" )
public class PoolDataSourceWrapper implements ServiceTrackerCustomizer<DataSource, RegistrationHolder> {

    /**
     * 
     */
    public static final String PID = "db.pool"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(PoolDataSourceWrapper.class);

    private static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_DESCRIPTION,
        DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_PORT_NUMBER, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, DataSourceFactory.JDBC_USER
    };

    private ServiceTracker<DataSource, RegistrationHolder> tracker;
    private ComponentContext componentContext;

    private int connPoolSize = 8;
    private int connMaxIdle = 2;
    private int connMinIdle = 0;
    private long connTimeoutMs = 2000;

    private int stmtPoolSize = 50;

    private boolean debugAbandoned = false;

    private boolean blockWhenExhausted = true;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting datasource wrapper for pooled datasources"); //$NON-NLS-1$
        this.parseConfig(context.getProperties());
        this.componentContext = context;
        this.tracker = new ServiceTracker<>(context.getBundleContext(), DataSource.class, this);
        this.tracker.open();
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> properties ) {
        parsePoolParamsConfig(properties);

        String exhaustedStrategySpec = (String) properties.get("blockWhenExhausted"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(exhaustedStrategySpec) && !Boolean.parseBoolean(exhaustedStrategySpec) ) {
            this.blockWhenExhausted = false;
        }

        String stmtPoolSizeSpec = (String) properties.get("stmtPoolSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(stmtPoolSizeSpec) ) {
            this.stmtPoolSize = Integer.parseInt(stmtPoolSizeSpec);
        }
    }


    /**
     * @param properties
     */
    protected void parsePoolParamsConfig ( Dictionary<String, Object> properties ) {
        String maxActiveSpec = (String) properties.get("poolSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(maxActiveSpec) ) {
            this.connPoolSize = Integer.parseInt(maxActiveSpec.trim());
        }

        String maxIdleSpec = (String) properties.get("maxIdle"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(maxIdleSpec) ) {
            this.connMaxIdle = Integer.parseInt(maxIdleSpec.trim());
        }

        String minIdleSpec = (String) properties.get("minIdle"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(minIdleSpec) ) {
            this.connMinIdle = Integer.parseInt(minIdleSpec.trim());
        }

        String timeoutSpec = (String) properties.get("waitTimeoutMs"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(timeoutSpec) ) {
            this.connTimeoutMs = Long.parseLong(timeoutSpec.trim());
        }

        String debugAbandonedSpec = (String) properties.get("debugAbandoned"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(debugAbandonedSpec) && Boolean.parseBoolean(debugAbandonedSpec.trim()) ) {
            this.debugAbandoned = true;
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.tracker = null;
        this.componentContext = null;
    }


    private static Dictionary<String, Object> copyProperties ( ServiceReference<DataSource> reference ) {
        Dictionary<String, Object> dsProperties = new Hashtable<>();

        for ( String prop : COPY_PROPERTIES ) {
            if ( reference.getProperty(prop) != null ) {
                dsProperties.put(prop, reference.getProperty(prop));
            }
        }
        return dsProperties;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public RegistrationHolder addingService ( ServiceReference<DataSource> reference ) {
        String dataSourceName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);
        String dataSourceUser = (String) reference.getProperty(DataSourceFactory.JDBC_USER);
        String dataSourceDriver = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String dataSourceType = (String) reference.getProperty(DataSourceWrapper.TYPE);

        if ( dataSourceName == null ) {
            log.warn("Datasource without a name, ignore"); //$NON-NLS-1$
            return null;
        }

        if ( !DataSourceWrapper.TYPE_PLAIN.equals(dataSourceType) ) { // $NON-NLS-1$
            log.debug("Already wrapped"); //$NON-NLS-1$
            return null;
        }

        String resName;
        if ( dataSourceUser != null ) {
            resName = dataSourceName + '-' + dataSourceUser + '-';
        }
        else {
            resName = dataSourceName + '-';
        }

        int lastSep = dataSourceDriver.lastIndexOf('.');
        if ( lastSep >= 0 && lastSep < dataSourceDriver.length() ) {
            resName += dataSourceDriver.substring(lastSep + 1);
        }
        else {
            resName += dataSourceDriver;
        }
        resName = resName.substring(0, Math.min(45, resName.length()));

        DataSource s = this.componentContext.getBundleContext().getService(reference);

        DatabaseDriverUtil dsutil = null;
        try {
            String filter = FilterBuilder.get().eq(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, dataSourceDriver).toString();
            Collection<ServiceReference<DatabaseDriverUtil>> dsUtilRef = this.componentContext.getBundleContext()
                    .getServiceReferences(DatabaseDriverUtil.class, filter);

            if ( !dsUtilRef.isEmpty() ) {
                dsutil = this.componentContext.getBundleContext().getService(dsUtilRef.iterator().next());
            }
            else {
                log.warn("Failed to find datasource util for " + filter); //$NON-NLS-1$
            }
        }
        catch ( InvalidSyntaxException e ) {
            log.warn("Failed to get datasource util", e); //$NON-NLS-1$
        }

        PoolingDataSource<PoolableConnection> ds = createWrappedDataSource(s, dataSourceName, dataSourceUser, dsutil);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Wrapping datasource %s user %s: %s", dataSourceName, dataSourceUser, reference)); //$NON-NLS-1$
        }

        Dictionary<String, Object> properties = copyProperties(reference);
        properties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_POOLED);
        return new RegistrationHolder(DsUtil.registerSafe(this.componentContext, DataSource.class, ds, properties));
    }


    /**
     * @param dataSourceName
     * @param s
     * @param dataSourceUser
     * @param dataSourceName
     * @param dsutil
     * @param jdbc
     * @return
     */
    @SuppressWarnings ( "resource" )
    private PoolingDataSource<PoolableConnection> createWrappedDataSource ( DataSource s, String dataSourceName, String dataSourceUser,
            DatabaseDriverUtil dsutil ) {
        DataSourceConnectionFactory cf = new DataSourceConnectionFactory(s);
        ObjectName on = null;
        try {
            on = new ObjectName("org.apache.commons.dbcp2:type=PoolableConnectionFactory,dsName=" + //$NON-NLS-1$
                    dataSourceName + "/" + dataSourceUser); //$NON-NLS-1$
        }
        catch ( MalformedObjectNameException e ) {
            log.warn("Failed to create object name", e); //$NON-NLS-1$
        }
        PoolableConnectionFactory factory = new PoolableConnectionFactory(cf, on);
        factory.setPoolStatements(true);
        factory.setMaxOpenPrepatedStatements(this.stmtPoolSize);
        factory.setCacheState(false);
        if ( dsutil != null && !StringUtils.isBlank(dsutil.getValidationQuery()) ) {
            factory.setValidationQuery(dsutil.getValidationQuery());
        }

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setJmxEnabled(true);
        cfg.setJmxNamePrefix(String.format("db-%s-%s", dataSourceName, dataSourceUser)); //$NON-NLS-1$
        GenericObjectPool<PoolableConnection> connPool = new GenericObjectPool<>(factory, cfg);
        this.configureConnectionPool(connPool);
        if ( this.debugAbandoned ) {
            log.info("Enable debugging of abandoned connections"); //$NON-NLS-1$
            connPool.setAbandonedConfig(makeAbandonedConfig());
        }
        factory.setPool(connPool);

        PoolingDataSource<PoolableConnection> ds = new PoolingDataSource<>(connPool);
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setLogWriter(new PrintWriter(new LogWriter(log, Level.DEBUG)));
        return ds;
    }


    /**
     * @return
     */
    @SuppressWarnings ( "resource" )
    private static AbandonedConfig makeAbandonedConfig () {
        AbandonedConfig cfg = new AbandonedConfig();
        cfg.setLogAbandoned(true);
        cfg.setRemoveAbandonedOnBorrow(true);
        cfg.setRemoveAbandonedOnMaintenance(true);
        cfg.setLogWriter(new PrintWriter(new LogWriter(log, Level.WARN)));
        cfg.setRemoveAbandonedTimeout(30);
        cfg.setUseUsageTracking(true);
        return cfg;
    }


    /**
     * @param pool
     */
    private void configureConnectionPool ( GenericObjectPool<PoolableConnection> pool ) {
        pool.setMaxWaitMillis(this.connTimeoutMs);
        pool.setMaxTotal(this.connPoolSize);
        pool.setMaxIdle(this.connMaxIdle);
        pool.setMinIdle(this.connMinIdle);
        pool.setEvictionPolicyClassName(DefaultEvictionPolicy.class.getName());
        pool.setBlockWhenExhausted(this.blockWhenExhausted);
        pool.setTestOnReturn(true);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<DataSource> reference, RegistrationHolder service ) {
        if ( service != null ) {
            service.getServiceRegistration().setProperties(copyProperties(reference));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<DataSource> reference, RegistrationHolder service ) {
        if ( service != null ) {
            DsUtil.unregisterSafe(this.componentContext, service.getServiceRegistration());
        }
    }
}

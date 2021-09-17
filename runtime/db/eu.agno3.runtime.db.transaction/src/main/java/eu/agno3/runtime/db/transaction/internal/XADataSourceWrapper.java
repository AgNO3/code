/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2013 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import java.io.PrintWriter;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.atomikos.datasource.xa.XATransactionalResource;
import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;

import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.transaction.TransactionService;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true, configurationPid = XADataSourceWrapper.PID )
public class XADataSourceWrapper implements ServiceTrackerCustomizer<XADataSource, RegistrationHolder> {

    /**
     * 
     */
    public static final String PID = "db.pool"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(XADataSourceWrapper.class);

    private static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_DESCRIPTION,
        DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_PORT_NUMBER, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, DataSourceFactory.JDBC_USER
    };

    private ServiceTracker<XADataSource, RegistrationHolder> tracker;
    private TransactionService tm;
    private ComponentContext componentContext;

    private int connPoolSize = 8;
    private int connMaxIdle = 2;
    private int connMinIdle = 0;
    private long connTimeoutMs = 2000;

    private int stmtPoolSize = 50;

    private boolean debugAbandoned = false;

    private boolean blockWhenExhausted = true;


    @Reference
    protected synchronized void setTransactionManager ( TransactionService t ) {
        this.tm = t;
    }


    protected synchronized void unsetTransactionManager ( TransactionService t ) {
        if ( this.tm == t ) {
            this.tm = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting datasource wrapper for transactional datasources"); //$NON-NLS-1$
        this.parseConfig(context.getProperties());
        this.componentContext = context;
        this.tracker = new ServiceTracker<>(context.getBundleContext(), XADataSource.class, this);
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


    private static Dictionary<String, Object> copyProperties ( ServiceReference<XADataSource> reference ) {
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
    public RegistrationHolder addingService ( ServiceReference<XADataSource> reference ) {
        String dataSourceName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);
        String dataSourceUser = (String) reference.getProperty(DataSourceFactory.JDBC_USER);
        String dataSourceDriver = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

        if ( dataSourceName == null ) {
            log.warn("Datasource without a name, ignore"); //$NON-NLS-1$
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

        XADataSource s = this.componentContext.getBundleContext().getService(reference);

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

        JdbcTransactionalResource jdbcTransactionalResource = new WrappedJdbcTransactionalResource(resName, s, dsutil);
        jdbcTransactionalResource.useWeakCompare(dsutil != null ? dsutil.isUseXAWeakCompare() : false);
        if ( log.isDebugEnabled() ) {
            log.debug("Registering resource " + resName); //$NON-NLS-1$
        }
        this.tm.registerResource(jdbcTransactionalResource);

        ManagedDataSource<PoolableConnection> ds = createWrappedDataSource(s, dataSourceName, dataSourceUser, dsutil, jdbcTransactionalResource);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Wrapping datasource %s user %s: %s", dataSourceName, dataSourceUser, reference)); //$NON-NLS-1$
        }

        Dictionary<String, Object> properties = copyProperties(reference);
        properties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_XA);
        if ( log.isDebugEnabled() ) {
            log.debug("Properties " + properties); //$NON-NLS-1$
        }
        return new RegistrationHolder(jdbcTransactionalResource, DsUtil.registerSafe(this.componentContext, DataSource.class, ds, properties));
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
    private ManagedDataSource<PoolableConnection> createWrappedDataSource ( XADataSource s, String dataSourceName, String dataSourceUser,
            DatabaseDriverUtil dsutil, JdbcTransactionalResource jdbc ) {
        DataSourceXAConnectionFactory cf = new FixedDataSourceXAConnectionFactory(this.tm.getTransactionManager(), s);
        ObjectName on = null;
        try {
            on = new ObjectName("org.apache.commons.dbcp2:type=PoolableManagedConnectionFactory,dsName=" + //$NON-NLS-1$
                    dataSourceName + "/" + dataSourceUser); //$NON-NLS-1$
        }
        catch ( MalformedObjectNameException e ) {
            log.warn("Failed to create object name", e); //$NON-NLS-1$
        }
        FixedPoolableManagedConnectionFactory factory = new FixedPoolableManagedConnectionFactory(cf, on, jdbc);
        factory.setPoolStatements(true);
        factory.setMaxOpenPrepatedStatements(this.stmtPoolSize);
        factory.setCacheState(false);
        if ( dsutil != null && !StringUtils.isBlank(dsutil.getValidationQuery()) ) {
            factory.setValidationQuery(dsutil.getValidationQuery());
        }

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setJmxEnabled(true);
        cfg.setJmxNamePrefix(String.format("db-%s-%s", dataSourceName, dataSourceUser)); //$NON-NLS-1$
        GenericObjectPool<PoolableConnection> connPool = new FixedGenericObjectPool<>(factory, cfg);
        this.configureConnectionPool(connPool);
        if ( this.debugAbandoned ) {
            log.info("Enable debugging of abandoned connections"); //$NON-NLS-1$
            connPool.setAbandonedConfig(makeAbandonedConfig());
        }
        factory.setPool(connPool);

        ManagedDataSource<PoolableConnection> ds = new ManagedDataSource<>(connPool, cf.getTransactionRegistry());
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
    public void modifiedService ( ServiceReference<XADataSource> reference, RegistrationHolder service ) {
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
    public void removedService ( ServiceReference<XADataSource> reference, RegistrationHolder service ) {
        if ( service != null ) {
            DsUtil.unregisterSafe(this.componentContext, service.getServiceRegistration());
            XATransactionalResource jdbcTransactionalResource = service.getJdbcTransactionalResource();
            if ( jdbcTransactionalResource != null ) {
                this.tm.unregisterResource(jdbcTransactionalResource);
            }
        }
    }
}

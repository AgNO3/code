/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.runtime.db.jmx.internal;


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.AdministrativeDataSourceUtil;
import eu.agno3.runtime.db.DatabaseException;
import eu.agno3.runtime.db.jmx.DatabaseManagementMXBean;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.util.threads.NamedThreadFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = MBean.class, property = {
    "objectName=eu.agno3.runtime.db:type=DatabaseManagementBean"
} )
public class DatabaseManagementBean implements DatabaseManagementMXBean, MBean {

    private static final Logger log = Logger.getLogger(DatabaseManagementBean.class);
    private BundleContext context;

    private ScheduledExecutorService timeoutScheduler;

    private Map<String, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.context = ctx.getBundleContext();
        this.timeoutScheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DatabaseManage-Timeout")); //$NON-NLS-1$
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.timeoutScheduler.shutdownNow();
        try {
            this.timeoutScheduler.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e ) {
            log.warn("Failed to shutdown timeout scheduler", e); //$NON-NLS-1$
        }
        this.context = null;
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    @Override
    public boolean lockDataSource ( String ds, long timeoutMs ) throws DatabaseException {
        if ( this.timeoutFutures.containsKey(ds) ) {
            throw new DatabaseException("Already locking " + ds); //$NON-NLS-1$
        }
        AdministrativeDataSourceUtil dsu = getAdminDataSourceUtil(ds);
        ScheduledFuture<?> schedule = null;
        if ( timeoutMs > 0 ) {
            schedule = this.timeoutScheduler.schedule(new Runnable() {

                @Override
                public void run () {
                    try {
                        unlockDataSource(ds);
                    }
                    catch ( DatabaseException e ) {
                        getLog().error("Automatic unlocking after timeout failed", e); //$NON-NLS-1$
                    }
                }
            }, timeoutMs, TimeUnit.MILLISECONDS);
        }
        log.info("Locking database " + ds); //$NON-NLS-1$
        boolean res = dsu.lockDatabase();
        if ( !res && schedule != null ) {
            schedule.cancel(false);
        }
        else if ( schedule != null ) {
            this.timeoutFutures.put(ds, schedule);
        }
        return res;

    }


    @Override
    public void unlockDataSource ( String ds ) throws DatabaseException {
        ScheduledFuture<?> scheduledFuture = this.timeoutFutures.remove(ds);
        if ( scheduledFuture != null ) {
            scheduledFuture.cancel(false);
        }
        AdministrativeDataSourceUtil dsu = getAdminDataSourceUtil(ds);
        log.info("Unlocking database " + ds); //$NON-NLS-1$
        dsu.unlockDatabase();
    }


    /**
     * @param ds
     * @return
     * @throws InvalidSyntaxException
     * @throws DatabaseException
     */
    protected AdministrativeDataSourceUtil getAdminDataSourceUtil ( String ds ) throws DatabaseException {
        try {
            Collection<ServiceReference<AdministrativeDataSourceUtil>> refs = this.context.getServiceReferences(
                AdministrativeDataSourceUtil.class,
                FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, ds).toString());

            if ( refs == null || refs.isEmpty() ) {
                throw new DatabaseException("DataSource not found " + ds); //$NON-NLS-1$
            }

            if ( refs.size() > 1 && log.isDebugEnabled() ) {
                log.debug("Multiple datasources found for " + ds); //$NON-NLS-1$
                log.debug(refs);
            }

            ServiceReference<AdministrativeDataSourceUtil> first = refs.iterator().next();

            AdministrativeDataSourceUtil service = this.context.getService(first);
            return service;
        }
        catch ( InvalidSyntaxException e ) {
            throw new DatabaseException("Failed to get datasource", e); //$NON-NLS-1$
        }
    }
}

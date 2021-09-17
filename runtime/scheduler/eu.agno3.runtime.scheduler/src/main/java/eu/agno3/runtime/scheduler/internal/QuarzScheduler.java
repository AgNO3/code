/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.runtime.scheduler.internal;


import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.DefaultThreadExecutor;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.spi.ThreadExecutor;
import org.quartz.spi.ThreadPool;

import eu.agno3.runtime.scheduler.SchedulerConfiguration;
import eu.agno3.runtime.scheduler.SchedulerService;
import eu.agno3.runtime.scheduler.TriggeredJob;
import eu.agno3.runtime.scheduler.TriggeredJobFactory;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = SchedulerService.class, immediate = true, configurationPid = SchedulerConfiguration.PID )
public class QuarzScheduler implements SchedulerService {

    private static final Logger log = Logger.getLogger(QuarzScheduler.class);

    private Scheduler scheduler;
    private ComponentContext componentContext;

    private JobStore jobStore;
    private ThreadExecutor threadExecutor;
    private ThreadPool threadPool;
    private Map<String, SchedulerPlugin> schedulerPluginMap;

    private OSGIJobFactory jobFactory;
    private TriggeredJobTracker jobTracker;
    private TriggeredJobFactoryTracker jobFactoryTracker;

    @SuppressWarnings ( "rawtypes" )
    private ServiceTracker<TriggeredJobFactory, TriggerKey> jobFactoryServiceTracker;
    private ServiceTracker<TriggeredJob, TriggerKey> jobServiceTracker;

    @SuppressWarnings ( "rawtypes" )
    private ServiceRegistration<ServiceTracker> jobFactoryServiceTrackerRegistration;
    @SuppressWarnings ( "rawtypes" )
    private ServiceRegistration<ServiceTracker> jobServiceTrackerRegistration;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {

        this.componentContext = context;
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", Boolean.TRUE.toString()); //$NON-NLS-1$

        try {
            setupScheduler();
        }
        catch ( SchedulerException e ) {
            log.error("Failed to create scheduler:", e); //$NON-NLS-1$
            return;
        }

        try {
            this.jobFactory = new OSGIJobFactory();
            this.scheduler.setJobFactory(this.jobFactory);
        }
        catch ( SchedulerException e ) {
            log.error("Failed to set job factory", e); //$NON-NLS-1$
            this.scheduler = null;
            return;
        }

        try {
            this.scheduler.getContext().put("dsContext", context); //$NON-NLS-1$
        }
        catch ( SchedulerException e ) {
            log.error("Failed to set scheduler context", e); //$NON-NLS-1$
        }

        this.jobFactoryTracker = new TriggeredJobFactoryTracker(this, context.getBundleContext());
        this.jobFactoryServiceTracker = new ServiceTracker<>(context.getBundleContext(), TriggeredJobFactory.class, this.jobFactoryTracker);
        this.jobFactory.setJobFactoryTracker(this.jobFactoryTracker);
        this.jobFactoryServiceTracker.open();
        this.jobFactoryServiceTrackerRegistration = DsUtil.registerSafe(context, ServiceTracker.class, this.jobFactoryServiceTracker, null);

        this.jobTracker = new TriggeredJobTracker(this, context.getBundleContext());
        this.jobServiceTracker = new ServiceTracker<>(context.getBundleContext(), TriggeredJob.class, this.jobTracker);
        this.jobFactory.setJobTracker(this.jobTracker);
        this.jobServiceTracker.open();
        this.jobServiceTrackerRegistration = DsUtil.registerSafe(context, ServiceTracker.class, this.jobServiceTracker, null);

        try {
            this.scheduler.start();
        }
        catch ( SchedulerException e ) {
            log.error("Failed to start scheduler:", e); //$NON-NLS-1$
            this.scheduler = null;
            return;
        }

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {

        if ( this.jobFactoryServiceTrackerRegistration != null ) {
            DsUtil.unregisterSafe(context, this.jobFactoryServiceTrackerRegistration);
            this.jobFactoryServiceTrackerRegistration = null;
        }

        if ( this.jobFactoryServiceTracker != null ) {
            this.jobFactoryServiceTracker.close();
            this.jobFactoryServiceTracker = null;
        }

        if ( this.jobServiceTrackerRegistration != null ) {
            DsUtil.unregisterSafe(context, this.jobServiceTrackerRegistration);
            this.jobServiceTrackerRegistration = null;
        }

        if ( this.jobServiceTracker != null ) {
            this.jobServiceTracker.close();
            this.jobServiceTracker = null;
        }

        try {
            if ( this.scheduler != null ) {
                this.scheduler.shutdown();
            }
        }
        catch ( SchedulerException e ) {
            log.warn("Failed to shutdown scheduler:", e); //$NON-NLS-1$
        }

    }


    /**
     * @throws SchedulerException
     */
    private void setupScheduler () throws SchedulerException {
        String schedulerName = "RuntimeQuartzScheduler"; //$NON-NLS-1$
        String schedulerInstanceId = "SIMPLE_NON_CLUSTERED"; //$NON-NLS-1$
        long idleWaitTime = -1;
        long dbFailureRetryInterval = -1;

        DirectSchedulerFactory.getInstance().createScheduler(
            schedulerName,
            schedulerInstanceId,
            getThreadPool(),
            getThreadExecutor(),
            getJobStore(),
            this.schedulerPluginMap,
            null, // rmiRegistryHost
            -1, // rmiRegistryPort
            idleWaitTime, // idleWaitTime
            dbFailureRetryInterval, // dbFailureRetryInterval
            true, // jmxExport
            null); // jmxObjectName

        this.scheduler = DirectSchedulerFactory.getInstance().getScheduler(schedulerName);
    }


    /**
     * @return
     */
    private ThreadPool getThreadPool () {
        int threadCount = 5;

        ThreadPool useThreadPool;

        if ( this.threadPool == null ) {
            useThreadPool = new SimpleThreadPool();
            ( (SimpleThreadPool) useThreadPool ).setThreadCount(threadCount);
        }
        else {
            useThreadPool = this.threadPool;
        }
        return useThreadPool;
    }


    /**
     * @return
     */
    private ThreadExecutor getThreadExecutor () {
        ThreadExecutor useThreadExecutor;
        if ( this.threadExecutor == null ) {
            useThreadExecutor = new DefaultThreadExecutor();
        }
        else {
            useThreadExecutor = this.threadExecutor;
        }
        return useThreadExecutor;
    }


    /**
     * @return
     */
    private JobStore getJobStore () {
        JobStore useJobStore;
        if ( this.jobStore == null ) {
            useJobStore = new RAMJobStore();
        }
        else {
            useJobStore = this.jobStore;
        }
        return useJobStore;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SchedulerException
     * 
     * @see eu.agno3.runtime.scheduler.SchedulerService#scheduleJob(org.quartz.JobDetail, org.quartz.Trigger)
     */
    @Override
    public void scheduleJob ( JobDetail job, Trigger trigger ) throws SchedulerException {
        this.scheduler.scheduleJob(job, trigger);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.scheduler.SchedulerService#unscheduleJob(org.quartz.TriggerKey)
     */
    @Override
    public void unscheduleJob ( TriggerKey trigger ) throws SchedulerException {
        this.scheduler.unscheduleJob(trigger);
    }


    /**
     * @return the componentContext
     */
    ComponentContext getComponentContext () {
        return this.componentContext;
    }

}

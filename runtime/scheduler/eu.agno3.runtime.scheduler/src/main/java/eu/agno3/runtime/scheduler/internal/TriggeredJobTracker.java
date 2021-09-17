/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.runtime.scheduler.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import eu.agno3.runtime.scheduler.SchedulerService;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
public class TriggeredJobTracker implements ServiceTrackerCustomizer<TriggeredJob, TriggerKey> {

    private static final Logger log = Logger.getLogger(TriggeredJobTracker.class);

    private Map<String, TriggeredJob> map = new ConcurrentHashMap<>();
    private SchedulerService schedulerService;
    private BundleContext bundleContext;


    /**
     * @param scheduler
     * @param context
     */
    public TriggeredJobTracker ( QuarzScheduler scheduler, BundleContext context ) {
        this.schedulerService = scheduler;
        this.bundleContext = context;
    }


    /**
     * @param name
     * @return a job for the type
     */
    public TriggeredJob getJobByType ( String name ) {
        return this.map.get(name);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public TriggerKey addingService ( ServiceReference<TriggeredJob> reference ) {

        try {
            TriggeredJob job = this.bundleContext.getService(reference);

            Map<String, Object> serviceProperties = new HashMap<>();
            for ( String propertyKey : reference.getPropertyKeys() ) {
                serviceProperties.put(propertyKey, reference.getProperty(propertyKey));
            }

            String jobClassName = job.getClass().getName();
            String group = "default"; //$NON-NLS-1$

            this.map.put(jobClassName, job);
            return scheduleJob(job, serviceProperties, jobClassName, group);
        }
        catch ( Exception e ) {
            log.error("Failed to schedule job:", e); //$NON-NLS-1$
            return null;
        }

    }


    /**
     * @param job
     * @param serviceProperties
     * @param jobClassName
     * @param group
     * @return
     * @throws SchedulerException
     */
    private TriggerKey scheduleJob ( TriggeredJob job, Map<String, Object> serviceProperties, String jobClassName, String group )
            throws SchedulerException {
        JobDetail detail = JobBuilder.newJob(job.getClass()).setJobData(new JobDataMap(serviceProperties)).withIdentity(jobClassName).build();
        TriggerKey triggerKey = new TriggerKey(jobClassName, group);
        Trigger t = job.buildTrigger(TriggerBuilder.newTrigger().forJob(detail).withIdentity(triggerKey));

        if ( log.isDebugEnabled() ) {
            log.debug("Scheduling job " + triggerKey); //$NON-NLS-1$
        }
        this.schedulerService.scheduleJob(detail, t);
        return triggerKey;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<TriggeredJob> reference, TriggerKey service ) {
        // unused
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<TriggeredJob> reference, TriggerKey trigger ) {
        if ( trigger != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Unscheduling job " + trigger); //$NON-NLS-1$
                }
                this.schedulerService.unscheduleJob(trigger);
                this.map.remove(trigger.getName());
            }
            catch ( SchedulerException e ) {
                log.error("Failed to remove job:", e); //$NON-NLS-1$
            }
        }
    }

}

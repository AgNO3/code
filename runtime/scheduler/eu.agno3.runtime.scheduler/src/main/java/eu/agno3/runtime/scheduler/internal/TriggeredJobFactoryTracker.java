/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2013 by mbechler
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
import eu.agno3.runtime.scheduler.TriggeredJobFactory;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
public class TriggeredJobFactoryTracker implements ServiceTrackerCustomizer<TriggeredJobFactory, TriggerKey> {

    private static final Logger log = Logger.getLogger(TriggeredJobFactoryTracker.class);

    private Map<String, TriggeredJobFactory<?>> map = new ConcurrentHashMap<>();

    private SchedulerService schedulerService;
    private BundleContext bundleContext;


    /**
     * @param scheduler
     * @param ctx
     * 
     */
    public TriggeredJobFactoryTracker ( SchedulerService scheduler, BundleContext ctx ) {
        this.schedulerService = scheduler;
        this.bundleContext = ctx;
    }


    /**
     * @param jobType
     * @return the job factory for the given type, or null
     */
    public TriggeredJobFactory<?> getFactoryForType ( String jobType ) {
        return this.map.get(jobType);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public TriggerKey addingService ( ServiceReference<TriggeredJobFactory> reference ) {
        try {
            TriggeredJobFactory<?> jobFactory = this.bundleContext.getService(reference);

            Map<String, Object> serviceProperties = new HashMap<>();
            for ( String propertyKey : reference.getPropertyKeys() ) {
                serviceProperties.put(propertyKey, reference.getProperty(propertyKey));
            }

            String jobClassName = jobFactory.getClass().getName();
            String group = "default"; //$NON-NLS-1$

            this.map.put(jobFactory.getJobType().getName(), jobFactory);

            return scheduleJob(jobFactory, serviceProperties, jobClassName, group);
        }
        catch ( Exception e ) {
            log.error("Failed to schedule job:", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param jobFactory
     * @param serviceProperties
     * @param jobClassName
     * @param group
     * @return
     * @throws SchedulerException
     */
    private TriggerKey scheduleJob ( TriggeredJobFactory<?> jobFactory, Map<String, Object> serviceProperties, String jobClassName, String group )
            throws SchedulerException {
        JobDetail detail = JobBuilder.newJob(jobFactory.getJobType()).setJobData(new JobDataMap(serviceProperties)).withIdentity(jobClassName)
                .build();
        TriggerKey triggerKey = new TriggerKey(jobClassName, group);
        Trigger t = jobFactory.buildTrigger(TriggerBuilder.newTrigger().forJob(detail).withIdentity(triggerKey));

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
    public void modifiedService ( ServiceReference<TriggeredJobFactory> reference, TriggerKey service ) {
        // unused
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<TriggeredJobFactory> reference, TriggerKey service ) {
        if ( service != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Unscheduling job " + service); //$NON-NLS-1$
                }
                this.schedulerService.unscheduleJob(service);
                this.map.remove(service.getClass().getName());
            }
            catch ( SchedulerException e ) {
                log.error("Failed to unschedule job " + service, e); //$NON-NLS-1$
            }
        }
    }

}

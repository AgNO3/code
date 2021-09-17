/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2013 by mbechler
 */
package eu.agno3.runtime.scheduler.internal;


import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import eu.agno3.runtime.scheduler.TriggeredJob;
import eu.agno3.runtime.scheduler.TriggeredJobFactory;


/**
 * @author mbechler
 *
 */
public class OSGIJobFactory extends SimpleJobFactory {

    private static final Logger log = Logger.getLogger(OSGIJobFactory.class);

    private TriggeredJobFactoryTracker jobFactoryTracker;
    private TriggeredJobTracker jobTracker;


    /**
     * @param jobFactoryTracker
     *            the jobFactoryTracker to set
     */
    public void setJobFactoryTracker ( TriggeredJobFactoryTracker jobFactoryTracker ) {
        this.jobFactoryTracker = jobFactoryTracker;
    }


    /**
     * @param jobTracker
     *            the jobTracker to set
     */
    public void setJobTracker ( TriggeredJobTracker jobTracker ) {
        this.jobTracker = jobTracker;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.simpl.SimpleJobFactory#newJob(org.quartz.spi.TriggerFiredBundle, org.quartz.Scheduler)
     */

    @Override
    public Job newJob ( TriggerFiredBundle trigger, Scheduler sched ) throws SchedulerException {

        if ( this.jobFactoryTracker == null || this.jobTracker == null ) {
            return new InvalidJob();
        }

        Class<?> jobClass = trigger.getJobDetail().getJobClass();

        Job j = makeJobUsingFactory(jobClass);

        if ( j != null ) {
            return j;
        }

        if ( TriggeredJob.class.isAssignableFrom(jobClass) ) {
            return getTriggeredJobInstance(jobClass);
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Creating job using default instantiation: " + jobClass.getName()); //$NON-NLS-1$
        }
        return super.newJob(trigger, sched);
    }


    /**
     * @param jobClass
     * @param j
     * @return
     * @throws SchedulerException
     */
    private Job getTriggeredJobInstance ( Class<?> jobClass ) throws SchedulerException {
        if ( log.isTraceEnabled() ) {
            log.trace("Using TriggeredJob instance for: " + jobClass.getName()); //$NON-NLS-1$
        }

        TriggeredJob j = this.jobTracker.getJobByType(jobClass.getName());
        if ( j == null ) {
            log.warn(String.format("TriggeredJob found but not registered as a service (jobType=%s), cannot run", jobClass.getName())); //$NON-NLS-1$
            return new InvalidJob();
        }
        return j;
    }


    /**
     * @param jobClass
     * @return
     * @throws SchedulerException
     */

    private Job makeJobUsingFactory ( Class<?> jobClass ) throws SchedulerException {
        TriggeredJobFactory<?> factoryForType = this.jobFactoryTracker.getFactoryForType(jobClass.getName());
        if ( factoryForType == null ) {
            return null;
        }
        return factoryForType.createJobInstance();
    }

}

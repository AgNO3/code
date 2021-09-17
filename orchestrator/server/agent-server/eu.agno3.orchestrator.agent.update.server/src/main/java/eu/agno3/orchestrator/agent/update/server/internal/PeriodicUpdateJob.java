/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 26, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.internal;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.agent.update.server.internal.UpdateCheckJobRunnableFactory.UpdateJobRunnable;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 *
 */
@DisallowConcurrentExecution
@Component (
    service = TriggeredJob.class,
    property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.agent.update.server.internal.PeriodicUpdateJob" )
public class PeriodicUpdateJob implements TriggeredJob {

    private static final Random RANDOM = new Random();
    private static final Logger log = Logger.getLogger(PeriodicUpdateJob.class);

    private UpdateCheckJobRunnableFactory updateCheckFactory;


    @Reference
    protected synchronized void setUpdateCheckFactory ( UpdateCheckJobRunnableFactory rf ) {
        this.updateCheckFactory = rf;
    }


    protected synchronized void unsetUpdateCheckFactory ( UpdateCheckJobRunnableFactory rf ) {
        if ( this.updateCheckFactory == rf ) {
            this.updateCheckFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        log.debug("Running periodic update check"); //$NON-NLS-1$
        try {
            UpdateJobRunnable r = this.updateCheckFactory.updateAll();
            r.run();
            Map<String, Set<String>> streamsWithUpdate = r.getStreamsWithUpdate();
            for ( Entry<String, Set<String>> e : streamsWithUpdate.entrySet() ) {
                log.info(String.format("Found updates for %s: %s", e.getKey(), e.getValue())); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.debug("Failure during periodic update check", e); //$NON-NLS-1$
            throw new JobExecutionException("Failed to check for updates", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(1, RANDOM.nextInt(60))).build();
    }

}

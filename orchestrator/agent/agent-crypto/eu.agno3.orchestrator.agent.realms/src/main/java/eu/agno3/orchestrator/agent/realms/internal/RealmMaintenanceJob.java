/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 *
 */
@DisallowConcurrentExecution
@Component ( service = TriggeredJob.class, property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.agent.realms.internal.RealmMaintenanceJob" )
public class RealmMaintenanceJob implements TriggeredJob {

    private static final Logger log = Logger.getLogger(RealmMaintenanceJob.class);

    private RealmsManager realmsManager;


    protected synchronized void setRealmsManager ( RealmsManager rm ) {
        this.realmsManager = rm;
    }


    protected synchronized void unsetRealmsManager ( RealmsManager rm ) {
        if ( this.realmsManager == rm ) {
            this.realmsManager = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.startNow().withSchedule(SimpleScheduleBuilder.repeatHourlyForever()).build();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public synchronized void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        if ( this.realmsManager == null ) {
            return;
        }

        log.debug("Running realm maintenance"); //$NON-NLS-1$
        this.realmsManager.runMaintenance();
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.server.sysinfo.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
@DisallowConcurrentExecution
@Component ( service = TriggeredJob.class, property = JobProperties.JOB_TYPE
        + "=eu.agno3.orchestrator.agent.server.sysinfo.internal.UpdateSystemInformationJob" )
public class UpdateSystemInformationJob implements TriggeredJob {

    private AgentSystemInformationTracker sysInfoTracker;


    @Reference
    protected synchronized void setAgentSystemInfoTracker ( AgentSystemInformationTracker tracker ) {
        this.sysInfoTracker = tracker;
    }


    protected synchronized void unsetAgentSystemInfoTracker ( AgentSystemInformationTracker tracker ) {
        if ( this.sysInfoTracker == null ) {
            this.sysInfoTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        this.sysInfoTracker.refreshAll();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(300)).build();
    }

}

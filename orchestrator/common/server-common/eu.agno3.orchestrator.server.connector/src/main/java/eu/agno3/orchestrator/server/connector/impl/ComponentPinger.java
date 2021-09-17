/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
public class ComponentPinger implements TriggeredJob {

    private int pingInterval;
    private ServerConnector<?> connector;


    /**
     * @param connector
     * @param pingInterval
     */
    public ComponentPinger ( ServerConnector<?> connector, int pingInterval ) {
        this.connector = connector;
        this.pingInterval = pingInterval;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        if ( this.connector.getState() == ServerConnectorState.CONNECTED ) {
            this.connector.sendPing();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(this.pingInterval)).build();
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * @param <TConfig>
 * 
 */
public abstract class AbstractConnectionMonitor <TConfig extends ComponentConfig> implements TriggeredJob {

    private static final Logger log = Logger.getLogger(AbstractConnectionMonitor.class);
    private ServerConnector<TConfig> connector;


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setServerConnector ( ServerConnector<TConfig> c ) {
        this.connector = c;
    }


    protected synchronized void unsetServerConnector ( ServerConnector<TConfig> c ) {
        if ( this.connector == c ) {
            this.connector = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext context ) throws JobExecutionException {
        if ( this.connector != null ) {
            ServerConnectorState state = this.connector.getState();
            if ( state == ServerConnectorState.ERROR ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Trying to reconnect server connection, reason " + state); //$NON-NLS-1$
                }
                if ( !this.connector.tryConnect() ) {
                    log.debug("Failed to reconnect to server"); //$NON-NLS-1$
                }
                else {
                    log.debug("Reconnected to server"); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever()).build();
    }

}
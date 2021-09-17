/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.orchestrator.server.component.ComponentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 * @param <T>
 */
public abstract class AbstractConnectorWatcherJob <T extends ComponentConfig> implements TriggeredJob {

    private static final Logger log = Logger.getLogger(AbstractConnectorWatcherJob.class);
    private ComponentConfigurationProvider<T> configProvider;
    private AbstractComponentConnectorWatcher<T> watcher;


    /**
     * 
     */
    public AbstractConnectorWatcherJob () {
        super();
    }


    protected void setComponentConnectorWatcher ( AbstractComponentConnectorWatcher<T> w ) {
        this.watcher = w;
    }


    protected void unsetComponentConnectorWatcher ( AbstractComponentConnectorWatcher<T> w ) {
        if ( this.watcher == w ) {
            this.watcher = null;
        }
    }


    protected void setConfigurationProvider ( ComponentConfigurationProvider<T> provider ) {
        this.configProvider = provider;
    }


    protected void unsetConfigurationProvider ( ComponentConfigurationProvider<T> provider ) {
        if ( this.configProvider == provider ) {
            this.configProvider = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public synchronized void execute ( JobExecutionContext context ) throws JobExecutionException {

        DateTime now = new DateTime();

        for ( UUID componentId : this.watcher.getActiveComponentIds() ) {
            try {
                checkComponent(now, componentId);
            }
            catch ( Exception e ) {
                // catch all, otherwise the job will not be executed anymore
                log.warn("Failed to check component state " + componentId, e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param now
     * @param componentId
     */
    protected void checkComponent ( DateTime now, @NonNull UUID componentId ) {
        if ( this.watcher.getComponentConnectorState(componentId) == ComponentState.CONNECTED
                || this.watcher.getComponentConnectorState(componentId) == ComponentState.CONNECTING ) {
            DateTime lastPing = this.watcher.getLastPing(componentId);
            int seconds = Seconds.secondsBetween(lastPing, now).getSeconds();

            try {
                if ( seconds > this.configProvider.getConfiguration(componentId).getPingTimeout() ) {
                    // component timeout
                    this.watcher.timeout(componentId);
                }
            }
            catch ( ComponentConfigurationException e ) {
                log.error("Failed to get component configuration:", e); //$NON-NLS-1$
                this.watcher.timeout(componentId);
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
        return trigger.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(30)).build();
    }

}
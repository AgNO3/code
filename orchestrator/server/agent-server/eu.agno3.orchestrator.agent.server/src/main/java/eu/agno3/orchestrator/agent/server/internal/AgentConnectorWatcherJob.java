/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.server.base.component.AbstractConnectorWatcherJob;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
@DisallowConcurrentExecution
@Component ( service = TriggeredJob.class, property = JobProperties.JOB_TYPE
        + "=eu.agno3.orchestrator.agent.server.internal.AgentConnectorWatcherJob" )
public class AgentConnectorWatcherJob extends AbstractConnectorWatcherJob<AgentConfig> {

    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcherImpl w ) {
        setComponentConnectorWatcher(w);
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcherImpl w ) {
        unsetComponentConnectorWatcher(w);
    }


    @Reference
    protected synchronized void setAgentConfigProvider ( AgentConfigurationProvider provider ) {
        setConfigurationProvider(provider);
    }


    protected synchronized void unsetAgentConfigProvider ( AgentConfigurationProvider provider ) {
        unsetConfigurationProvider(provider);
    }

}

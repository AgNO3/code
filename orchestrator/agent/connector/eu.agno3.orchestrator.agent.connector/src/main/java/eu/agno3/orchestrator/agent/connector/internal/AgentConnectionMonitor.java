/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.quartz.DisallowConcurrentExecution;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.server.connector.impl.AbstractConnectionMonitor;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
@DisallowConcurrentExecution
@Component ( service = TriggeredJob.class, property = "jobType=eu.agno3.orchestrator.agent.connector.internal.AgentConnectionMonitor" )
public class AgentConnectionMonitor extends AbstractConnectionMonitor<AgentConfig> {

    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setServerConnector ( AgentServerConnector c ) {
        super.setServerConnector(c);
    }


    protected synchronized void unsetServerConnector ( AgentServerConnector c ) {
        super.unsetServerConnector(c);
    }

}

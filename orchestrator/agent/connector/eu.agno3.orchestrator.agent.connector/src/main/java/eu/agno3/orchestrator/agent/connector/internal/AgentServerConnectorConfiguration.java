/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.orchestrator.agent.connector.AgentConfiguration;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfigurationImpl;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;


/**
 * @author mbechler
 *
 */

/**
 * Configuration PID
 */
@Component ( service = {
    ServerConnectorConfiguration.class, AgentConfiguration.class
}, configurationPid = "agent.connector", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AgentServerConnectorConfiguration extends ServerConnectorConfigurationImpl implements AgentConfiguration {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.ServerConnectorConfigurationImpl#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected synchronized void activate ( ComponentContext context ) throws ServerConnectorException {
        super.activate(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.ServerConnectorConfigurationImpl#modified(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Modified
    protected synchronized void modified ( ComponentContext context ) throws ServerConnectorException {
        super.modified(context);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.connector.AgentConfiguration#getAgentId()
     */
    @Override
    public UUID getAgentId () {
        return this.getComponentId();
    }
}

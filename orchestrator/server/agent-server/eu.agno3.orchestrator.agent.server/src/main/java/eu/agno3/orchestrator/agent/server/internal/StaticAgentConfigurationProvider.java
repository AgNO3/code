/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.UUID;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.config.AgentConfigImpl;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.runtime.ws.server.WebserviceEndpointInfo;


/**
 * @author mbechler
 * 
 */
@Component ( service = AgentConfigurationProvider.class )
public class StaticAgentConfigurationProvider implements AgentConfigurationProvider {

    private static final int DEFAULT_PING_TIMEOUT = 30;

    private int pingTimeout = DEFAULT_PING_TIMEOUT;

    private WebserviceEndpointInfo webserviceInfo;


    @Reference
    protected synchronized void setWebServiceInfo ( WebserviceEndpointInfo wsInfo ) {
        this.webserviceInfo = wsInfo;
    }


    protected synchronized void unsetWebServiceInfo ( WebserviceEndpointInfo wsInfo ) {
        if ( this.webserviceInfo == wsInfo ) {
            this.webserviceInfo = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ComponentConfigurationException
     * 
     * @see eu.agno3.orchestrator.agent.server.AgentConfigurationProvider#getConfiguration(java.util.UUID)
     */
    @Override
    public AgentConfig getConfiguration ( @NonNull UUID agentId ) throws ComponentConfigurationException {
        AgentConfigImpl config = new AgentConfigImpl();

        config.setId(agentId);
        config.setPingTimeout(this.pingTimeout);

        try {
            config.setWebServiceBaseAddress(this.webserviceInfo.getBaseAddress());
        }
        catch ( ServletException e ) {
            throw new ComponentConfigurationException("Illegal webservice endpoint URL:", e); //$NON-NLS-1$
        }

        config.setEventOutQueue(String.format("agents-%s-in", agentId.toString())); //$NON-NLS-1$
        config.setEventTopic(String.format("agents-%s-out", agentId.toString())); //$NON-NLS-1$
        config.setRequestQueuePrefix(String.format("agent-%s/", agentId.toString())); //$NON-NLS-1$

        return config;
    }

}

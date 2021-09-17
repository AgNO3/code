/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.config;


import java.net.URI;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( AgentConfig.class )
public class AgentConfigImpl implements AgentConfig {

    private UUID agentId;
    private int pingTimeout;
    private URI wsBaseAddress;

    private String eventOutQueue;
    private String eventTopic;
    private String requestQueuePrefix;


    /**
     * 
     */
    public AgentConfigImpl () {}


    @Override
    public UUID getId () {
        return this.agentId;
    }


    @Override
    public int getPingTimeout () {
        return this.pingTimeout;
    }


    @Override
    public URI getWebServiceBaseAddress () {
        return this.wsBaseAddress;
    }


    /**
     * 
     * @param agentId
     */
    public void setId ( @NonNull UUID agentId ) {
        this.agentId = agentId;
    }


    /**
     * 
     * @param pingTimeout
     */
    public void setPingTimeout ( int pingTimeout ) {
        this.pingTimeout = pingTimeout;
    }


    /**
     * 
     * @param wsBaseAddress
     */
    public void setWebServiceBaseAddress ( URI wsBaseAddress ) {
        this.wsBaseAddress = wsBaseAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.config.AgentConfig#getEventOutQueue()
     */
    @Override
    public String getEventOutQueue () {
        return this.eventOutQueue;
    }


    /**
     * 
     * @param queue
     */
    public void setEventOutQueue ( String queue ) {
        this.eventOutQueue = queue;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.config.AgentConfig#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return this.eventTopic;
    }


    /**
     * 
     * @param topic
     */
    public void setEventTopic ( String topic ) {
        this.eventTopic = topic;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.config.AgentConfig#getRequestQueuePrefix()
     */
    @Override
    public String getRequestQueuePrefix () {
        return this.requestQueuePrefix;
    }


    /**
     * 
     * @param prefix
     */
    public void setRequestQueuePrefix ( String prefix ) {
        this.requestQueuePrefix = prefix;
    }

}

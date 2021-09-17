/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.config;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.msg.ComponentConfigResponse;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class AgentConfigResponse extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements
        ComponentConfigResponse<@NonNull AgentConfig> {

    @NonNull
    private AgentConfig agentConfiguration;


    /**
     * 
     */
    public AgentConfigResponse () {
        this.agentConfiguration = new AgentConfigImpl();
    }


    /**
     * @param config
     * @param origin
     * @param replyTo
     */
    public AgentConfigResponse ( @NonNull AgentConfig config, @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.agentConfiguration = config;
    }


    /**
     * @param config
     * @param origin
     */
    public AgentConfigResponse ( @NonNull AgentConfig config, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.agentConfiguration = config;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }


    /**
     * @return the agentConfiguration
     */
    @Override
    public @NonNull AgentConfig getConfiguration () {
        return this.agentConfiguration;
    }


    /**
     * @param agentConfiguration
     *            the agentConfiguration to set
     */
    protected void setConfiguration ( @NonNull AgentConfig agentConfiguration ) {
        this.agentConfiguration = agentConfiguration;
    }

}

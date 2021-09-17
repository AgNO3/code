/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg;


import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.server.component.msg.AbstractComponentPingMessage;
import eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageTarget;


/**
 * @author mbechler
 * 
 */
public class AgentPingMessage extends AbstractComponentPingMessage<@NonNull AgentMessageSource> {

    /**
     * 
     */
    public AgentPingMessage () {
        super();
    }


    /**
     * @param pingTime
     * @param origin
     */
    public AgentPingMessage ( Date pingTime, @NonNull AgentMessageSource origin ) {
        super(origin);
        this.getProperties().put(PING_TIME, pingTime.toString());
    }


    /**
     * @param s
     * @param ttl
     * 
     */
    public AgentPingMessage ( @NonNull AgentMessageSource s, int ttl ) {
        super(s, ttl);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new JMSQueueMessageTarget("agents-ping"); //$NON-NLS-1$
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.connector;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 *
 */
public interface QueueingEventProducer {

    /**
     * @param ev
     */
    void publish ( @NonNull EventMessage<? extends @NonNull MessageSource> ev );


    /**
     * @return the message source
     */
    @NonNull
    AgentMessageSource getMessageSource ();

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.msg;


import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = EventMessage.class )
public class SystemRevertedEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    private long revertedToSequence;
    private String revertedToStream;


    /**
     * 
     */
    public SystemRevertedEvent () {
        super();
    }


    /**
     * 
     * @param origin
     * @param ttl
     */
    public SystemRevertedEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * 
     * @param origin
     * @param replyTo
     */
    public SystemRevertedEvent ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * 
     * @param origin
     */
    public SystemRevertedEvent ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Arrays.asList(new ServersEventScope());
    }


    /**
     * @return the revertedToSequence
     */
    public long getRevertedToSequence () {
        return this.revertedToSequence;
    }


    /**
     * @param revertedToSequence
     *            the revertedToSequence to set
     */
    public void setRevertedToSequence ( long revertedToSequence ) {
        this.revertedToSequence = revertedToSequence;
    }


    /**
     * @return the revertedToStream
     */
    public String getRevertedToStream () {
        return this.revertedToStream;
    }


    /**
     * @param revertedToStream
     *            the revertedToStream to set
     */
    public void setRevertedToStream ( String revertedToStream ) {
        this.revertedToStream = revertedToStream;
    }
}

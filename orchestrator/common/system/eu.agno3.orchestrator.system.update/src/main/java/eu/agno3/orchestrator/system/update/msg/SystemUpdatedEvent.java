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
public class SystemUpdatedEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    private long updatedSequence;
    private String updatedStream;
    private boolean rebootIndicated;


    /**
     * 
     */
    public SystemUpdatedEvent () {
        super();
    }


    /**
     * 
     * @param origin
     * @param ttl
     */
    public SystemUpdatedEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * 
     * @param origin
     * @param replyTo
     */
    public SystemUpdatedEvent ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * 
     * @param origin
     */
    public SystemUpdatedEvent ( @NonNull AgentMessageSource origin ) {
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
     * @return the updatedSequence
     */
    public long getUpdatedSequence () {
        return this.updatedSequence;
    }


    /**
     * @param updatedSequence
     *            the updatedSequence to set
     */
    public void setUpdatedSequence ( long updatedSequence ) {
        this.updatedSequence = updatedSequence;
    }


    /**
     * @return the updatedStream
     */
    public String getUpdatedStream () {
        return this.updatedStream;
    }


    /**
     * @param updatedStream
     *            the updatedStream to set
     */
    public void setUpdatedStream ( String updatedStream ) {
        this.updatedStream = updatedStream;
    }


    /**
     * @return the rebootIndicated
     */
    public boolean getRebootIndicated () {
        return this.rebootIndicated;
    }


    /**
     * @param rebootIndicated
     *            the rebootIndicated to set
     */
    public void setRebootIndicated ( boolean rebootIndicated ) {
        this.rebootIndicated = rebootIndicated;
    }
}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.events;


import java.util.Collection;
import java.util.Collections;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
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
public class ConfigTestResultUpdateEvent extends XmlMarshallableMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    private ConfigTestResultImpl result;
    private long sequence;


    /**
     * 
     */
    public ConfigTestResultUpdateEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ConfigTestResultUpdateEvent ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ConfigTestResultUpdateEvent ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ConfigTestResultUpdateEvent ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Collections.singleton(new ServersEventScope());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryMode()
     */
    @Override
    public int getDeliveryMode () {
        return DeliveryMode.NON_PERSISTENT;
    }


    /**
     * @return the result
     */
    public ConfigTestResultImpl getResult () {
        return this.result;
    }


    /**
     * @param result
     *            the result to set
     */
    public void setResult ( ConfigTestResultImpl result ) {
        this.result = result;
    }


    /**
     * @return the sequence
     */
    public long getSequence () {
        return this.sequence;
    }


    /**
     * @param sequence
     *            the sequence to set
     */
    public void setSequence ( long sequence ) {
        this.sequence = sequence;
    }

}

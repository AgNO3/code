/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * @param <TSource>
 * 
 */
public abstract class AbstractComponentPingMessage <@NonNull TSource extends MessageSource> extends EmptyMessage<TSource>
        implements ComponentPingRequest<TSource> {

    protected static final String PING_TIME = "pingTime"; //$NON-NLS-1$
    private static final long TIMEOUT = 5000;


    /**
     * 
     */
    public AbstractComponentPingMessage () {
        super();
    }


    /**
     * @param origin
     */
    public AbstractComponentPingMessage ( @NonNull TSource origin ) {
        super(origin);
    }


    /**
     * @param origin
     * @param ttl
     */
    public AbstractComponentPingMessage ( @NonNull TSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public AbstractComponentPingMessage ( @NonNull TSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @return the pingTime
     */
    public DateTime getPingTime () {
        String pingTime = (String) this.getProperties().get(PING_TIME);

        if ( pingTime == null ) {
            return null;
        }

        return new DateTime(pingTime);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<ComponentConnStateFailureMessage> getErrorResponseType () {
        return ComponentConnStateFailureMessage.class;
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
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return TIMEOUT;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<ComponentPongMessage> getResponseType () {
        return ComponentPongMessage.class;
    }

}
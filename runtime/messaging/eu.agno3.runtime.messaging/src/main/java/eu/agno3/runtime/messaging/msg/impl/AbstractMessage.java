/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractMessage <@NonNull T extends MessageSource> implements Message<T> {

    protected static final int DEFAULT_TTL = 255;
    private transient int ttl;
    private transient Optional<@NonNull T> origin = Optional.empty();
    private transient String senderUserId;


    /**
     * 
     */
    public AbstractMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public AbstractMessage ( @NonNull T origin, int ttl ) {
        this.ttl = ttl;
        this.origin = Optional.of(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.Message#getOrigin()
     */
    @Override
    public @NonNull T getOrigin () {
        return this.origin.get();
    }


    /**
     * @param origin
     *            the origin to set
     */
    public void setOrigin ( @NonNull T origin ) {
        this.origin = Optional.of(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.Message#getTTL()
     */
    @Override
    public int getTTL () {
        return this.ttl;
    }


    /**
     * @param ttl
     *            the ttl to set
     */
    public void setTtl ( int ttl ) {
        this.ttl = ttl;
    }


    /**
     * @param userId
     */
    public void setSenderUser ( String userId ) {
        this.senderUserId = userId;
    }


    /**
     * @return the senderUserId
     */
    public String getSenderUserId () {
        return this.senderUserId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.Message#hasResponse()
     */
    @Override
    public boolean hasResponse () {
        return true;
    }


    /**
     * @return the deliveryMode
     */
    @Override
    public int getDeliveryMode () {
        return javax.jms.Message.DEFAULT_DELIVERY_MODE;
    }


    /**
     * @return the deliveryPriority
     */
    @Override
    public int getDeliveryPriority () {
        return javax.jms.Message.DEFAULT_PRIORITY;
    }


    /**
     * @return the deliveryTTL
     */
    @Override
    public long getDeliveryTTL () {
        return javax.jms.Message.DEFAULT_TIME_TO_LIVE;
    }

}
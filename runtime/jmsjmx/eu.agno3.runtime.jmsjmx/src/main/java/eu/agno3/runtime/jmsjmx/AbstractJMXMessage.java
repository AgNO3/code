/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.AbstractMessage;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class AbstractJMXMessage <@NonNull T extends MessageSource> extends AbstractMessage<T> {

    /**
     * 
     */
    public AbstractJMXMessage () {
        super();
    }


    protected AbstractJMXMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    protected AbstractJMXMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        this(origin, replyTo.getTTL() - 1);
    }


    protected AbstractJMXMessage ( @NonNull T origin ) {
        this(origin, DEFAULT_TTL);
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
}

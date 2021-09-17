/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.UUID;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class JobKeepAliveEvent extends JobEvent<@NonNull MessageSource> {

    /**
     * 
     */
    public JobKeepAliveEvent () {
        super();
    }


    /**
     * @param jobId
     * @param origin
     * @param ttl
     */
    public JobKeepAliveEvent ( UUID jobId, @NonNull MessageSource origin, int ttl ) {
        super(jobId, origin, ttl);
    }


    /**
     * @param jobId
     * @param origin
     * @param replyTo
     */
    public JobKeepAliveEvent ( UUID jobId, @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(jobId, origin, replyTo);
    }


    /**
     * @param jobId
     * @param origin
     */
    public JobKeepAliveEvent ( UUID jobId, @NonNull MessageSource origin ) {
        super(jobId, origin);
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

/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.test.messages.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.msg.impl.AckMessage;
import eu.agno3.runtime.messaging.msg.impl.DefaultErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
public class RedeliveredTestRequestMessage extends EmptyMessage<@NonNull MessageSource>
        implements RequestMessage<@NonNull MessageSource, AckMessage, DefaultErrorResponseMessage> {

    /**
     * 
     */
    private static final String REQUEST_STATUS = "requestStatus"; //$NON-NLS-1$


    /**
     * 
     */
    public RedeliveredTestRequestMessage () {}


    /**
     * @param origin
     */
    public RedeliveredTestRequestMessage ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<AckMessage> getResponseType () {
        return AckMessage.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultErrorResponseMessage> getErrorResponseType () {
        return DefaultErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new JMSQueueMessageTarget("msgs/redelivery"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 10000;
    }


    /**
     * @return the requestStatus
     */
    public ResponseStatus getRequestStatus () {
        String respStatus = (String) this.getProperties().get(REQUEST_STATUS);
        if ( respStatus == null ) {
            return null;
        }
        return ResponseStatus.valueOf(respStatus);
    }


    /**
     * @param requestStatus
     *            the requestStatus to set
     */
    public void setRequestStatus ( ResponseStatus requestStatus ) {
        if ( requestStatus != null ) {
            this.getProperties().put(REQUEST_STATUS, requestStatus.name());
        }
        else {
            this.getProperties().remove(REQUEST_STATUS);
        }
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 18, 2017 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import javax.jms.TemporaryQueue;


/**
 * @author mbechler
 *
 */
public class BaseMessageContext implements MessageContext {

    private TemporaryQueue replyQueue;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.internal.MessageContext#setReplyQueue(javax.jms.TemporaryQueue)
     */
    @Override
    public void setReplyQueue ( TemporaryQueue replyQueue ) {
        this.replyQueue = replyQueue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.internal.MessageContext#getReplyQueue()
     */
    @Override
    public TemporaryQueue getReplyQueue () {
        return this.replyQueue;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import javax.jms.TemporaryQueue;


/**
 * @author mbechler
 *
 */
public interface MessageContext {

    /**
     * @param replyQueue
     */
    void setReplyQueue ( TemporaryQueue replyQueue );


    /**
     * @return the reply queue
     */
    TemporaryQueue getReplyQueue ();

}

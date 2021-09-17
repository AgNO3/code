/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface Message <@NonNull T extends MessageSource> {

    /**
     * 
     * @return the message source
     */
    @NonNull
    T getOrigin ();


    /**
     * Time to live
     * 
     * Messages created upon reception of other messages must decrement this value by one. When this value reaches zero
     * an error must be raised.
     * 
     * @return time to live
     */
    int getTTL ();


    /**
     * 
     * @return whether this message type has a response
     */
    boolean hasResponse ();


    /**
     * 
     * @return the JMS delivery mode
     */
    int getDeliveryMode ();


    /**
     * 
     * @return the JMS delivery priority
     */
    int getDeliveryPriority ();


    /**
     * 
     * @return the JMS delivery TTL
     */
    long getDeliveryTTL ();
}

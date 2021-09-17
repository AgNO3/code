/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class EmptyMessage <@NonNull T extends MessageSource> extends AbstractRawMessage<T> {

    /**
     * @param origin
     * @param ttl
     */
    public EmptyMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public EmptyMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public EmptyMessage ( @NonNull T origin ) {
        super(origin);
    }


    /**
     * 
     */
    public EmptyMessage () {
        super();
    }

}

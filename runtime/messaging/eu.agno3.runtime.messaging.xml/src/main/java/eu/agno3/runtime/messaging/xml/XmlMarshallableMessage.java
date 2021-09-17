/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.AbstractMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class XmlMarshallableMessage <@NonNull T extends MessageSource> extends AbstractMessage<T> {

    /**
     * 
     */
    public XmlMarshallableMessage () {
        super();
    }


    protected XmlMarshallableMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    protected XmlMarshallableMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        this(origin, replyTo.getTTL() - 1);
    }


    protected XmlMarshallableMessage ( @NonNull T origin ) {
        this(origin, DEFAULT_TTL);
    }

}

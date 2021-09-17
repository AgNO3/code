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
public class TextMessage <@NonNull T extends MessageSource> extends AbstractRawMessage<T> {

    private String text;


    /**
     * 
     */
    public TextMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public TextMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public TextMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public TextMessage ( @NonNull T origin ) {
        super(origin);
    }


    /**
     * @return the text
     */
    public String getText () {
        return this.text;
    }


    /**
     * @param text
     *            the text to set
     */
    public void setText ( String text ) {
        this.text = text;
    }
}

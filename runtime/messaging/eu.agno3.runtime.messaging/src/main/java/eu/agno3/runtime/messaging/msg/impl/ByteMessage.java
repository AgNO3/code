/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class ByteMessage <@NonNull T extends MessageSource> extends AbstractRawMessage<T> {

    private byte[] bytes;


    /**
     * 
     */
    public ByteMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ByteMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ByteMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ByteMessage ( @NonNull T origin ) {
        super(origin);
    }


    /**
     * @return the bytes
     */
    public byte[] getBytes () {
        if ( this.bytes != null ) {
            return Arrays.copyOf(this.bytes, this.bytes.length);
        }
        return new byte[] {};
    }


    /**
     * @param bytes
     *            the bytes to set
     */
    public void setBytes ( byte[] bytes ) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }
}

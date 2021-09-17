/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 * 
 */
public class DefaultErrorResponseMessage extends TextMessage<@NonNull MessageSource> implements ErrorResponseMessage<@NonNull MessageSource> {

    /**
     * 
     */
    public DefaultErrorResponseMessage () {}


    /**
     * @param msg
     * @param origin
     * @param replyTo
     */
    public DefaultErrorResponseMessage ( String msg, @NonNull MessageSource origin, RequestMessage<@NonNull MessageSource, ?, ?> replyTo ) {
        super(origin, replyTo);
        setText(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ErrorResponseMessage#getThrowable()
     */
    @Override
    public Throwable getThrowable () {
        return new Exception(getText());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.ERROR;
    }

}

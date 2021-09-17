/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 * 
 */
public class DefaultXmlErrorResponseMessage extends XmlMarshallableMessage<@NonNull MessageSource>
        implements ErrorResponseMessage<@NonNull MessageSource> {

    private Throwable throwable;


    /**
     * @param exception
     * @param origin
     * @param replyTo
     */
    public DefaultXmlErrorResponseMessage ( Throwable exception, @NonNull MessageSource origin,
            RequestMessage<@NonNull ? extends MessageSource, ?, ?> replyTo ) {
        super(origin, replyTo);
        this.throwable = exception;
    }


    /**
     * 
     */
    public DefaultXmlErrorResponseMessage () {
        super();
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


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ErrorResponseMessage#getThrowable()
     */
    @Override
    public Throwable getThrowable () {
        return this.throwable;
    }

}

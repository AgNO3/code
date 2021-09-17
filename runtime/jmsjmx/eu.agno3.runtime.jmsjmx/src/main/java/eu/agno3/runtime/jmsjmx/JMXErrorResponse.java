/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 *
 */
public class JMXErrorResponse extends AbstractJMXMessage<@NonNull MessageSource> implements ErrorResponseMessage<@NonNull MessageSource> {

    private Throwable throwable;


    /**
     * 
     */
    public JMXErrorResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public JMXErrorResponse ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public JMXErrorResponse ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public JMXErrorResponse ( @NonNull MessageSource origin ) {
        super(origin);
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


    /**
     * @param throwable
     *            the throwable to set
     */
    public void setThrowable ( Throwable throwable ) {
        this.throwable = throwable;
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 *
 */
public class JMXResponse extends AbstractJMXMessage<@NonNull MessageSource> implements ResponseMessage<@NonNull MessageSource> {

    private Object responseObject;


    /**
     * 
     */
    public JMXResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public JMXResponse ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public JMXResponse ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public JMXResponse ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * @return the reponseObject
     */
    public Object getResponseObject () {
        return this.responseObject;
    }


    /**
     * @param responseObject
     *            the reponseObject to set
     */
    public void setResponseObject ( Object responseObject ) {
        this.responseObject = responseObject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }

}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class ConfigTestResultUpdateResponse extends XmlMarshallableMessage<@NonNull ServerMessageSource>
        implements ResponseMessage<@NonNull ServerMessageSource> {

    /**
     * 
     */
    public ConfigTestResultUpdateResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ConfigTestResultUpdateResponse ( @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ConfigTestResultUpdateResponse ( @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ConfigTestResultUpdateResponse ( @NonNull ServerMessageSource origin ) {
        super(origin);
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

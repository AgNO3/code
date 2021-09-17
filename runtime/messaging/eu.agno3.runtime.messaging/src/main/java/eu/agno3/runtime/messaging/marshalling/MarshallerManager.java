/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * 
 */
public interface MarshallerManager {

    /**
     * @param msgType
     * @return the active marshaller for this type
     * @throws MarshallingException
     */
    <TMsg extends Message<@NonNull ? extends MessageSource>> @NonNull MessageMarshaller<TMsg> getMarshaller ( Class<TMsg> msgType )
            throws MarshallingException;

}

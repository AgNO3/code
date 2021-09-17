/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import javax.jms.Message;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface MessageUnmarshaller <T> {

    /**
     * @param msg
     * @param targetClassLoader
     * @param request
     * @return a marshalled message
     * @throws MarshallingException
     */
    T unmarshall ( Message msg, ClassLoader targetClassLoader, eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource> request )
            throws MarshallingException;

}

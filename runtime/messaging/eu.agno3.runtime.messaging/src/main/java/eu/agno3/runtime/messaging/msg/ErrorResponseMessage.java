/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface ErrorResponseMessage <@NonNull T extends MessageSource> extends ResponseMessage<T> {

    /**
     * @return a corresponding throwable for this error
     */
    Throwable getThrowable ();
}

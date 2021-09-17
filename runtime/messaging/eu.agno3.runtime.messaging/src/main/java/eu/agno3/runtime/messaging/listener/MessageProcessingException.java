/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;


/**
 * @author mbechler
 * 
 */
public class MessageProcessingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6416618625300348969L;
    private final ErrorResponseMessage<@NonNull ? extends MessageSource> errorMsg;


    /**
     * @param errorMessage
     */
    public MessageProcessingException ( ErrorResponseMessage<@NonNull ? extends MessageSource> errorMessage ) {
        super("Message processing failed"); //$NON-NLS-1$
        this.errorMsg = errorMessage;
    }


    /**
     * @return the error response to respond with
     */
    public ErrorResponseMessage<@NonNull ? extends MessageSource> getErrorResponse () {
        return this.errorMsg;
    }
}

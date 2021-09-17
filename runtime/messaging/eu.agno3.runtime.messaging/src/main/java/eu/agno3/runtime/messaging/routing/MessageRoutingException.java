/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing;


import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 * 
 */
public class MessageRoutingException extends MessagingException {

    /**
     * 
     */
    private static final long serialVersionUID = -7654888472261141570L;


    /**
     * 
     */
    public MessageRoutingException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public MessageRoutingException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public MessageRoutingException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public MessageRoutingException ( Throwable cause ) {
        super(cause);
    }

}

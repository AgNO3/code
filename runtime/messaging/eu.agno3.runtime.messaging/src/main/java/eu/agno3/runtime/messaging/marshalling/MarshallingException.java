/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 * 
 */
public class MarshallingException extends MessagingException {

    /**
     * 
     */
    public MarshallingException () {
        super();
    }


    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public MarshallingException ( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    /**
     * @param message
     * @param cause
     */
    public MarshallingException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public MarshallingException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public MarshallingException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2259146338338079306L;

}

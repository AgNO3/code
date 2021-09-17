/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2016 by mbechler
 */
package eu.agno3.runtime.messaging;


/**
 * @author mbechler
 *
 */
public class InvalidSessionException extends MessagingException {

    /**
     * 
     */
    private static final long serialVersionUID = -5739471600173343240L;


    /**
     * 
     */
    public InvalidSessionException () {
        super();
    }


    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public InvalidSessionException ( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    /**
     * @param message
     * @param cause
     */
    public InvalidSessionException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public InvalidSessionException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public InvalidSessionException ( Throwable cause ) {
        super(cause);
    }

}

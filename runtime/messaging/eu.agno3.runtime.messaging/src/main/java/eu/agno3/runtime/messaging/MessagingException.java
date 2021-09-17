/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging;


/**
 * @author mbechler
 * 
 */
public class MessagingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2667661701278284826L;


    /**
     * 
     */
    public MessagingException () {
        super();
    }


    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public MessagingException ( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    /**
     * @param message
     * @param cause
     */
    public MessagingException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public MessagingException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public MessagingException ( Throwable cause ) {
        super(cause);
    }

}

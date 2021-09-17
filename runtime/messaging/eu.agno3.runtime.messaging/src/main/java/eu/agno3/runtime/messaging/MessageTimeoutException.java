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
public class MessageTimeoutException extends MessagingException {

    /**
     * 
     */
    private static final long serialVersionUID = -3541442270705314423L;


    /**
     * 
     */
    public MessageTimeoutException () {
        super();
    }


    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public MessageTimeoutException ( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    /**
     * @param message
     * @param cause
     */
    public MessageTimeoutException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public MessageTimeoutException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public MessageTimeoutException ( Throwable cause ) {
        super(cause);
    }

}

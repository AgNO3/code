/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2014 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


/**
 * @author mbechler
 * 
 */
public class ConsoleRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -5562701368583820261L;


    /**
     * 
     */
    public ConsoleRuntimeException () {}


    /**
     * @param message
     */
    public ConsoleRuntimeException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ConsoleRuntimeException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public ConsoleRuntimeException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ConsoleRuntimeException ( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

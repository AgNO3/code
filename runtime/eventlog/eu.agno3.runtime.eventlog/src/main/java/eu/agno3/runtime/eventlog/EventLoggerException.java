/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


/**
 * @author mbechler
 *
 */
public class EventLoggerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -902997175643018590L;


    /**
     * 
     */
    public EventLoggerException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public EventLoggerException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public EventLoggerException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public EventLoggerException ( Throwable cause ) {
        super(cause);
    }

}

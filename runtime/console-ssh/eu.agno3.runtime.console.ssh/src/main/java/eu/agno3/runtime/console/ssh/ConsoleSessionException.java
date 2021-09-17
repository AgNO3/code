/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh;


import eu.agno3.runtime.console.ConsoleException;


/**
 * @author mbechler
 * 
 */
public class ConsoleSessionException extends ConsoleException {

    /**
     * 
     */
    private static final long serialVersionUID = 1214551729920366201L;


    /**
     * 
     */
    public ConsoleSessionException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public ConsoleSessionException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ConsoleSessionException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ConsoleSessionException ( Throwable cause ) {
        super(cause);
    }

}

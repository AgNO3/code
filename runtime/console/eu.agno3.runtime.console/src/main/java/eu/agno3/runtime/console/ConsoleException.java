/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.console;


/**
 * @author mbechler
 * 
 */
public class ConsoleException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -8308621093220186653L;


    /**
     * 
     */
    public ConsoleException () {}


    /**
     * @param message
     */
    public ConsoleException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ConsoleException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public ConsoleException ( String message, Throwable cause ) {
        super(message, cause);
    }

}

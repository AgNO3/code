/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logging;


/**
 * @author mbechler
 *
 */
public class InvalidLogEntryException extends Exception {

    /**
     * 
     */
    public InvalidLogEntryException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public InvalidLogEntryException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public InvalidLogEntryException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public InvalidLogEntryException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -4896345832488961791L;

}

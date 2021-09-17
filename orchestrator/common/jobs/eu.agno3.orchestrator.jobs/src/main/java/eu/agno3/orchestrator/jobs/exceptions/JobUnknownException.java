/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.exceptions;


/**
 * @author mbechler
 * 
 */
public class JobUnknownException extends JobQueueException {

    /**
     * 
     */
    private static final long serialVersionUID = -6638189416292042962L;


    /**
     * 
     */
    public JobUnknownException () {}


    /**
     * @param message
     * @param cause
     */
    public JobUnknownException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JobUnknownException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JobUnknownException ( Throwable cause ) {
        super(cause);
    }

}

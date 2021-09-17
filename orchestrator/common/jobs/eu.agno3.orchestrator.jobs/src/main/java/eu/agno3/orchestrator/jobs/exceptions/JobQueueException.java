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
public class JobQueueException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4181955732070748066L;


    /**
     * 
     */
    public JobQueueException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public JobQueueException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JobQueueException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JobQueueException ( Throwable cause ) {
        super(cause);
    }

}

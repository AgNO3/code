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
public class JobIllegalStateException extends JobQueueException {

    /**
     * 
     */
    private static final long serialVersionUID = -5477227514697194367L;


    /**
     * 
     */
    public JobIllegalStateException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public JobIllegalStateException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JobIllegalStateException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JobIllegalStateException ( Throwable cause ) {
        super(cause);
    }

}

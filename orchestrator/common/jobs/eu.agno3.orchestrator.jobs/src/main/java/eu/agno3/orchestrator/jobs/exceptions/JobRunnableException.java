/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.exceptions;


/**
 * @author mbechler
 * 
 */
public class JobRunnableException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4928642491606050332L;


    /**
     * 
     */
    public JobRunnableException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public JobRunnableException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JobRunnableException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JobRunnableException ( Throwable cause ) {
        super(cause);
    }

}

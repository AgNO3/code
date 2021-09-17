/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


/**
 * @author mbechler
 *
 */
public class ConfigRepositoryException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5277911136562220768L;


    /**
     * 
     */
    public ConfigRepositoryException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ConfigRepositoryException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param message
     */
    public ConfigRepositoryException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ConfigRepositoryException ( Throwable cause ) {
        super(cause);
    }

}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup;


/**
 * @author mbechler
 *
 */
public class BackupException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1241921758514026686L;


    /**
     * 
     */
    public BackupException () {
        super();
    }


    /**
     * 
     * @param message
     * @param cause
     */
    public BackupException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * 
     * @param message
     */
    public BackupException ( String message ) {
        super(message);
    }


    /**
     * 
     * @param cause
     */
    public BackupException ( Throwable cause ) {
        super(cause);
    }

}

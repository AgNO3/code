package eu.agno3.orchestrator.system.update;


/**
 * @author mbechler
 *
 */
public class UpdateException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7093999098767159439L;


    /**
     * 
     */
    public UpdateException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public UpdateException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public UpdateException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public UpdateException ( Throwable cause ) {
        super(cause);
    }

}

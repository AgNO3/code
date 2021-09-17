/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * 
 */
public class NoSuchServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4011603716404547143L;


    /**
     * 
     */
    public NoSuchServiceException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public NoSuchServiceException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public NoSuchServiceException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public NoSuchServiceException ( Throwable cause ) {
        super(cause);
    }

}

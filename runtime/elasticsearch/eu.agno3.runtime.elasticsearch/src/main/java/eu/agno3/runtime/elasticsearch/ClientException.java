/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public class ClientException extends Exception {

    /**
     * 
     */
    public ClientException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public ClientException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ClientException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ClientException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3891582525068431762L;

}

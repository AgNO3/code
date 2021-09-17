/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


/**
 * @author mbechler
 *
 */
public class KeystoreManagerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5562510839511985307L;


    /**
     * 
     */
    public KeystoreManagerException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public KeystoreManagerException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public KeystoreManagerException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public KeystoreManagerException ( Throwable cause ) {
        super(cause);
    }

}

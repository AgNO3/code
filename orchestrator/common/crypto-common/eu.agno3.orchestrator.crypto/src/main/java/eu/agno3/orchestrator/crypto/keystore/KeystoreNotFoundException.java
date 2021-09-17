/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2016 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


/**
 * @author mbechler
 *
 */
public class KeystoreNotFoundException extends KeystoreManagerException {

    /**
     * 
     */
    private static final long serialVersionUID = 8684448428258294136L;


    /**
     * 
     */
    public KeystoreNotFoundException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public KeystoreNotFoundException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public KeystoreNotFoundException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public KeystoreNotFoundException ( Throwable cause ) {
        super(cause);
    }

}

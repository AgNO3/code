/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class StorageEntityNotFoundException extends StorageException {

    /**
     * 
     */
    private static final long serialVersionUID = -3474888703063619149L;


    /**
     * 
     */
    public StorageEntityNotFoundException () {}


    /**
     * @param msg
     * @param t
     */
    public StorageEntityNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public StorageEntityNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public StorageEntityNotFoundException ( Throwable cause ) {
        super(cause);
    }

}

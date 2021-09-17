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
public class StorageException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = 2640595940381370037L;


    /**
     * 
     */
    public StorageException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public StorageException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public StorageException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public StorageException ( Throwable cause ) {
        super(cause);
    }

}

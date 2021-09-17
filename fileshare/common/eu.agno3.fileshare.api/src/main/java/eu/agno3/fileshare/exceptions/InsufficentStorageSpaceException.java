/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class InsufficentStorageSpaceException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 992422597691718093L;


    /**
     * 
     */
    public InsufficentStorageSpaceException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public InsufficentStorageSpaceException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InsufficentStorageSpaceException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public InsufficentStorageSpaceException ( Throwable cause ) {
        super(cause);
    }

}

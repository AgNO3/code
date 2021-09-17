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
public class StoreFailedException extends StorageException {

    /**
     * 
     */
    private static final long serialVersionUID = -4407538115387833052L;


    /**
     * 
     */
    public StoreFailedException () {}


    /**
     * @param msg
     * @param t
     */
    public StoreFailedException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public StoreFailedException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public StoreFailedException ( Throwable cause ) {
        super(cause);
    }

}

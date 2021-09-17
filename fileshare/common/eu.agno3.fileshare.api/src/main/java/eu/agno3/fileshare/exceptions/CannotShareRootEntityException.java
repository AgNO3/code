/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class CannotShareRootEntityException extends ShareException {

    /**
     * 
     */
    private static final long serialVersionUID = -3636242574093550131L;


    /**
     * 
     */
    public CannotShareRootEntityException () {}


    /**
     * @param msg
     * @param t
     */
    public CannotShareRootEntityException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public CannotShareRootEntityException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public CannotShareRootEntityException ( Throwable cause ) {
        super(cause);
    }

}

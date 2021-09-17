/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class CannotDeleteCurrentUserException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -1020271813081452700L;


    /**
     * 
     */
    public CannotDeleteCurrentUserException () {}


    /**
     * @param msg
     * @param t
     */
    public CannotDeleteCurrentUserException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public CannotDeleteCurrentUserException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public CannotDeleteCurrentUserException ( Throwable cause ) {
        super(cause);
    }

}

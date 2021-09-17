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
public class InvalidPasswordException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -9111148510903933160L;


    /**
     * 
     */
    public InvalidPasswordException () {}


    /**
     * @param msg
     * @param t
     */
    public InvalidPasswordException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InvalidPasswordException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public InvalidPasswordException ( Throwable cause ) {
        super(cause);
    }

}

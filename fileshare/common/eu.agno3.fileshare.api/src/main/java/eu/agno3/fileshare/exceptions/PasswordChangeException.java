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
public class PasswordChangeException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -6620882329598643465L;


    /**
     * 
     */
    public PasswordChangeException () {}


    /**
     * @param msg
     * @param t
     */
    public PasswordChangeException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public PasswordChangeException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public PasswordChangeException ( Throwable cause ) {
        super(cause);
    }

}

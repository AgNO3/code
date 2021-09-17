/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class PasswordInMessageException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 3465617488824844630L;


    /**
     * 
     */
    public PasswordInMessageException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public PasswordInMessageException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public PasswordInMessageException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public PasswordInMessageException ( Throwable cause ) {
        super(cause);
    }

}

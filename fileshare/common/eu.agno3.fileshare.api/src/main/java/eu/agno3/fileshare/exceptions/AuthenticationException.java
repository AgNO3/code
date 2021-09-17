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
public class AuthenticationException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -2505507069365300618L;


    /**
     * 
     */
    public AuthenticationException () {}


    /**
     * @param msg
     * @param t
     */
    public AuthenticationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AuthenticationException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public AuthenticationException ( Throwable cause ) {
        super(cause);
    }

}

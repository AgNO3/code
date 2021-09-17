/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class TokenValidationException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 4986510728797769077L;


    /**
     * 
     */
    public TokenValidationException () {}


    /**
     * @param msg
     * @param t
     */
    public TokenValidationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public TokenValidationException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public TokenValidationException ( Throwable cause ) {
        super(cause);
    }

}

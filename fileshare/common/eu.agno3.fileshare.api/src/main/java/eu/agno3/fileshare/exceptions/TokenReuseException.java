/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class TokenReuseException extends TokenValidationException {

    /**
     * 
     */
    private static final long serialVersionUID = -8213698763305577385L;


    /**
     * 
     */
    public TokenReuseException () {}


    /**
     * @param msg
     * @param t
     */
    public TokenReuseException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public TokenReuseException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public TokenReuseException ( Throwable cause ) {
        super(cause);
    }

}

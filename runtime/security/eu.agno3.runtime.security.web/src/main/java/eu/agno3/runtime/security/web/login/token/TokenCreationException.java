/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login.token;


/**
 * @author mbechler
 *
 */
public class TokenCreationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -613626945018810349L;


    /**
     * 
     */
    public TokenCreationException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public TokenCreationException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public TokenCreationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public TokenCreationException ( Throwable cause ) {
        super(cause);
    }

}

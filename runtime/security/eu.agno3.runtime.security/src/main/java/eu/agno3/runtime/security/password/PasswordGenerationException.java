/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.05.2015 by mbechler
 */
package eu.agno3.runtime.security.password;


/**
 * @author mbechler
 *
 */
public class PasswordGenerationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7635038862961205675L;


    /**
     * 
     */
    public PasswordGenerationException () {}


    /**
     * @param message
     */
    public PasswordGenerationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public PasswordGenerationException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public PasswordGenerationException ( String message, Throwable cause ) {
        super(message, cause);
    }

}

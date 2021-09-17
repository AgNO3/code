/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password;


/**
 * @author mbechler
 *
 */
public class PasswordChangePolicyException extends PasswordPolicyException {

    /**
     * 
     */
    private static final long serialVersionUID = 3467980013567108763L;


    /**
     * 
     */
    public PasswordChangePolicyException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public PasswordChangePolicyException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public PasswordChangePolicyException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public PasswordChangePolicyException ( Throwable cause ) {
        super(cause);
    }

}

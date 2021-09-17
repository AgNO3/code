/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.password;


import org.apache.shiro.authc.AccountException;


/**
 * @author mbechler
 *
 */
public class PasswordPolicyException extends AccountException {

    /**
     * 
     */
    private static final long serialVersionUID = 7204667899709071829L;


    /**
     * 
     */
    public PasswordPolicyException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public PasswordPolicyException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public PasswordPolicyException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public PasswordPolicyException ( Throwable cause ) {
        super(cause);
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import org.apache.shiro.authc.AuthenticationException;


/**
 * @author mbechler
 *
 */
public class CustomWebAuthAuthenticationException extends AuthenticationException {

    /**
     * 
     */
    private static final long serialVersionUID = 1142061968595335590L;
    private String code;


    /**
     * 
     */
    public CustomWebAuthAuthenticationException () {
        super();
    }


    /**
     * @param code
     * @param message
     * @param cause
     */
    public CustomWebAuthAuthenticationException ( String code, String message, Throwable cause ) {
        super(message, cause);
        this.code = code;
    }


    /**
     * @param code
     * @param message
     */
    public CustomWebAuthAuthenticationException ( String code, String message ) {
        super(message);
        this.code = code;
    }


    /**
     * @param code
     * @param cause
     */
    public CustomWebAuthAuthenticationException ( String code, Throwable cause ) {
        super(cause);
        this.code = code;
    }


    /**
     * @return the code
     */
    public String getCode () {
        return this.code;
    }

}

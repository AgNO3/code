/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import org.apache.shiro.authc.AuthenticationException;


/**
 * @author mbechler
 *
 */
public class CasAuthenticationException extends AuthenticationException {

    /**
     * 
     */
    private static final long serialVersionUID = -7985068019041002488L;


    /**
     * 
     */
    public CasAuthenticationException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public CasAuthenticationException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param message
     */
    public CasAuthenticationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public CasAuthenticationException ( Throwable cause ) {
        super(cause);
    }

}

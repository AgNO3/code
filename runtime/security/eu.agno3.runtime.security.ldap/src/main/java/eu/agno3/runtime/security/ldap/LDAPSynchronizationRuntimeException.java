/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


/**
 * @author mbechler
 *
 */
public class LDAPSynchronizationRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1988094310749620028L;


    /**
     * 
     */
    public LDAPSynchronizationRuntimeException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public LDAPSynchronizationRuntimeException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public LDAPSynchronizationRuntimeException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public LDAPSynchronizationRuntimeException ( Throwable cause ) {
        super(cause);
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.runtime.ldap.client;


/**
 * @author mbechler
 *
 */
public class LDAPConfigurationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -123320751164579706L;


    /**
     * 
     */
    public LDAPConfigurationException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public LDAPConfigurationException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public LDAPConfigurationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public LDAPConfigurationException ( Throwable cause ) {
        super(cause);
    }

}

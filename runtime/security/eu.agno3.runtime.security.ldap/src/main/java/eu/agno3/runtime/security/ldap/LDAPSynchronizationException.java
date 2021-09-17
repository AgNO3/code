/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


/**
 * @author mbechler
 *
 */
public class LDAPSynchronizationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2219006454808890137L;


    /**
     * 
     */
    public LDAPSynchronizationException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public LDAPSynchronizationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public LDAPSynchronizationException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public LDAPSynchronizationException ( Throwable t ) {
        super(t);
    }

}

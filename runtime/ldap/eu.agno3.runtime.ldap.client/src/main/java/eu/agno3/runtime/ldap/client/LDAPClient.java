/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.RootDSE;


/**
 * @author mbechler
 *
 */
public interface LDAPClient extends LDAPInterface, AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close ();


    /**
     * @param extendedRequest
     * @return the result
     * @throws LDAPException
     */
    ExtendedResult processExtendedOperation ( ExtendedRequest extendedRequest ) throws LDAPException;


    /**
     * @return the configured or automatically discovered base DN
     * @throws LDAPException
     */
    DN getBaseDN () throws LDAPException;


    /**
     * 
     * @param oid
     * @return whether the control is supported
     * @throws LDAPException
     */
    boolean isControlSupported ( String oid ) throws LDAPException;


    /**
     * @return a cached root DSE instance
     * @throws LDAPException
     */
    RootDSE getCachedRootDSE () throws LDAPException;


    /**
     * @param relative
     * @return a full DN for the relative DN given (in respect to the root DN)
     * @throws LDAPException
     */
    DN relativeDN ( String relative ) throws LDAPException;
}

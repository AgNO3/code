/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPException;


/**
 * @author mbechler
 *
 */
public interface LDAPClientFactory {

    /**
     * @return a ldap connection
     * @throws LDAPException
     */
    LDAPClient getConnection () throws LDAPException;


    /**
     * @param bindReq
     * @return a non-pooled connection
     * @throws LDAPException
     */
    LDAPClient getIndependedConnection ( BindRequest bindReq ) throws LDAPException;


    /**
     * @param req
     * @return the bind result
     * @throws LDAPException
     */
    BindResult tryBind ( BindRequest req ) throws LDAPException;

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public interface LDAPClientBuilder {

    /**
     * Creates a connection pool based on the config
     * 
     * @param cfg
     * @param tc
     * @return the connection pool
     * @throws LDAPException
     * @throws CryptoException
     */
    LDAPConnectionPool createConnectionPool ( LDAPConfiguration cfg, TLSContext tc ) throws LDAPException, CryptoException;


    /**
     * Creates a single connection based on the config
     * 
     * @param cfg
     * @param tc
     * @param bindReq
     *            if non null the given bind will be used instead of the configured one
     * @return a connection
     * @throws LDAPException
     * @throws CryptoException
     */
    LDAPClient createSingleConnection ( LDAPConfiguration cfg, TLSContext tc, BindRequest bindReq ) throws LDAPException, CryptoException;


    /**
     * Creates a single connection based on the config
     * 
     * @param cfg
     * @param tc
     * @return a connection
     * @throws LDAPException
     * @throws CryptoException
     */
    LDAPClient createSingleConnection ( LDAPConfiguration cfg, TLSContext tc ) throws LDAPException, CryptoException;


    /**
     * @param pool
     * @param conn
     * @param cfg
     * @return a wrapped pooled connection
     */
    LDAPClient wrapPoolConnection ( LDAPConnectionPool pool, LDAPConnection conn, LDAPConfiguration cfg );

}
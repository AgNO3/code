/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;


/**
 * @author mbechler
 *
 */
@Component (
    service = LDAPClientFactory.class,
    configurationPid = LDAPClientFactoryImpl.INTERNAL_PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LDAPClientFactoryImpl implements LDAPClientFactory {

    /**
     * 
     */
    public static final String INTERNAL_PID = "ldap.client.internal"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(LDAPClientFactoryImpl.class);

    private LDAPConfiguration config;
    private LDAPConnectionPool pool;
    private LDAPClientBuilder clientBuilder;

    private TLSContext tlsContext;


    @Reference ( target = "(|(subsystem=ldap)(role=client)(role=default))", updated = "updateTLSContext" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    protected synchronized void updateTLSContext ( TLSContext tc ) {
        reconnect();
    }


    @Reference ( updated = "updateLDAPConfiguration" )
    protected synchronized void setLDAPConfiguration ( LDAPConfiguration cfg ) {
        this.config = cfg;
    }


    protected synchronized void unsetLDAPConfiguration ( LDAPConfiguration cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    protected synchronized void updateLDAPConfiguration ( LDAPConfiguration tc ) {
        reconnect();
    }


    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        this.clientBuilder = lcb;
    }


    protected synchronized void unsetLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        if ( this.clientBuilder == lcb ) {
            this.clientBuilder = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.pool = connect();
    }


    /**
     * @return
     * 
     */
    private LDAPConnectionPool connect () {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Activating ldap connection pool for %s (%d initial, %d max)", //$NON-NLS-1$
                this.config.getInstanceId(),
                this.config.getInitialPoolSize(),
                this.config.getMaxPoolSize()));
        }

        try {
            return this.clientBuilder.createConnectionPool(this.config, this.tlsContext);
        }
        catch (
            LDAPException |
            CryptoException e ) {
            log.error("Failed to set up ldap connection pool", e); //$NON-NLS-1$
            return null;
        }
    }


    protected synchronized void reconnect () {
        if ( log.isDebugEnabled() ) {
            log.debug("Reconfiguring ldap connection pool " + this.config.getInstanceId()); //$NON-NLS-1$
        }

        if ( this.pool == null || this.pool.isClosed() ) {
            this.pool = connect();
            return;
        }

        LDAPConnectionPool oldPool = this.pool;
        LDAPConnectionPool newPool = connect();

        if ( newPool != null ) {
            oldPool.close();
            this.pool = newPool;
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Deactivating ldap connection pool for " + ctx.getProperties().get(LDAPConfigurationTracker.INSTANCE_ID)); //$NON-NLS-1$
        }

        if ( this.pool != null ) {
            this.pool.close();
            this.pool = null;
        }
    }


    @SuppressWarnings ( "resource" )
    @Override
    public LDAPClient getConnection () throws LDAPException {
        LDAPConnectionPool p = getPool();
        return new LDAPClientWrapper(p, p.getConnection(), this.config);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientFactory#getIndependedConnection(com.unboundid.ldap.sdk.BindRequest)
     */
    @Override
    public LDAPClient getIndependedConnection ( BindRequest bindReq ) throws LDAPException {
        try {
            return this.clientBuilder.createSingleConnection(this.config, this.tlsContext, bindReq);
        }
        catch ( CryptoException e ) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, e);
        }
    }


    @Override
    public BindResult tryBind ( BindRequest req ) throws LDAPException {
        return getPool().bindAndRevertAuthentication(req);
    }


    /**
     * @return
     * @throws LDAPException
     */
    private LDAPConnectionPool getPool () throws LDAPException {
        LDAPConnectionPool p = this.pool;
        if ( p == null ) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "Pool is not properly initialized"); //$NON-NLS-1$
        }
        return p;
    }

}

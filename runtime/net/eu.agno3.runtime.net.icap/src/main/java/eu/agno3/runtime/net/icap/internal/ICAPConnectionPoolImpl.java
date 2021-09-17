/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.net.icap.ICAPConfiguration;
import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPConnectionPool;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPScanRequest;
import eu.agno3.runtime.net.icap.ICAPScannerException;


/**
 * @author mbechler
 *
 */
@Component ( service = ICAPConnectionPool.class, configurationPid = "icap", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ICAPConnectionPoolImpl implements ICAPConnectionPool {

    private static final Logger log = Logger.getLogger(ICAPConnectionPoolImpl.class);

    private GenericObjectPool<ICAPConnection> pool;
    private ICAPConfiguration configuration;
    private TLSContext tlsContext;

    private ICAPConnectionPoolFactoryImpl connFactory;


    @Reference ( target = "(|(subsystem=icap)(role=client)(role=default))" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
        if ( log.isDebugEnabled() ) {
            try {
                log.debug("Bound TLS config " + tc.getConfig().getId()); //$NON-NLS-1$
            }
            catch ( CryptoException e ) {
                log.error("Failed to TLS config", e); //$NON-NLS-1$
            }
        }
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            this.configuration = ICAPConfigurationImpl.fromProperties(ctx.getProperties());
        }
        catch ( ICAPException e ) {
            log.error("Failed to parse ICAP configuration", e); //$NON-NLS-1$
            return;
        }

        this.connFactory = new ICAPConnectionPoolFactoryImpl(this.configuration, this.tlsContext);

        if ( this.pool == null ) {
            ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                GenericObjectPool<ICAPConnection> icapPool = new GenericObjectPool<>(this.connFactory);
                icapPool.setTestOnBorrow(true);
                icapPool.setTestOnReturn(true);
                icapPool.setMaxIdle(1);
                this.pool = icapPool;
            }
            finally {
                Thread.currentThread().setContextClassLoader(oldTCCL);
            }
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.pool != null ) {
            log.debug("Closing ICAP connection pool"); //$NON-NLS-1$
            this.pool.close();
            this.pool = null;
        }
        this.configuration = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnectionPool#getConnection()
     */
    @Override
    public ICAPConnection getConnection () throws ICAPException {
        try {
            if ( this.pool == null ) {
                throw new ICAPException("Not initialized"); //$NON-NLS-1$
            }
            return new ICAPPoolConnectionWrapper(this, this.pool.borrowObject());
        }
        catch ( ICAPException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new ICAPException("Failed to get connection from pool", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param req
     * @throws ICAPScannerException
     */
    @Override
    public void scan ( ICAPScanRequest req ) throws ICAPScannerException, ICAPException {
        try ( ICAPConnection conn = this.getConnection() ) {
            conn.scan(req);
        }
        catch (
            ICAPException |
            IOException e ) {
            throw new ICAPException("Failed to get ICAP connection", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnectionPool#createConnection()
     */
    @Override
    public ICAPConnection createConnection () throws ICAPException {
        if ( this.connFactory == null ) {
            throw new ICAPException("Not initialized"); //$NON-NLS-1$
        }
        return this.connFactory.createICAPConnection();
    }


    /**
     * @param conn
     */
    public void returnConnection ( ICAPConnection conn ) {
        this.pool.returnObject(conn);
    }
}

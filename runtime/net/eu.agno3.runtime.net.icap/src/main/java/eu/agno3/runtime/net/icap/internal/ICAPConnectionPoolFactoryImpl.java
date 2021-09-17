/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.net.icap.ICAPConfiguration;
import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPException;


/**
 * @author mbechler
 *
 */
public class ICAPConnectionPoolFactoryImpl extends BasePooledObjectFactory<ICAPConnection> {

    private TLSContext tlsContext;
    private ICAPConfiguration configuration;


    /**
     * @param cfg
     * @param tc
     */
    public ICAPConnectionPoolFactoryImpl ( ICAPConfiguration cfg, TLSContext tc ) {
        this.configuration = cfg;
        this.tlsContext = tc;
    }


    /**
     * @return a fresh icap connection
     * @throws ICAPException
     */
    public ICAPConnection createICAPConnection () throws ICAPException {
        if ( this.configuration == null ) {
            throw new ICAPException("Not initialized"); //$NON-NLS-1$
        }
        try {
            ICAPConnection conn = new ICAPConnectionImpl(this.configuration, this.tlsContext);
            conn.ensureConnected();
            return conn;
        }
        catch (
            CryptoException |
            URISyntaxException |
            IOException e ) {
            throw new ICAPException("Failed to create connection", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void destroyObject ( PooledObject<ICAPConnection> pt ) throws Exception {
        super.destroyObject(pt);
        pt.getObject().close();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public boolean validateObject ( PooledObject<ICAPConnection> pt ) {
        return super.validateObject(pt) && pt.getObject().check();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
     */
    @Override
    public ICAPConnection create () throws Exception {
        return this.createICAPConnection();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
     */
    @Override
    public PooledObject<ICAPConnection> wrap ( ICAPConnection t ) {
        return new DefaultPooledObject<>(t);
    }
}

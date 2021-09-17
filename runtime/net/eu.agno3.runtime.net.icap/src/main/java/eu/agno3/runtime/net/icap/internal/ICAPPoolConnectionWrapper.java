/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPOptions;
import eu.agno3.runtime.net.icap.ICAPResponse;
import eu.agno3.runtime.net.icap.ICAPScanRequest;
import eu.agno3.runtime.net.icap.ICAPScannerException;


/**
 * @author mbechler
 *
 */
public class ICAPPoolConnectionWrapper implements ICAPConnection {

    private ICAPConnectionPoolImpl pool;
    private ICAPConnection delegate;
    private boolean released;


    /**
     * @param pool
     * @param conn
     */
    public ICAPPoolConnectionWrapper ( ICAPConnectionPoolImpl pool, ICAPConnection conn ) {
        this.pool = pool;
        this.delegate = conn;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#close()
     */
    @Override
    public void close () throws IOException {
        this.released = true;
        this.pool.returnConnection(this.delegate);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#getOptions()
     */
    @Override
    public ICAPOptions getOptions () throws IOException, ICAPException {
        if ( this.released ) {
            throw new ICAPException("Connection is released"); //$NON-NLS-1$
        }
        return this.delegate.getOptions();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#respmod(java.io.InputStream, java.util.Map, byte[], byte[],
     *      boolean)
     */
    @Override
    public ICAPResponse respmod ( InputStream is, Map<String, List<String>> icapHeaders, byte[] reqHeader, byte[] resHeader, boolean preview )
            throws IOException, ICAPException {
        if ( this.released ) {
            throw new ICAPException("Connection is released"); //$NON-NLS-1$
        }
        return this.delegate.respmod(is, icapHeaders, reqHeader, resHeader, preview);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#reqmod(java.io.InputStream, java.util.Map, byte[], boolean)
     */
    @Override
    public ICAPResponse reqmod ( InputStream reqBody, Map<String, List<String>> icapHeaders, byte[] reqHeader, boolean preview )
            throws IOException, ICAPException {
        if ( this.released ) {
            throw new ICAPException("Connection is released"); //$NON-NLS-1$
        }
        return this.delegate.reqmod(reqBody, icapHeaders, reqHeader, preview);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#scan(eu.agno3.runtime.net.icap.ICAPScanRequest)
     */
    @Override
    public void scan ( ICAPScanRequest req ) throws ICAPScannerException, ICAPException {
        if ( this.released ) {
            throw new ICAPException("Connection is released"); //$NON-NLS-1$
        }
        this.delegate.scan(req);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#check()
     */
    @Override
    public boolean check () {
        return !this.released && this.delegate.check();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#ensureConnected()
     */
    @Override
    public void ensureConnected () throws IOException, ICAPException {

    }

}

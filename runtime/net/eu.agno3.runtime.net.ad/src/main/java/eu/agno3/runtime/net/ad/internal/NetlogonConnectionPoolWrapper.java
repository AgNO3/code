/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.NetlogonAuthenticator;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.ad.NetlogonOperations;

import jcifs.dcerpc.DcerpcHandle;


/**
 * @author mbechler
 *
 */
public class NetlogonConnectionPoolWrapper implements NetlogonConnection {

    private static final Logger log = Logger.getLogger(NetlogonConnectionPoolWrapper.class);

    private NetlogonConnection delegate;
    private GenericObjectPool<NetlogonConnection> pool;


    /**
     * @param pool
     * @param conn
     */
    public NetlogonConnectionPoolWrapper ( GenericObjectPool<NetlogonConnection> pool, NetlogonConnection conn ) {
        this.pool = pool;
        this.delegate = conn;
    }


    @Override
    public ADRealm getRealm () {
        return this.delegate.getRealm();
    }


    @Override
    public NetlogonOperations getNetlogonOperations () {
        return this.delegate.getNetlogonOperations();
    }


    @Override
    public DcerpcHandle getDcerpcHandle () {
        return this.delegate.getDcerpcHandle();
    }


    /**
     * @throws ADException
     * @throws IOException
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#init()
     */
    @Override
    public void init () throws ADException, IOException {
        this.delegate.init();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#fail()
     */
    @Override
    public void fail () {
        this.delegate.fail();
    }


    @Override
    public int getNegotiatedFlags () {
        return this.delegate.getNegotiatedFlags();
    }


    @Override
    public byte[] encryptSession ( byte[] input ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException {
        return this.delegate.encryptSession(input);
    }


    @Override
    public NetlogonAuthenticator authenticate () throws ADException {
        return this.delegate.authenticate();
    }


    @Override
    public void validate ( NetlogonAuthenticator returnAuthenticator, boolean error ) throws ADException {
        this.delegate.validate(returnAuthenticator, error);
    }


    @Override
    public void close () throws ADException {
        log.debug("released connection"); //$NON-NLS-1$
        this.pool.returnObject(this.delegate);
    }


    @Override
    public boolean check () {
        return this.delegate.check();
    }

}

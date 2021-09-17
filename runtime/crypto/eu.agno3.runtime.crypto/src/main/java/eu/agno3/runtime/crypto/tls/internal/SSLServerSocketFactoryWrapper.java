/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoRuntimeException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class SSLServerSocketFactoryWrapper extends SSLServerSocketFactory {

    private SSLServerSocketFactory delegate;
    private InternalTLSConfiguration config;
    private TLSParameterFactory tpf;


    /**
     * @param cfg
     * @param tcf
     * @param socketFactory
     * @throws CryptoException
     */
    public SSLServerSocketFactoryWrapper ( InternalTLSConfiguration cfg, TLSParameterFactory tcf, SSLServerSocketFactory socketFactory )
            throws CryptoException {
        this.config = cfg;
        this.tpf = tcf;
        this.delegate = socketFactory;
    }


    /**
     * @return the config
     */
    public InternalTLSConfiguration getConfig () {
        return this.config;
    }


    /**
     * @param createServerSocket
     * @return
     */
    private ServerSocket wrapSocket ( ServerSocket sock ) {

        if ( sock instanceof SSLServerSocket ) {
            SSLServerSocket sslSock = (SSLServerSocket) sock;
            try {
                sslSock.setSSLParameters(
                    this.tpf.makeSSLParameters(this.config, sslSock.getSupportedCipherSuites(), sslSock.getSupportedProtocols()));
            }
            catch ( CryptoException e ) {
                throw new CryptoRuntimeException(e);
            }
        }

        return sock;
    }


    /**
     * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
     */
    @Override
    public String[] getDefaultCipherSuites () {
        return this.delegate.getDefaultCipherSuites();
    }


    /**
     * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
     */
    @Override
    public String[] getSupportedCipherSuites () {
        return this.delegate.getSupportedCipherSuites();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ServerSocketFactory#createServerSocket(int)
     */
    @Override
    public ServerSocket createServerSocket ( int port ) throws IOException {
        return this.wrapSocket(this.delegate.createServerSocket(port));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ServerSocketFactory#createServerSocket(int, int)
     */
    @Override
    public ServerSocket createServerSocket ( int port, int backlog ) throws IOException {
        return this.wrapSocket(this.delegate.createServerSocket(port, backlog));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ServerSocketFactory#createServerSocket(int, int, java.net.InetAddress)
     */
    @Override
    public ServerSocket createServerSocket ( int port, int backlog, InetAddress ifAddress ) throws IOException {
        return this.wrapSocket(this.delegate.createServerSocket(port, backlog, ifAddress));
    }

}

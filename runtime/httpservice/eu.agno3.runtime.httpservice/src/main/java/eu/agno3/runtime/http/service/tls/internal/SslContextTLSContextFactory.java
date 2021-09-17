/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.io.IOException;
import java.net.InetAddress;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoRuntimeException;
import eu.agno3.runtime.crypto.tls.TLSConfiguration;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class SslContextTLSContextFactory extends SslContextFactory {

    private TLSContext tlsContext;
    private TLSConfiguration cfg;


    /**
     * @param tlsContext
     * @throws CryptoException
     */
    public SslContextTLSContextFactory ( TLSContext tlsContext ) throws CryptoException {
        this.tlsContext = tlsContext;
        this.cfg = tlsContext.getConfig();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#newSSLEngine()
     */
    @Override
    public SSLEngine newSSLEngine () {
        try {
            return this.tlsContext.createSSLEngine();
        }
        catch ( CryptoException e ) {
            throw new CryptoRuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#getNeedClientAuth()
     */
    @Override
    public boolean getNeedClientAuth () {
        return this.cfg.getRequireClientAuth();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#getWantClientAuth()
     */
    @Override
    public boolean getWantClientAuth () {
        return this.cfg.getRequestClientAuth();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#newSSLEngine(java.lang.String, int)
     */
    @Override
    public SSLEngine newSSLEngine ( String host, int port ) {
        try {
            return this.tlsContext.createSSLEngine(host, port);
        }
        catch ( CryptoException e ) {
            throw new CryptoRuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#newSslServerSocket(java.lang.String, int, int)
     */
    @Override
    public SSLServerSocket newSslServerSocket ( String host, int port, int backlog ) throws IOException {
        SSLServerSocketFactory sf;
        try {
            sf = this.tlsContext.getServerSocketFactory();
        }
        catch ( CryptoException e ) {
            throw new CryptoRuntimeException(e);
        }

        return (SSLServerSocket) ( host == null ? sf.createServerSocket(port, backlog) : sf.createServerSocket(
            port,
            backlog,
            InetAddress.getByName(host)) );

    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.util.ssl.SslContextFactory#newSslSocket()
     */
    @Override
    public SSLSocket newSslSocket () throws IOException {
        SSLSocketFactory sf;
        try {
            sf = this.tlsContext.getSocketFactory();
        }
        catch ( CryptoException e ) {
            throw new CryptoRuntimeException(e);
        }

        return (SSLSocket) sf.createSocket();
    }

}

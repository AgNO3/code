/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoRuntimeException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class SSLSocketFactoryWrapper extends SSLSocketFactory {

    private SSLSocketFactory delegate;
    private InternalTLSConfiguration config;
    private TLSParameterFactory tpf;


    /**
     * @param cfg
     * @param tcf
     * @param socketFactory
     * @throws CryptoException
     */
    public SSLSocketFactoryWrapper ( InternalTLSConfiguration cfg, TLSParameterFactory tcf, SSLSocketFactory socketFactory ) throws CryptoException {
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
     * @param sock
     * @return
     */
    private Socket wrapSocket ( Socket sock ) {
        if ( sock instanceof SSLSocket ) {
            SSLSocket sslSock = (SSLSocket) sock;
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
     * @throws IOException
     * @see javax.net.SocketFactory#createSocket()
     */
    @Override
    public Socket createSocket () throws IOException {
        return this.wrapSocket(this.delegate.createSocket());
    }


    /**
     * @param address
     * @param port
     * @param localAddress
     * @param localPort
     * @throws IOException
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int, java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket ( InetAddress address, int port, InetAddress localAddress, int localPort ) throws IOException {
        return this.wrapSocket(this.delegate.createSocket(address, port, localAddress, localPort));
    }


    /**
     * @param host
     * @param port
     * @throws IOException
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket ( InetAddress host, int port ) throws IOException {
        return this.wrapSocket(this.delegate.createSocket(host, port));
    }


    /**
     * @param s
     * @param host
     * @param port
     * @param autoClose
     * @throws IOException
     * @see javax.net.ssl.SSLSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
     */
    @Override
    public Socket createSocket ( Socket s, String host, int port, boolean autoClose ) throws IOException {
        return this.wrapSocket(this.delegate.createSocket(s, host, port, autoClose));
    }


    /**
     * @param host
     * @param port
     * @param localHost
     * @param localPort
     * @throws IOException
     * @throws UnknownHostException
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket ( String host, int port, InetAddress localHost, int localPort ) throws IOException, UnknownHostException {
        return this.wrapSocket(this.delegate.createSocket(host, port, localHost, localPort));
    }


    /**
     * @param host
     * @param port
     * @throws IOException
     * @throws UnknownHostException
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public Socket createSocket ( String host, int port ) throws IOException, UnknownHostException {
        return this.wrapSocket(this.delegate.createSocket(host, port));
    }


    /**
     * @param obj
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return this.delegate.equals(obj);
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.delegate.toString();
    }

}

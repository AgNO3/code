/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 3, 2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class RMISSLServerSocketFactory extends SslRMIServerSocketFactory {

    private final TLSContext context;
    private final String bindAddress;


    /**
     * @param tc
     * @param bindAddr
     * 
     */
    public RMISSLServerSocketFactory ( TLSContext tc, String bindAddr ) {
        super();
        this.bindAddress = bindAddr;
        this.context = tc;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
     */
    @Override
    public ServerSocket createServerSocket ( int port ) throws IOException {
        if ( this.bindAddress != null ) {
            try {
                InetAddress addr = InetAddress.getByName(this.bindAddress);
                return new TLSServerSocket(port, 0, addr, this.context);
            }
            catch ( UnknownHostException e ) {
                throw new IOException("Failed to determine bind address", e); //$NON-NLS-1$
            }
        }

        return new TLSServerSocket(port, this.context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.RMITCPServerSocketFactory#hashCode()
     */
    @Override
    public int hashCode () {
        return super.hashCode() + 3 * this.context.hashCode() + 5 * ( this.bindAddress != null ? this.bindAddress.hashCode() : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.RMITCPServerSocketFactory#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return super.equals(obj) && obj instanceof RMISSLServerSocketFactory
                && Objects.equals( ( (RMISSLServerSocketFactory) obj ).context, this.context)
                && Objects.equals( ( (RMISSLServerSocketFactory) obj ).bindAddress, this.bindAddress);
    }

    private static class TLSServerSocket extends ServerSocket {

        private final TLSContext context;


        TLSServerSocket ( int port, TLSContext ctx ) throws IOException {
            super(port);
            this.context = ctx;
        }


        TLSServerSocket ( int port, int backlog, InetAddress bindAddr, TLSContext ctx ) throws IOException {
            super(port, backlog, bindAddr);
            this.context = ctx;
        }


        @SuppressWarnings ( "resource" )
        @Override
        public Socket accept () throws IOException {
            Socket socket = super.accept();
            try {
                SSLSocket sslSocket = (SSLSocket) this.context.getSocketFactory()
                        .createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
                sslSocket.setUseClientMode(false);
                sslSocket.setWantClientAuth(this.context.getConfig().getRequestClientAuth());
                sslSocket.setNeedClientAuth(this.context.getConfig().getRequireClientAuth());
                return sslSocket;
            }
            catch ( CryptoException e ) {
                throw new IOException("Failed to set up TLS config", e); //$NON-NLS-1$
            }
        }

    }
}

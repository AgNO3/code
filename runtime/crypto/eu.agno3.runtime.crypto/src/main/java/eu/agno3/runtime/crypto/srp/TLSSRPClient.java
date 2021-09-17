/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;

import org.bouncycastle.crypto.tls.TlsClientProtocol;


/**
 * @author mbechler
 *
 */
public class TLSSRPClient implements AutoCloseable {

    private Socket sock;
    private SecureRandom sr;
    private byte[] identity;
    private byte[] secret;


    /**
     * @param s
     * @param identity
     * @param secret
     * @param sr
     * 
     */
    public TLSSRPClient ( Socket s, byte[] identity, byte[] secret, SecureRandom sr ) {
        this.sock = s;
        this.identity = identity;
        this.secret = secret;
        this.sr = sr;
    }


    /**
     * 
     * @return a connected socket
     * @throws IOException
     */
    public TLSSRPSocket connect () throws IOException {
        TlsClientProtocol tlsClientProtocol = new TlsClientProtocol(this.sock.getInputStream(), this.sock.getOutputStream(), this.sr);
        TLSSRPClientProtocol client = new TLSSRPClientProtocol(this.identity, this.secret);
        try {
            tlsClientProtocol.connect(client);
        }
        catch ( IOException e ) {
            try {
                this.sock.close();
            }
            catch ( Exception e2 ) {
                e.addSuppressed(e2);
            }
            throw new SSLHandshakeException(e.getMessage());
        }

        return new TLSSRPSocket(this.sock, tlsClientProtocol);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public synchronized void close () throws IOException {
        if ( this.sock != null ) {
            this.sock.close();
            this.sock = null;
        }
    }


    /**
     * @param sa
     * @param connTimeout
     * @param identity
     * @param secret
     * @param sr
     * @return connected client
     * @throws IOException
     */
    @SuppressWarnings ( "resource" )
    public static TLSSRPSocket create ( SocketAddress sa, int connTimeout, byte[] identity, byte[] secret, SecureRandom sr ) throws IOException {
        Socket s = SocketFactory.getDefault().createSocket();
        s.connect(sa, connTimeout);
        s.setSoTimeout(connTimeout);
        TLSSRPClient cl = new TLSSRPClient(s, identity, secret, sr);
        try {
            return cl.connect();
        }
        catch ( Exception e ) {
            try {
                cl.close();
            }
            catch ( IOException e2 ) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }


    /**
     * 
     * @param host
     * @param port
     * @param connTimeout
     * @param identity
     * @param secret
     * @param sr
     * @return connected client
     * @throws IOException
     */
    public static TLSSRPSocket create ( String host, int port, int connTimeout, byte[] identity, byte[] secret, SecureRandom sr ) throws IOException {
        return create(new InetSocketAddress(host, port), connTimeout, identity, secret, sr);
    }
}

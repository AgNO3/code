/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import org.bouncycastle.crypto.tls.TlsSRPIdentityManager;
import org.bouncycastle.crypto.tls.TlsServerProtocol;


/**
 * @author mbechler
 *
 */
public class TLSSRPServer implements Runnable, AutoCloseable, UncaughtExceptionHandler {

    private static final Logger log = Logger.getLogger(TLSSRPServer.class);

    private final TlsSRPIdentityManager identityManager;
    private final ServerSocket serverSocket;
    private final SecureRandom secureRandom;
    private final AtomicBoolean exit = new AtomicBoolean(false);
    private int connectionLimit = -1;

    private final ExecutorService pool;
    final TLSSRPConnectionHandler handler;


    /**
     * @param sock
     * @param handler
     * @param nThreads
     * @param name
     * @param identityManager
     * @param sr
     * 
     */
    public TLSSRPServer ( ServerSocket sock, TLSSRPConnectionHandler handler, int nThreads, final String name, TlsSRPIdentityManager identityManager,
            SecureRandom sr ) {
        this.serverSocket = sock;
        this.handler = handler;
        this.identityManager = identityManager;
        this.secureRandom = sr;
        this.pool = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {

            private AtomicInteger n = new AtomicInteger();


            @Override
            public Thread newThread ( Runnable r ) {
                Thread t = new Thread(r, name + "" + this.n.getAndIncrement()); //$NON-NLS-1$
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(TLSSRPServer.this);
                t.setContextClassLoader(null);
                return t;
            }
        });
    }


    /**
     * @param connectionLimit
     *            the connectionLimit to set
     */
    public void setConnectionLimit ( int connectionLimit ) {
        this.connectionLimit = connectionLimit;
    }


    /**
     * Shutdown workers, close socket
     */
    @Override
    public void close () {
        this.exit.set(true);
        this.pool.shutdownNow();
        try {
            this.pool.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e ) {
            log.warn("Workers failed to terminate in a timely manner", e); //$NON-NLS-1$
        }
        try {
            if ( this.serverSocket != null ) {
                this.serverSocket.close();
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to close socket", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the bound port
     */
    public int getPort () {
        return this.serverSocket.getLocalPort();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings ( "resource" )
    @Override
    public void run () {
        while ( !this.exit.get() ) {
            try {
                Socket accept = this.serverSocket.accept();
                if ( log.isDebugEnabled() ) {
                    log.debug("Have connection from " + accept.getRemoteSocketAddress()); //$NON-NLS-1$
                }
                this.pool.execute(new ConnectionRunnable(accept));
                this.connectionLimit--;
                if ( this.connectionLimit == 0 ) {
                    log.debug("Connection limit reached"); //$NON-NLS-1$
                    return;
                }
            }
            catch ( Exception e ) {
                log.warn("Error creating connection runnable", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return socket connection
     * @throws IOException
     */
    public TLSSRPSocket accept () throws IOException {
        return doAccept(this.serverSocket.accept());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException ( Thread t, Throwable e ) {
        log.warn("Uncaught in SRP listener", e); //$NON-NLS-1$
    }


    /**
     * @param socket
     * @return
     * @throws IOException
     * @throws SSLHandshakeException
     */
    final TLSSRPSocket doAccept ( Socket socket ) throws IOException, SSLHandshakeException {
        TlsServerProtocol tlsServerProtocol = new TlsServerProtocol(socket.getInputStream(), socket.getOutputStream(), this.secureRandom);
        TLSSRPServerProtocol server = new TLSSRPServerProtocol(this.identityManager);
        try {
            tlsServerProtocol.accept(server);
        }
        catch ( IOException e ) {
            try {
                socket.close();
            }
            catch ( Exception e2 ) {
                e.addSuppressed(e2);
            }
            throw new SSLHandshakeException(e.getMessage());
        }
        return new TLSSRPSocket(socket, tlsServerProtocol);
    }


    /**
     * 
     * @param port
     * @param h
     * @param name
     * @param nThreads
     * @param identityManager
     * @param sr
     * @return TLS-SRP protected server socket
     * @throws IOException
     */
    public static TLSSRPServer create ( int port, TLSSRPConnectionHandler h, String name, int nThreads, TlsSRPIdentityManager identityManager,
            SecureRandom sr ) throws IOException {
        return new TLSSRPServer(ServerSocketFactory.getDefault().createServerSocket(port), h, nThreads, name, identityManager, sr);
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * 
     * @param port
     * @param h
     * @param name
     * @param nThreads
     * @param identity
     * @param secret
     * @param sr
     * @return TLS-SRP protected server socket
     * @throws IOException
     */
    public static TLSSRPServer create ( int port, TLSSRPConnectionHandler h, String name, int nThreads, byte[] identity, byte[] secret,
            SecureRandom sr ) throws IOException {
        SRP6GroupParameters groupParams = SRP6StandardGroups.rfc5054_4096;
        byte[] seedKey = new byte[16];
        sr.nextBytes(seedKey);
        TlsSRPIdentityManager im = new SingleIdentitySimulatedIdentityManager(groupParams, identity, secret, seedKey);
        return create(port, h, name, nThreads, im, sr);
    }

    private class ConnectionRunnable implements Runnable {

        private Socket s;


        /**
         * @param s
         */
        public ConnectionRunnable ( Socket s ) {
            this.s = s;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            try {
                try ( TLSSRPSocket accepted = doAccept(this.s) ) {
                    TLSSRPServer.this.handler.handle(accepted);
                }
                catch ( IOException e ) {
                    getLog().debug("Handshake failure", e); //$NON-NLS-1$
                    return;
                }
            }
            finally {
                try {
                    this.s.close();
                }
                catch ( IOException e ) {
                    getLog().debug("Failed to close socket", e); //$NON-NLS-1$
                }
            }
        }

    }

}

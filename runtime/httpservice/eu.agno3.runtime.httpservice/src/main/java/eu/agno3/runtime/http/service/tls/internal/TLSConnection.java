/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;


/**
 * Enhanced SslConnection
 * 
 * Adds logging for handshake failures, session id and idle closes.
 * 
 * Wraps SSLEngine to prevent unclean closes to invalidate the SSL session.
 * 
 * <blockquote>
 * RFC 5246 - 7.2.1:
 * Note that as of TLS 1.1, failure to properly close a connection no
 * longer requires that a session not be resumed.
 * 
 * It is not required for the initiator of the close to wait for the responding close_notify alert
 * before closing the read side of the connection.
 * </blockquote>
 * 
 * The default fill behavior calls SSLEngine.closeInbound if the socket is closed
 * which requires that the client will have responded with an close_notify message
 * otherwise this is treated as fatal error that will invalidate the TLS session
 * 
 * @author mbechler
 *
 */
public class TLSConnection extends SslConnection {

    private static final Logger log = Logger.getLogger(TLSConnection.class);

    private boolean handshakeComplete;

    private final TLSConnectionStatisticsInternal stats;


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param byteBufferPool
     * @param executor
     * @param endPoint
     * @param sslEngine
     * @param stats
     */
    public TLSConnection ( ByteBufferPool byteBufferPool, Executor executor, EndPoint endPoint, SSLEngine sslEngine,
            TLSConnectionStatisticsInternal stats ) {
        super(byteBufferPool, executor, endPoint, new SSLEngineWrapper(sslEngine));
        this.stats = stats;
    }


    /**
     * @return the stats
     */
    public TLSConnectionStatisticsInternal getStats () {
        return this.stats;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.io.ssl.SslConnection#onFillable()
     */
    @Override
    public void onFillable () {
        super.onFillable();
        if ( !this.handshakeComplete && getSSLEngine().getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING ) {
            this.handshakeComplete = true;
            SSLSession session = getSSLEngine().getSession();

            this.stats.trackSuccessful(session);

            byte[] id = session.getId();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Session for %s is %s", getEndPoint().getRemoteAddress(), Hex.encodeHexString(id))); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.io.ssl.SslConnection#onIdleExpired()
     */
    @Override
    public boolean onIdleExpired () {
        if ( log.isDebugEnabled() ) {
            log.debug("Closing connection on idle timeout " + this); //$NON-NLS-1$
        }
        return super.onIdleExpired();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.io.ssl.SslConnection#onFillInterestedFailed(java.lang.Throwable)
     */
    @Override
    public void onFillInterestedFailed ( Throwable cause ) {
        if ( log.isDebugEnabled() ) {
            log.debug("fillInterestFailed for " + getEndPoint().getRemoteAddress(), cause); //$NON-NLS-1$
        }
        super.onFillInterestedFailed(cause);
    }


    @Override
    protected DecryptedEndPoint newDecryptedEndPoint () {
        return new LoggingDecryptedEndPoint();
    }

    /**
     * @author mbechler
     *
     */
    public class LoggingDecryptedEndPoint extends DecryptedEndPoint {

        private final AtomicBoolean closing = new AtomicBoolean();


        /**
         * 
         */
        public LoggingDecryptedEndPoint () {
            super();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.jetty.io.ssl.SslConnection.DecryptedEndPoint#fill(java.nio.ByteBuffer)
         */
        @Override
        public synchronized int fill ( ByteBuffer buffer ) throws IOException {
            try {
                return super.fill(buffer);
            }
            catch ( SSLHandshakeException e ) {
                handleHandshakeFailure(e);
                throw e;
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.jetty.io.AbstractEndPoint#onClose()
         */
        @Override
        public void onClose () {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug("onClose " + getEndPoint().getRemoteAddress()); //$NON-NLS-1$
            }
            super.onClose();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.jetty.io.AbstractEndPoint#onClose(java.lang.Throwable)
         */
        @Override
        protected void onClose ( Throwable failure ) {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug("onClose " + getEndPoint().getRemoteAddress(), failure); //$NON-NLS-1$
            }
            super.onClose(failure);
        }


        /**
         * @param e
         */
        private void handleHandshakeFailure ( SSLHandshakeException e ) {
            getLog().debug("TLS handshake failed", e); //$NON-NLS-1$
            getStats().trackFailure(e);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.jetty.io.ssl.SslConnection.DecryptedEndPoint#doClose()
         */
        @Override
        public void doClose () {
            // TODO: is this behaving correctly? was: close(Throwable)
            // maybe no longer required
            if ( this.closing.getAndSet(true) ) {
                return;
            }
            super.doClose();
        }
    }

}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 24, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class SSLEngineWrapper extends SSLEngine {

    private static final Logger log = Logger.getLogger(SSLEngineWrapper.class);

    private final SSLEngine delegate;
    private final boolean valid;
    private final static Class<?> SSL_ENGINE_IMPL;
    private final static MethodHandle CLOSE_INBOUND_INTERNAL;
    private final static MethodHandle GET_CONNECTION_STATE;
    private final static MethodHandle GET_RECV_CN;
    private final static MethodHandle SET_RECV_CN;
    private final static boolean VALID;


    static {
        Class<?> impl = null;
        MethodHandle closeInboundInternal = null;
        MethodHandle getConnectionState = null;
        MethodHandle getRecvCN = null;
        MethodHandle setRecvCN = null;
        try {
            Lookup lookup = MethodHandles.lookup();
            impl = Class.forName("sun.security.ssl.SSLEngineImpl"); //$NON-NLS-1$
            Method cii = impl.getDeclaredMethod("closeInboundInternal"); //$NON-NLS-1$
            cii.setAccessible(true);
            closeInboundInternal = lookup.unreflect(cii);

            Field csF = impl.getDeclaredField("connectionState"); //$NON-NLS-1$
            csF.setAccessible(true);
            getConnectionState = lookup.unreflectGetter(csF);

            Field recvCN = impl.getDeclaredField("recvCN"); //$NON-NLS-1$
            recvCN.setAccessible(true);
            getRecvCN = lookup.unreflectGetter(recvCN);
            setRecvCN = lookup.unreflectSetter(recvCN);
        }
        catch ( Exception e ) {
            log.error("Failed to get SSLEngine internals", e); //$NON-NLS-1$
        }

        SSL_ENGINE_IMPL = impl;
        CLOSE_INBOUND_INTERNAL = closeInboundInternal;
        GET_CONNECTION_STATE = getConnectionState;
        GET_RECV_CN = getRecvCN;
        SET_RECV_CN = setRecvCN;
        VALID = SSL_ENGINE_IMPL != null && CLOSE_INBOUND_INTERNAL != null && GET_CONNECTION_STATE != null && GET_RECV_CN != null
                && SET_RECV_CN != null;
    }


    /**
     * @param delegate
     * 
     */
    public SSLEngineWrapper ( SSLEngine delegate ) {
        this.valid = VALID && delegate.getClass().isAssignableFrom(SSL_ENGINE_IMPL);
        this.delegate = delegate;
    }


    @Override
    public final int hashCode () {
        return this.delegate.hashCode();
    }


    @Override
    public final boolean equals ( Object obj ) {
        return this.delegate.equals(obj);
    }


    @Override
    public final String toString () {
        return this.delegate.toString();
    }


    @Override
    public final String getPeerHost () {
        return this.delegate.getPeerHost();
    }


    @Override
    public final int getPeerPort () {
        return this.delegate.getPeerPort();
    }


    @Override
    public final SSLEngineResult wrap ( ByteBuffer src, ByteBuffer dst ) throws SSLException {
        return this.delegate.wrap(src, dst);
    }


    @Override
    public final SSLEngineResult wrap ( ByteBuffer[] srcs, ByteBuffer dst ) throws SSLException {
        return this.delegate.wrap(srcs, dst);
    }


    @Override
    public final SSLEngineResult wrap ( ByteBuffer[] srcs, int offset, int length, ByteBuffer dst ) throws SSLException {
        return this.delegate.wrap(srcs, offset, length, dst);
    }


    @Override
    public final SSLEngineResult unwrap ( ByteBuffer src, ByteBuffer dst ) throws SSLException {
        return this.delegate.unwrap(src, dst);
    }


    @Override
    public final SSLEngineResult unwrap ( ByteBuffer src, ByteBuffer[] dsts ) throws SSLException {
        return this.delegate.unwrap(src, dsts);
    }


    @Override
    public final SSLEngineResult unwrap ( ByteBuffer src, ByteBuffer[] dsts, int offset, int length ) throws SSLException {
        return this.delegate.unwrap(src, dsts, offset, length);
    }


    @Override
    public final Runnable getDelegatedTask () {
        return this.delegate.getDelegatedTask();
    }


    @Override
    public final void closeInbound () throws SSLException {
        if ( !this.valid ) {
            this.delegate.closeInbound();
            return;
        }
        try {
            synchronized ( this.delegate ) {
                int connectionState = (int) GET_CONNECTION_STATE.invoke(this.delegate);
                boolean recvCN = (boolean) GET_RECV_CN.invoke(this.delegate);
                if ( ( connectionState != 0 ) && !recvCN ) { // 0 == cs_START
                    SET_RECV_CN.invoke(this.delegate, true);
                    CLOSE_INBOUND_INTERNAL.invoke(this.delegate);
                    throw new SSLException("Possible truncation"); //$NON-NLS-1$
                }
                CLOSE_INBOUND_INTERNAL.invoke(this.delegate);
            }
        }
        catch ( SSLException e ) {
            throw e;
        }
        catch ( Throwable e ) {
            log.warn("Non SSL exception", e); //$NON-NLS-1$
            throw new SSLException("Failed to close SSLEngine", e); //$NON-NLS-1$
        }
    }


    @Override
    public final boolean isInboundDone () {
        return this.delegate.isInboundDone();
    }


    @Override
    public final void closeOutbound () {
        this.delegate.closeOutbound();
    }


    @Override
    public final boolean isOutboundDone () {
        return this.delegate.isOutboundDone();
    }


    @Override
    public final String[] getSupportedCipherSuites () {
        return this.delegate.getSupportedCipherSuites();
    }


    @Override
    public final String[] getEnabledCipherSuites () {
        return this.delegate.getEnabledCipherSuites();
    }


    @Override
    public final void setEnabledCipherSuites ( String[] suites ) {
        this.delegate.setEnabledCipherSuites(suites);
    }


    @Override
    public final String[] getSupportedProtocols () {
        return this.delegate.getSupportedProtocols();
    }


    @Override
    public final String[] getEnabledProtocols () {
        return this.delegate.getEnabledProtocols();
    }


    @Override
    public final void setEnabledProtocols ( String[] protocols ) {
        this.delegate.setEnabledProtocols(protocols);
    }


    @Override
    public final SSLSession getSession () {
        return this.delegate.getSession();
    }


    @Override
    public final SSLSession getHandshakeSession () {
        return this.delegate.getHandshakeSession();
    }


    @Override
    public final void beginHandshake () throws SSLException {
        this.delegate.beginHandshake();
    }


    @Override
    public final HandshakeStatus getHandshakeStatus () {
        return this.delegate.getHandshakeStatus();
    }


    @Override
    public final void setUseClientMode ( boolean mode ) {
        this.delegate.setUseClientMode(mode);
    }


    @Override
    public final boolean getUseClientMode () {
        return this.delegate.getUseClientMode();
    }


    @Override
    public final void setNeedClientAuth ( boolean need ) {
        this.delegate.setNeedClientAuth(need);
    }


    @Override
    public final boolean getNeedClientAuth () {
        return this.delegate.getNeedClientAuth();
    }


    @Override
    public final void setWantClientAuth ( boolean want ) {
        this.delegate.setWantClientAuth(want);
    }


    @Override
    public final boolean getWantClientAuth () {
        return this.delegate.getWantClientAuth();
    }


    @Override
    public final void setEnableSessionCreation ( boolean flag ) {
        this.delegate.setEnableSessionCreation(flag);
    }


    @Override
    public final boolean getEnableSessionCreation () {
        return this.delegate.getEnableSessionCreation();
    }


    @Override
    public final SSLParameters getSSLParameters () {
        return this.delegate.getSSLParameters();
    }


    @Override
    public final void setSSLParameters ( SSLParameters params ) {
        this.delegate.setSSLParameters(params);
    }

}

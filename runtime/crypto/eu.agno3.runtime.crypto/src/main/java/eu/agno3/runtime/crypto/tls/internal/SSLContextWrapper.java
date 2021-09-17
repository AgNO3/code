/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.KeyManagementException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoRuntimeException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class SSLContextWrapper extends SSLContext {

    /**
     * 
     */
    private static final String AGNO3_PROVIDER_NAME = "AgNO3"; //$NON-NLS-1$


    /**
     * @param cfg
     * @param tcf
     * @param context
     * @param socketFactory
     * @param serverSocketFactory
     */
    public SSLContextWrapper ( InternalTLSConfiguration cfg, TLSParameterFactory tcf, SSLContext context, SSLSocketFactory socketFactory,
            SSLServerSocketFactory serverSocketFactory ) {

        super(
            new SSLContextWrapperSpi(cfg, tcf, context, socketFactory, serverSocketFactory),
            new java.security.Provider(AGNO3_PROVIDER_NAME, 0.1, StringUtils.EMPTY) {

                /**
                 * 
                 */
                private static final long serialVersionUID = 1L;
            },
            "TLS"); //$NON-NLS-1$
    }

    /**
     * @author mbechler
     *
     */
    public static class SSLContextWrapperSpi extends SSLContextSpi {

        private InternalTLSConfiguration cfg;
        private SSLContext context;
        private SSLSocketFactory socketFactory;
        private SSLServerSocketFactory serverSocketFactory;
        private TLSParameterFactory tpf;


        /**
         * @param cfg
         * @param tcf
         * @param context
         * @param socketFactory
         * @param serverSocketFactory
         */
        public SSLContextWrapperSpi ( InternalTLSConfiguration cfg, TLSParameterFactory tcf, SSLContext context, SSLSocketFactory socketFactory,
                SSLServerSocketFactory serverSocketFactory ) {
            this.cfg = cfg;
            this.tpf = tcf;
            this.context = context;
            this.socketFactory = socketFactory;
            this.serverSocketFactory = serverSocketFactory;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineCreateSSLEngine()
         */
        @Override
        protected SSLEngine engineCreateSSLEngine () {
            SSLEngine engine = this.context.createSSLEngine();
            try {
                return setupSSLEngine(engine);
            }
            catch ( CryptoException e ) {
                throw new CryptoRuntimeException(e);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineCreateSSLEngine(java.lang.String, int)
         */
        @Override
        protected SSLEngine engineCreateSSLEngine ( String host, int port ) {
            SSLEngine engine = this.context.createSSLEngine(host, port);
            try {
                return setupSSLEngine(engine);
            }
            catch ( CryptoException e ) {
                throw new CryptoRuntimeException(e);
            }
        }


        /**
         * @param createSSLEngine
         * @return
         * @throws CryptoException
         */
        private SSLEngine setupSSLEngine ( SSLEngine engine ) throws CryptoException {
            engine.setSSLParameters(this.tpf.makeSSLParameters(this.cfg, engine.getSupportedCipherSuites(), engine.getSupportedProtocols()));
            return engine;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineGetClientSessionContext()
         */
        @Override
        protected SSLSessionContext engineGetClientSessionContext () {
            return this.context.getClientSessionContext();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineGetServerSessionContext()
         */
        @Override
        protected SSLSessionContext engineGetServerSessionContext () {
            return this.context.getServerSessionContext();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineGetServerSocketFactory()
         */
        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory () {
            return this.serverSocketFactory;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineGetSocketFactory()
         */
        @Override
        protected SSLSocketFactory engineGetSocketFactory () {
            return this.socketFactory;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.SSLContextSpi#engineInit(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[],
         *      java.security.SecureRandom)
         */
        @Override
        protected void engineInit ( KeyManager[] km, TrustManager[] tm, SecureRandom sr ) throws KeyManagementException {
            // ignore
        }

    }

}

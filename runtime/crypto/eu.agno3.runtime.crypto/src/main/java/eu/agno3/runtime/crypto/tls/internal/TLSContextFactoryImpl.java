/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = TLSContextFactory.class )
public class TLSContextFactoryImpl implements TLSContextFactory {

    private static final String DEFAULT_TLS_PROTOCOL = "TLSv1.2"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(TLSContextFactoryImpl.class);


    @Override
    public SSLContext getContext ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException {
        return getContextInternal(cfg, randomSource);
    }


    @Override
    public KeyManager[] getKeyManagers ( InternalTLSConfiguration cfg ) throws CryptoException {
        return cfg.getKeyManagers();
    }


    @Override
    public TrustManager[] getTrustManagers ( InternalTLSConfiguration cfg ) throws CryptoException {
        return cfg.getTrustManagers();
    }


    private static SSLContext getContextInternal ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException {
        try {
            SSLContext ctx = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);
            ctx.init(cfg.getKeyManagers(), cfg.getTrustManagers(), randomSource);
            return ctx;
        }
        catch ( GeneralSecurityException e ) {
            log.error("Failed to create default TLS context"); //$NON-NLS-1$
            throw new CryptoException("Failed to create TLS context", e); //$NON-NLS-1$
        }
    }


    @Override
    public SSLSocketFactory getSocketFactory ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException {
        return new SSLSocketFactoryWrapper(cfg, getParameterFactory(cfg), this.getContext(cfg, randomSource).getSocketFactory());
    }


    @Override
    public SSLServerSocketFactory getServerSocketFactory ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException {
        SSLServerSocketFactory socketFactory = this.getContext(cfg, randomSource).getServerSocketFactory();
        return new SSLServerSocketFactoryWrapper(cfg, getParameterFactory(cfg), socketFactory);
    }


    @Override
    public HostnameVerifier getHostnameVerifier ( InternalTLSConfiguration cfg ) throws CryptoException {
        return cfg.getHostnameVerifier();
    }


    @Override
    public TLSParameterFactory getParameterFactory ( InternalTLSConfiguration cfg ) throws CryptoException {
        return new TLSParameterFactoryImpl();
    }
}

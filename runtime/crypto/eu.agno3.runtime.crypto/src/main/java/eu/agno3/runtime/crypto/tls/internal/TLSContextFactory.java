/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public interface TLSContextFactory {

    /**
     * @param cfg
     * @param randomSource
     * @return an ssl context for the specified usage
     * @throws CryptoException
     */
    SSLContext getContext ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException;


    /**
     * @param cfg
     * @param randomSource
     * @return a client socket factory for the specified usage
     * @throws CryptoException
     */
    SSLSocketFactory getSocketFactory ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException;


    /**
     * @param cfg
     * @param randomSource
     * @return a server socket factory for the specified usage
     * @throws CryptoException
     */
    SSLServerSocketFactory getServerSocketFactory ( InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException;


    /**
     * 
     * @param cfg
     * @return a hostname verifier for the specified usage
     * @throws CryptoException
     */
    HostnameVerifier getHostnameVerifier ( InternalTLSConfiguration cfg ) throws CryptoException;


    /**
     * @param cfg
     * @return the trust managers for this context
     * @throws CryptoException
     */
    TrustManager[] getTrustManagers ( InternalTLSConfiguration cfg ) throws CryptoException;


    /**
     * @param cfg
     * @return the key managers for this context
     * @throws CryptoException
     */
    KeyManager[] getKeyManagers ( InternalTLSConfiguration cfg ) throws CryptoException;


    /**
     * @param cfg
     * @return tls parameter factory
     * @throws CryptoException
     */
    TLSParameterFactory getParameterFactory ( InternalTLSConfiguration cfg ) throws CryptoException;

}

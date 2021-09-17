/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.PublicKey;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface TLSContext {

    /**
     * @param subsystem
     * @param uri
     * @return an ssl context for the specified usage
     * @throws CryptoException
     */
    SSLContext getContext () throws CryptoException;


    /**
     * @param subsystem
     * @param uri
     * @return a client socket factory
     * @throws CryptoException
     */
    SSLSocketFactory getSocketFactory () throws CryptoException;


    /**
     * @return a server socket factory
     * @throws CryptoException
     */
    SSLServerSocketFactory getServerSocketFactory () throws CryptoException;


    /**
     * 
     * @return a ssl engine
     * @throws CryptoException
     */
    SSLEngine createSSLEngine () throws CryptoException;


    /**
     * 
     * @param peerHost
     * @param peerPort
     * @return a ssl engine
     * @throws CryptoException
     */
    SSLEngine createSSLEngine ( String peerHost, int peerPort ) throws CryptoException;


    /**
     * 
     * @param subsystem
     * @param uri
     * @return a hostname verifier for the specified usage
     * @throws CryptoException
     */
    HostnameVerifier getHostnameVerifier () throws CryptoException;


    /**
     * @param subsystem
     * @param uri
     * @return the TLS configuration for specified usage
     * @throws CryptoException
     */
    TLSConfiguration getConfig () throws CryptoException;


    /**
     * @return a random source
     */
    SecureRandom getRandomSource ();


    /**
     * @param subsystem
     * @param uri
     * @return the trust managers for this context
     * @throws CryptoException
     */
    TrustManager[] getTrustManagers () throws CryptoException;


    /**
     * @param subsystem
     * @param uri
     * @return the key managers for this context
     * @throws CryptoException
     */
    KeyManager[] getKeyManagers () throws CryptoException;


    /**
     * @return the public key for the configured key
     */
    PublicKey getPrimaryCertificatePubKey ();

}

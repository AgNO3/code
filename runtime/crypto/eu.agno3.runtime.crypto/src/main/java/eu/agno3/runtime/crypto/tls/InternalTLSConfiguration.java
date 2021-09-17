/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface InternalTLSConfiguration extends TLSConfiguration {

    /**
     * @return key managers to use
     */
    KeyManager[] getKeyManagers ();


    /**
     * @return trust managers to use
     * @throws CryptoException
     */
    TrustManager[] getTrustManagers () throws CryptoException;


    /**
     * @return hostname verifier to use
     */
    HostnameVerifier getHostnameVerifier ();


    /**
     * @return sni matchers to use
     */
    Collection<SNIMatcher> getSniMatchers ();


    /**
     * @param sslParameters
     * @return adapter ssl parameters
     */
    SSLParameters adaptParameters ( SSLParameters sslParameters );

}

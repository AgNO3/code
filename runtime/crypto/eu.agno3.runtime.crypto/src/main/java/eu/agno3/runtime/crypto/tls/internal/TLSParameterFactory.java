/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import javax.net.ssl.SSLParameters;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public interface TLSParameterFactory {

    /**
     * @param cfg
     * @param supportedCipherSuites
     * @param supportedProtocols
     * @return ssl parameters
     * @throws CryptoException
     */
    SSLParameters makeSSLParameters ( InternalTLSConfiguration cfg, String[] supportedCipherSuites, String[] supportedProtocols )
            throws CryptoException;

}

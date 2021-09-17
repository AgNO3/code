/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation;


import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface SSLEndpointConfigTestFactory {

    /**
     * 
     * @param sec
     * @return adapted tls context
     * @throws CryptoException
     */
    TLSTestContext adaptSSLClient ( SSLClientConfiguration sec ) throws CryptoException;
}

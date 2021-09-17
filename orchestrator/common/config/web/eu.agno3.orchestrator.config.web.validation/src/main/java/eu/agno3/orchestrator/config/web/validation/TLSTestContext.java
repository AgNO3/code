/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation;


import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public interface TLSTestContext {

    /**
     * 
     * @return TLS context to use
     */
    TLSContext getContext ();


    /**
     * @return name/chain pairs for which hostname verification failed
     */
    Map<String, X509Certificate[]> getNameValidationFailures ();


    /**
     * @return certificate chains failing validation
     */
    List<X509Certificate[]> getChainValidationFailures ();


    /**
     * @return whether another trust store was used than specified
     */
    boolean isTrustFallback ();


    /**
     * 
     * @return truststore specified in configuration
     */
    String getTrustFallbackFrom ();


    /**
     * 
     * @return truststore fallback back to
     */
    String getTrustFallbackTo ();
}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.security.cert.PKIXCertPathChecker;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface PKIXCertPathCheckerFactory {

    /**
     * @return whether forward checking is supported
     */
    boolean isForwardCheckingSupported ();


    /**
     * @return the supported extensions
     */
    Set<String> getSupportedExtensions ();


    /**
     * @return a patch checker instance
     */
    PKIXCertPathChecker createInstance ();

}
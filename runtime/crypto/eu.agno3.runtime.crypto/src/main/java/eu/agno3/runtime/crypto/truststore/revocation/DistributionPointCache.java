/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import java.io.IOException;
import java.net.URI;
import java.security.cert.CRLException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CRL;


/**
 * @author mbechler
 *
 */
public interface DistributionPointCache {

    /**
     * @param uri
     * @param params
     * @return the cached CRL or null if not available
     * @throws IOException
     * @throws CRLException
     */
    X509CRL getCRL ( URI uri, PKIXParameters params ) throws IOException, CRLException;

}

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
public class NoDistributionPointCache implements DistributionPointCache {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.DistributionPointCache#getCRL(java.net.URI,
     *      java.security.cert.PKIXParameters)
     */
    @Override
    public X509CRL getCRL ( URI uri, PKIXParameters params ) throws IOException, CRLException {
        return null;
    }

}

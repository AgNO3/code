/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.SingleResp;

import eu.agno3.runtime.crypto.truststore.revocation.OCSPCache;


/**
 * @author mbechler
 *
 */
public class NoOCSPCache implements OCSPCache {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPCache#getCached(org.bouncycastle.cert.ocsp.CertificateID)
     */
    @Override
    public SingleResp getCached ( CertificateID id ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPCache#updateCache(org.bouncycastle.cert.ocsp.SingleResp)
     */
    @Override
    public void updateCache ( SingleResp resp ) {}

}

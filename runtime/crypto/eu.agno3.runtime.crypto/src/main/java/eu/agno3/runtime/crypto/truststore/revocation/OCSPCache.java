/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.SingleResp;


/**
 * @author mbechler
 *
 */
public interface OCSPCache {

    /**
     * @param id
     * @return a cached ocsp response for the given certificate, must only return non-expired entries
     */
    public SingleResp getCached ( CertificateID id );


    /**
     * 
     * @param resp
     *            a validated ocsp response
     */
    public void updateCache ( SingleResp resp );
}

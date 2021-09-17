/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.SingleResp;

import eu.agno3.runtime.crypto.truststore.revocation.OCSPCache;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class OCSPCacheImpl implements OCSPCache {

    private Map<CertificateID, SoftReference<SingleResp>> cache;


    /**
     * @param config
     * 
     */
    public OCSPCacheImpl ( RevocationConfig config ) {
        this.cache = Collections.synchronizedMap(new LRUMap<CertificateID, SoftReference<SingleResp>>(config.getOcspCacheSize()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPCache#getCached(org.bouncycastle.cert.ocsp.CertificateID)
     */
    @Override
    public SingleResp getCached ( CertificateID id ) {
        SoftReference<SingleResp> ref = this.cache.get(id);

        if ( ref == null ) {
            return null;
        }

        SingleResp resp = ref.get();

        if ( resp == null ) {
            this.cache.remove(id);
            return null;
        }

        if ( resp.getNextUpdate().before(new Date()) ) {
            this.cache.remove(id);
            return null;
        }

        return resp;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPCache#updateCache(org.bouncycastle.cert.ocsp.SingleResp)
     */
    @Override
    public void updateCache ( SingleResp resp ) {
        this.cache.put(resp.getCertID(), new SoftReference<>(resp));
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CRL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.truststore.revocation.DistributionPointCache;
import eu.agno3.runtime.crypto.truststore.revocation.RemoteLoaderUtil;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class DistributionPointCacheImpl implements DistributionPointCache {

    private static final Logger log = Logger.getLogger(DistributionPointCacheImpl.class);

    private static final int CRL_EXPIRE_SKEW = 2;

    private final Map<URI, CacheEntry<X509CRL>> cache;
    private RevocationConfig revocationConfig;

    private SecureRandom rand;


    /**
     * @param config
     * @param rand
     * 
     */
    public DistributionPointCacheImpl ( RevocationConfig config, SecureRandom rand ) {
        this.revocationConfig = config;
        this.cache = Collections.synchronizedMap(new LRUMap<URI, CacheEntry<X509CRL>>(this.revocationConfig.getCrlCacheSize()));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.DistributionPointCache#getCRL(java.net.URI,
     *      java.security.cert.PKIXParameters)
     */
    @Override
    public X509CRL getCRL ( URI uri, PKIXParameters params ) throws IOException, CRLException {
        CacheEntry<X509CRL> entry = this.cache.get(uri);
        if ( entry == null ) {
            log.debug("Not cached"); //$NON-NLS-1$
            return this.fetchCRL(uri, null, params);
        }

        if ( entry.isExpired() ) {
            log.debug("Found an expired entry"); //$NON-NLS-1$
            return this.fetchCRL(uri, entry.getObject(), params);
        }

        if ( entry.isNegative() ) {
            log.debug("Negative cached"); //$NON-NLS-1$
            return null;
        }
        return entry.getObject();
    }


    /**
     * @param uri
     * @param params
     * @param object
     * @return
     * @throws IOException
     * @throws InvalidAlgorithmParameterException
     */
    private X509CRL fetchCRL ( URI uri, X509CRL old, PKIXParameters params ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Need to fetch " + uri); //$NON-NLS-1$
        }

        try {
            X509CRL crl = RemoteLoaderUtil.loadCRL(
                makeParams(params),
                this.rand,
                old,
                uri,
                this.revocationConfig.getConnectTimeout(),
                this.revocationConfig.getReadTimeout(),
                this.revocationConfig.getMaxRedirects());
            this.cache.put(uri, new CacheEntry<>(crl, new DateTime(crl.getNextUpdate()).minusHours(CRL_EXPIRE_SKEW)));
            return crl;
        }
        catch ( IOException e ) {
            this.cache.put(uri, new CacheEntry<X509CRL>(DateTime.now().plusMinutes(this.revocationConfig.getCrlNegativeCacheMinutes())));
            throw e;
        }
    }


    /**
     * @param certs
     * @return
     * @throws InvalidAlgorithmParameterException
     */
    private static PKIXBuilderParameters makeParams ( PKIXParameters params ) throws IOException {

        try {
            PKIXBuilderParameters builder = new PKIXBuilderParameters(params.getTrustAnchors(), params.getTargetCertConstraints());
            builder.setRevocationEnabled(false);

            for ( CertStore store : params.getCertStores() ) {
                builder.addCertStore(store);
            }

            return builder;
        }
        catch ( GeneralSecurityException e ) {
            throw new IOException("Failed to make PKIX params for CRL connection", e); //$NON-NLS-1$
        }
    }

}

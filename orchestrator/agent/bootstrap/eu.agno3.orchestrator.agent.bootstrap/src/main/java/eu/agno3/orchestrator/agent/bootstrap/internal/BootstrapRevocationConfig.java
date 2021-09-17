/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.net.URI;
import java.security.cert.X509Certificate;

import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class BootstrapRevocationConfig implements RevocationConfig {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isCheckOnlyEndEntityCerts()
     */
    @Override
    public boolean isCheckOnlyEndEntityCerts () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isCheckCRL()
     */
    @Override
    public boolean isCheckCRL () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isRequireCRL()
     */
    @Override
    public boolean isRequireCRL () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isIgnoreUnavailableCRL()
     */
    @Override
    public boolean isIgnoreUnavailableCRL () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isIgnoreExpiredCRL()
     */
    @Override
    public boolean isIgnoreExpiredCRL () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getCrlCacheSize()
     */
    @Override
    public int getCrlCacheSize () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getCrlNegativeCacheMinutes()
     */
    @Override
    public int getCrlNegativeCacheMinutes () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getCrlUpdateIntervalMinutes()
     */
    @Override
    public int getCrlUpdateIntervalMinutes () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isDownloadCRLs()
     */
    @Override
    public boolean isDownloadCRLs () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isCheckOCSP()
     */
    @Override
    public boolean isCheckOCSP () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isRequireOCSP()
     */
    @Override
    public boolean isRequireOCSP () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getSystemOCSPUri()
     */
    @Override
    public URI getSystemOCSPUri () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#isCheckAllUsingSystemOCSP()
     */
    @Override
    public boolean isCheckAllUsingSystemOCSP () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getOcspCacheSize()
     */
    @Override
    public int getOcspCacheSize () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getSystemOCSPTrustCert()
     */
    @Override
    public X509Certificate getSystemOCSPTrustCert () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getReadTimeout()
     */
    @Override
    public int getReadTimeout () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getConnectTimeout()
     */
    @Override
    public int getConnectTimeout () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getMaxRedirects()
     */
    @Override
    public int getMaxRedirects () {
        return 0;
    }

}

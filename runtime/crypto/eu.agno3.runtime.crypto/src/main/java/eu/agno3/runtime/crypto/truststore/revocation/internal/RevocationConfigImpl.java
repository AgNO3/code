/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.net.URI;
import java.security.cert.X509Certificate;

import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class RevocationConfigImpl implements RevocationConfig {

    private boolean checkOnlyEndEntityCerts = false;
    private boolean checkCRL = true;
    private boolean requireCRL = false;
    private boolean ignoreNoCRL = false;
    private boolean ignoreExpiredCRL = false;

    private int crlCacheSize = 16;
    private int crlNegativeCacheMinutes = 60;
    private int crlUpdateIntervalMinutes = 60;
    private boolean downloadCRLs = false;

    private boolean checkOCSP = true;
    private boolean requireOCSP = false;
    private int ocspCacheSize = 64;

    private URI systemOCSPUri;
    private boolean checkAllUsingSystemOCSP;
    private X509Certificate systemOCSPCert;

    // http params
    private int readTimeout = 500;
    private int connectTimeout = 1000;
    private int maxRedirects = 1;


    /**
     * @return the checkOnlyEndEntityCerts
     */
    @Override
    public boolean isCheckOnlyEndEntityCerts () {
        return this.checkOnlyEndEntityCerts;
    }


    /**
     * @param checkOnlyEndEntityCerts
     *            the checkOnlyEndEntityCerts to set
     */
    public void setCheckOnlyEndEntityCerts ( boolean checkOnlyEndEntityCerts ) {
        this.checkOnlyEndEntityCerts = checkOnlyEndEntityCerts;
    }


    /**
     * @return the checkCRL
     */
    @Override
    public boolean isCheckCRL () {
        return this.checkCRL;
    }


    /**
     * @param checkCRL
     *            the checkCRL to set
     */
    public void setCheckCRL ( boolean checkCRL ) {
        this.checkCRL = checkCRL;
    }


    /**
     * @return the requireCRL
     */
    @Override
    public boolean isRequireCRL () {
        return this.requireCRL;
    }


    /**
     * @param requireCRL
     *            the requireCRL to set
     */
    public void setRequireCRL ( boolean requireCRL ) {
        this.requireCRL = requireCRL;
    }


    /**
     * @return the ignoreNoCRL
     */
    @Override
    public boolean isIgnoreUnavailableCRL () {
        return this.ignoreNoCRL;
    }


    /**
     * @param ignoreNoCRL
     *            the ignoreNoCRL to set
     */
    public void setIgnoreNoCRL ( boolean ignoreNoCRL ) {
        this.ignoreNoCRL = ignoreNoCRL;
    }


    /**
     * @return the ignoreExpiredCRL
     */
    @Override
    public boolean isIgnoreExpiredCRL () {
        return this.ignoreExpiredCRL;
    }


    /**
     * @param ignoreExpiredCRL
     *            the ignoreExpiredCRL to set
     */
    public void setIgnoreExpiredCRL ( boolean ignoreExpiredCRL ) {
        this.ignoreExpiredCRL = ignoreExpiredCRL;
    }


    /**
     * @return the crlCacheSize
     */
    @Override
    public int getCrlCacheSize () {
        return this.crlCacheSize;
    }


    /**
     * @param crlCacheSize
     *            the crlCacheSize to set
     */
    public void setCrlCacheSize ( int crlCacheSize ) {
        this.crlCacheSize = crlCacheSize;
    }


    /**
     * @return the crlNegativeCacheMinutes
     */
    @Override
    public int getCrlNegativeCacheMinutes () {
        return this.crlNegativeCacheMinutes;
    }


    /**
     * @param crlNegativeCacheMinutes
     *            the crlNegativeCacheMinutes to set
     */
    public void setCrlNegativeCacheMinutes ( int crlNegativeCacheMinutes ) {
        this.crlNegativeCacheMinutes = crlNegativeCacheMinutes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig#getCrlUpdateIntervalMinutes()
     */
    @Override
    public int getCrlUpdateIntervalMinutes () {
        return this.crlUpdateIntervalMinutes;
    }


    /**
     * @param crlUpdateIntervalMinutes
     *            the crlUpdateIntervalMinutes to set
     */
    public void setCrlUpdateIntervalMinutes ( int crlUpdateIntervalMinutes ) {
        this.crlUpdateIntervalMinutes = crlUpdateIntervalMinutes;
    }


    /**
     * @return the downloadCRLs
     */
    @Override
    public boolean isDownloadCRLs () {
        return this.downloadCRLs;
    }


    /**
     * @param downloadCRLs
     *            the downloadCRLs to set
     */
    public void setDownloadCRLs ( boolean downloadCRLs ) {
        this.downloadCRLs = downloadCRLs;
    }


    /**
     * @return the checkOCSP
     */
    @Override
    public boolean isCheckOCSP () {
        return this.checkOCSP;
    }


    /**
     * @param checkOCSP
     *            the checkOCSP to set
     */
    public void setCheckOCSP ( boolean checkOCSP ) {
        this.checkOCSP = checkOCSP;
    }


    /**
     * @return the requireOCSP
     */
    @Override
    public boolean isRequireOCSP () {
        return this.requireOCSP;
    }


    /**
     * @param requireOCSP
     *            the requireOCSP to set
     */
    public void setRequireOCSP ( boolean requireOCSP ) {
        this.requireOCSP = requireOCSP;
    }


    /**
     * @return the ocspCacheSize
     */
    @Override
    public int getOcspCacheSize () {
        return this.ocspCacheSize;
    }


    /**
     * @param ocspCacheSize
     *            the ocspCacheSize to set
     */
    public void setOcspCacheSize ( int ocspCacheSize ) {
        this.ocspCacheSize = ocspCacheSize;
    }


    /**
     * @return the systemOCSPUri
     */
    @Override
    public URI getSystemOCSPUri () {
        return this.systemOCSPUri;
    }


    /**
     * @param systemOCSPUri
     *            the systemOCSPUri to set
     */
    public void setSystemOCSPUri ( URI systemOCSPUri ) {
        this.systemOCSPUri = systemOCSPUri;
    }


    /**
     * @return the checkAllUsingSystemOCSP
     */
    @Override
    public boolean isCheckAllUsingSystemOCSP () {
        return this.checkAllUsingSystemOCSP;
    }


    /**
     * @param checkAllUsingSystemOCSP
     *            the checkAllUsingSystemOCSP to set
     */
    public void setCheckAllUsingSystemOCSP ( boolean checkAllUsingSystemOCSP ) {
        this.checkAllUsingSystemOCSP = checkAllUsingSystemOCSP;
    }


    /**
     * @return the systemOCSPParams
     */
    @Override
    public X509Certificate getSystemOCSPTrustCert () {
        return this.systemOCSPCert;
    }


    /**
     * @param systemOCSPCert
     *            the systemOCSPCert to set
     */
    public void setSystemOCSPCert ( X509Certificate systemOCSPCert ) {
        this.systemOCSPCert = systemOCSPCert;
    }


    /**
     * @return the readTimeout
     */
    @Override
    public int getReadTimeout () {
        return this.readTimeout;
    }


    /**
     * @param readTimeout
     *            the readTimeout to set
     */
    public void setReadTimeout ( int readTimeout ) {
        this.readTimeout = readTimeout;
    }


    /**
     * @return the connectTimeout
     */
    @Override
    public int getConnectTimeout () {
        return this.connectTimeout;
    }


    /**
     * @param connectTimeout
     *            the connectTimeout to set
     */
    public void setConnectTimeout ( int connectTimeout ) {
        this.connectTimeout = connectTimeout;
    }


    /**
     * @return the maxRedirects
     */
    @Override
    public int getMaxRedirects () {
        return this.maxRedirects;
    }


    /**
     * @param maxRedirects
     *            the maxRedirects to set
     */
    public void setMaxRedirects ( int maxRedirects ) {
        this.maxRedirects = maxRedirects;
    }

}

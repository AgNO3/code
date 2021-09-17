/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.tls.internal;


import java.net.URI;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class AdaptedTLSConfiguration implements InternalTLSConfiguration {

    private final SSLClientConfiguration cfg;
    private final KeyManager[] keyManagers;
    private final TrustManager[] trustManagers;
    private final HostnameVerifier hostnameVerifier;
    private Set<PublicKey> pinnedPublicKeys;


    /**
     * @param cfg
     * @param trustManagers
     * @param hv
     * @param pinnedPublicKeys
     * 
     */
    public AdaptedTLSConfiguration ( SSLClientConfiguration cfg, TrustManager[] trustManagers, HostnameVerifier hv,
            Set<PublicKey> pinnedPublicKeys ) {
        this(cfg, new KeyManager[0], trustManagers, hv, pinnedPublicKeys);
    }


    /**
     * @param cfg
     * @param keyManagers
     * @param trustManagers
     * @param hv
     * @param pinnedPublicKeys
     */
    public AdaptedTLSConfiguration ( SSLClientConfiguration cfg, KeyManager[] keyManagers, TrustManager[] trustManagers, HostnameVerifier hv,
            Set<PublicKey> pinnedPublicKeys ) {
        this.cfg = cfg;
        this.keyManagers = keyManagers;
        this.trustManagers = trustManagers;
        this.hostnameVerifier = hv;
        this.pinnedPublicKeys = pinnedPublicKeys;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getId()
     */
    @Override
    public String getId () {
        return "test-" + this.cfg.getId(); //$NON-NLS-1$
    }


    @Override
    public boolean isApplicable ( String role, String subsystem, URI uri ) {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getCipherSuites()
     */
    @Override
    public List<String> getCipherSuites () {
        return this.cfg.getSecurityMode().getCiphers();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#useServerCipherPreferences()
     */
    @Override
    public boolean useServerCipherPreferences () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getProtocols()
     */
    @Override
    public List<String> getProtocols () {
        return this.cfg.getSecurityMode().getProtocols();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getPinPublicKeys()
     */
    @Override
    public Set<PublicKey> getPinPublicKeys () {
        return this.pinnedPublicKeys;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getKeyStoreId()
     */
    @Override
    public String getKeyStoreId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getTrustStoreId()
     */
    @Override
    public String getTrustStoreId () {
        return this.cfg.getTruststoreAlias();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getHostnameVerifierId()
     */
    @Override
    public String getHostnameVerifierId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getRequireClientAuth()
     */
    @Override
    public boolean getRequireClientAuth () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getRequestClientAuth()
     */
    @Override
    public boolean getRequestClientAuth () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#isEnableServerSNI()
     */
    @Override
    public boolean isEnableServerSNI () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.InternalTLSConfiguration#getSniMatchers()
     */
    @Override
    public Collection<SNIMatcher> getSniMatchers () {
        return Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.InternalTLSConfiguration#getKeyManagers()
     */
    @Override
    public KeyManager[] getKeyManagers () {
        return this.keyManagers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.InternalTLSConfiguration#getTrustManagers()
     */
    @Override
    public TrustManager[] getTrustManagers () throws CryptoException {
        return this.trustManagers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.InternalTLSConfiguration#getHostnameVerifier()
     */
    @Override
    public HostnameVerifier getHostnameVerifier () {
        return this.hostnameVerifier;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.InternalTLSConfiguration#adaptParameters(javax.net.ssl.SSLParameters)
     */
    @Override
    public SSLParameters adaptParameters ( SSLParameters sslParameters ) {
        return sslParameters;
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.security.KeyStore;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXCertPathChecker;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig;
import eu.agno3.runtime.crypto.truststore.TruststoreConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class TruststoreConfigImpl extends AbstractTruststoreConfig implements TruststoreConfig {

    private KeyStore trustStore;
    private String id;
    private RevocationConfig revocationConfig;


    /**
     * @param name
     * @param trustStore
     * @param revocationConfig
     * @param ppf
     */
    public TruststoreConfigImpl ( String name, KeyStore trustStore, RevocationConfig revocationConfig, PKIXParameterFactory ppf ) {
        super(ppf);
        this.id = name;
        this.trustStore = trustStore;
        this.revocationConfig = revocationConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getTrustStore()
     */
    @Override
    public KeyStore getTrustStore () {
        return this.trustStore;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#isCheckRevocation()
     */
    @Override
    public boolean isCheckRevocation () {
        return this.revocationConfig != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getExtraCertPathCheckers()
     */
    @Override
    public PKIXCertPathChecker[] getExtraCertPathCheckers () throws CryptoException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getExtraCertStores()
     */
    @Override
    public CertStore[] getExtraCertStores () throws CryptoException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getConstraint()
     */
    @Override
    public CertSelector getConstraint () throws CryptoException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getRevocationConfig()
     */
    @Override
    public RevocationConfig getRevocationConfig () {
        return this.revocationConfig;
    }

}

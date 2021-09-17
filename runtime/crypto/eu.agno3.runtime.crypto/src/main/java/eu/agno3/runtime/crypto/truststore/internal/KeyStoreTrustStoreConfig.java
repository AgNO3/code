/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.security.KeyStore;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXCertPathChecker;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class KeyStoreTrustStoreConfig extends AbstractTruststoreConfig implements TrustConfiguration {

    private KeyStoreConfiguration service;


    /**
     * @param ksConfig
     * @param ppf
     */
    public KeyStoreTrustStoreConfig ( KeyStoreConfiguration ksConfig, PKIXParameterFactory ppf ) {
        super(ppf);
        this.service = ksConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getTrustStore()
     */
    @Override
    public KeyStore getTrustStore () {
        return this.service.getKeyStore();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getId()
     */
    @Override
    public String getId () {
        return "keyStore:" + this.service.getId(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#isCheckRevocation()
     */
    @Override
    public boolean isCheckRevocation () {
        return false;
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
        return null;
    }

}

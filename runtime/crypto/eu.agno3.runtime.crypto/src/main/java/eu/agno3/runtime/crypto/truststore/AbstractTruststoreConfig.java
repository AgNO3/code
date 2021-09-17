/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.02.2016 by mbechler
 */
package eu.agno3.runtime.crypto.truststore;


import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.PKIXBuilderParameters;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.truststore.internal.AllInvalidTrustManagerFactory;


/**
 * @author mbechler
 *
 */
public abstract class AbstractTruststoreConfig implements TruststoreConfig {

    private static final Logger log = Logger.getLogger(AbstractTruststoreConfig.class);

    private TrustManagerFactory trustManagerFactory;
    private PKIXParameterFactory pkixParameterFactory;


    /**
     * 
     */
    public AbstractTruststoreConfig () {}


    /**
     * @param ppf
     */
    public AbstractTruststoreConfig ( PKIXParameterFactory ppf ) {
        this.pkixParameterFactory = ppf;
    }


    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        this.pkixParameterFactory = ppf;
    }


    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        if ( this.pkixParameterFactory == ppf ) {
            this.pkixParameterFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getTrustManagerFactory()
     */
    @Override
    public TrustManagerFactory getTrustManagerFactory () throws CryptoException {
        if ( this.trustManagerFactory != null ) {
            return this.trustManagerFactory;
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Initialize truststore " + this.getId()); //$NON-NLS-1$
            }
            CertPathTrustManagerParameters spec = makeParameters();
            TrustManagerFactory tmf = new ReloadableTrustManagerFactory(getId(), new AllInvalidTrustManagerFactory(getId()));
            if ( spec != null ) {
                tmf.init(spec);
            }
            this.trustManagerFactory = tmf;
            return this.trustManagerFactory;
        }
        catch (
            GeneralSecurityException |
            CryptoException e ) {
            throw new CryptoException("Failed to create trust manager factory", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws KeyStoreException
     * @throws InvalidAlgorithmParameterException
     * @throws CryptoException
     */
    CertPathTrustManagerParameters makeParameters () throws KeyStoreException, InvalidAlgorithmParameterException, CryptoException {
        KeyStore trustStore = getTrustStore();
        if ( trustStore == null || !trustStore.aliases().hasMoreElements() ) {
            log.warn("Invalid/empty truststore for " + this.getId()); //$NON-NLS-1$
            return null;
        }
        PKIXBuilderParameters params = this.pkixParameterFactory
                .makePKIXParameters(trustStore, null, this.getExtraCertStores(), this.getExtraCertPathCheckers(), this.getConstraint());
        CertPathTrustManagerParameters spec = new CertPathTrustManagerParameters(params);
        return spec;
    }


    protected void reload () throws CryptoException {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Reloading truststore config " + this.getId()); //$NON-NLS-1$
            }
            CertPathTrustManagerParameters params = makeParameters();
            getTrustManagerFactory().init(params);
        }
        catch (
            InvalidAlgorithmParameterException |
            KeyStoreException e ) {
            throw new CryptoException("Failed to reload trust store", e); //$NON-NLS-1$
        }
    }
}

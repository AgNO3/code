/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXCertPathChecker;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.update.UpdateTrustConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    UpdateTrustConfiguration.class, TrustConfiguration.class
} )
public class UpdateTrustConfigurationImpl extends AbstractTruststoreConfig implements UpdateTrustConfiguration {

    private static final Logger log = Logger.getLogger(UpdateTrustConfigurationImpl.class);
    private KeyStore truststore;

    private TrustConfiguration delegate;


    /**
     * 
     */
    public UpdateTrustConfigurationImpl () {}


    /**
     * @param ts
     */
    private UpdateTrustConfigurationImpl ( KeyStore ts ) {
        this.truststore = ts;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            URL resource = this.getClass().getClassLoader().getResource("trust.jks"); //$NON-NLS-1$
            if ( resource == null ) {
                throw new IOException("Keystore not found"); //$NON-NLS-1$
            }
            try ( InputStream is = resource.openStream() ) {
                ks.load(is, null);
            }
            this.truststore = ks;
        }
        catch ( Exception e ) {
            log.error("Failed to load truststore", e); //$NON-NLS-1$
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, target = "(instanceId=update)" )
    protected synchronized void setDelegate ( TrustConfiguration tc ) {
        this.delegate = tc;
    }


    protected synchronized void unsetDelegate ( TrustConfiguration tc ) {
        if ( this.delegate == tc ) {
            this.delegate = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#setPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.setPKIXParameterFactory(ppf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#unsetPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.unsetPKIXParameterFactory(ppf);
    }


    @Override
    public UpdateTrustConfiguration getFallback () {
        return new UpdateTrustConfigurationImpl(this.truststore);
    }


    @Override
    public boolean hasDelegate () {
        return this.delegate == null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getTrustStore()
     */
    @Override
    public KeyStore getTrustStore () {
        if ( this.delegate != null ) {
            return this.delegate.getTrustStore();
        }
        log.trace("Using builtin fallback trust store"); //$NON-NLS-1$
        return this.truststore;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getId()
     */
    @Override
    public String getId () {
        return "update"; //$NON-NLS-1$
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

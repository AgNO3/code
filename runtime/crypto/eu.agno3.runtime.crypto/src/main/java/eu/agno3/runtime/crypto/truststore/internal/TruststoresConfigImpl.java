/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.truststore.TruststoresConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = TruststoresConfig.class, configurationPid = TruststoresConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TruststoresConfigImpl implements TruststoresConfig {

    private static final String DEFAULT_TRUST_STORE_BASE = "/etc/truststores/"; //$NON-NLS-1$
    private File trustStoreBase;


    /**
     * 
     */
    public TruststoresConfigImpl () {}


    /**
     * @param base
     */
    public TruststoresConfigImpl ( File base ) {
        this.trustStoreBase = base;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException {

        String baseSpec = (String) ctx.getProperties().get("trustStoreBase"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(baseSpec) ) {
            this.trustStoreBase = new File(baseSpec.trim());
        }
        else {
            this.trustStoreBase = new File(DEFAULT_TRUST_STORE_BASE);
        }

        if ( !this.trustStoreBase.exists() ) {
            this.trustStoreBase.mkdirs();
        }

        if ( !this.trustStoreBase.canRead() || !this.trustStoreBase.isDirectory() ) {
            throw new CryptoException("Illegal trust store directory " + this.trustStoreBase); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.TruststoresConfig#getTruststoreBaseDirectory()
     */
    @Override
    public File getTruststoreBaseDirectory () {
        return this.trustStoreBase;
    }

}

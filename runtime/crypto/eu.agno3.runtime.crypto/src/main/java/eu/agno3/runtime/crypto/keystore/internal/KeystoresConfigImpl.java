/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeystoresConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = KeystoresConfig.class, configurationPid = KeystoresConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KeystoresConfigImpl implements KeystoresConfig {

    private static final String DEFAULT_KEYSTORE_BASE = "/etc/keystores/"; //$NON-NLS-1$
    private File keyStoreBase;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException {
        String keyStoreBaseSpec = (String) ctx.getProperties().get("keyStoreBase"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(keyStoreBaseSpec) ) {
            this.keyStoreBase = new File(keyStoreBaseSpec.trim());
        }
        else {
            this.keyStoreBase = new File(DEFAULT_KEYSTORE_BASE);
        }

        if ( !this.keyStoreBase.exists() ) {
            this.keyStoreBase.mkdirs();
        }

        if ( !this.keyStoreBase.isDirectory() || !this.keyStoreBase.canRead() ) {
            throw new CryptoException(String.format("Key store base %s directory does not exist or is inaccessible", this.keyStoreBase)); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.keystore.KeystoresConfig#getKeystoreBaseDirectory()
     */
    @Override
    public File getKeystoreBaseDirectory () {
        return this.keyStoreBase;
    }

}

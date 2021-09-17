/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KeystoresConfig.class )
public interface KeystoresConfigMutable extends KeystoresConfig {

    /**
     * @param keystores
     */
    void setKeystores ( Set<KeystoreConfig> keystores );

}

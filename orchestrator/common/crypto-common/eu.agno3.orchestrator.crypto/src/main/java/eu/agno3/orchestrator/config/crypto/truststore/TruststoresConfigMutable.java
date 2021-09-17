/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TruststoresConfig.class )
public interface TruststoresConfigMutable extends TruststoresConfig {

    /**
     * @param truststores
     */
    void setTruststores ( Set<TruststoreConfig> truststores );
}

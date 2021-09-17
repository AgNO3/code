/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareAdvancedConfig.class )
public interface FileshareAdvancedConfigMutable extends FileshareAdvancedConfig {

    /**
     * @param runtimeConfiguration
     */
    void setRuntimeConfiguration ( RuntimeConfigurationMutable runtimeConfiguration );

}

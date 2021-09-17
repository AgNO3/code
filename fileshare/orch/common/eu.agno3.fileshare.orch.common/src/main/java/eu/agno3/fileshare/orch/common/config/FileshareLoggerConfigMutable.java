/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.logger.LoggerConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareLoggerConfig.class )
public interface FileshareLoggerConfigMutable extends FileshareLoggerConfig {

    /**
     * @param unauthLoggerConfig
     */
    void setUnauthLoggerConfig ( LoggerConfigurationMutable unauthLoggerConfig );


    /**
     * @param defaultLoggerConfig
     */
    void setDefaultLoggerConfig ( LoggerConfigurationMutable defaultLoggerConfig );

}

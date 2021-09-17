/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorConfiguration.class )
public interface OrchestratorConfigurationMutable extends OrchestratorConfiguration {

    /**
     * 
     * @param eventLogConfig
     */
    void setEventLogConfig ( OrchestratorEventLogConfigurationMutable eventLogConfig );


    /**
     * 
     * @param authenticatorsConfig
     */
    void setAuthenticationConfig ( OrchestratorAuthenticationConfigurationMutable authenticatorsConfig );


    /**
     * 
     * @param webEndpointConfig
     */
    void setWebConfig ( OrchestratorWebConfigurationMutable webEndpointConfig );


    /**
     * @param advancedConfig
     */
    void setAdvancedConfig ( OrchestratorAdvancedConfigurationMutable advancedConfig );

}

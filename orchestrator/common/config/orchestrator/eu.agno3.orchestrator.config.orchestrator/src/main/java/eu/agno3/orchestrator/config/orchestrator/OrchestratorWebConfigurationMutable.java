/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import eu.agno3.orchestrator.config.web.WebEndpointConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorWebConfiguration.class )
public interface OrchestratorWebConfigurationMutable extends OrchestratorWebConfiguration {

    /**
     * 
     * @param apiEndpointConfig
     */
    void setApiEndpointConfig ( WebEndpointConfigMutable apiEndpointConfig );


    /**
     * 
     * @param webEndpointConfig
     */
    void setWebEndpointConfig ( WebEndpointConfigMutable webEndpointConfig );


    /**
     * 
     * @param themeLibrary
     */
    void setThemeLibrary ( String themeLibrary );

}

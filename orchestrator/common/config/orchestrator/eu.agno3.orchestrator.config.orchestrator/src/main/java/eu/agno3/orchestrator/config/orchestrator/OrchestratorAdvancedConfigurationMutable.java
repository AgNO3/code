/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorAdvancedConfiguration.class )
public interface OrchestratorAdvancedConfigurationMutable extends OrchestratorAdvancedConfiguration {

    /**
     * @param runtimeConfig
     */
    void setRuntimeConfig ( RuntimeConfigurationMutable runtimeConfig );


    /**
     * 
     * @param tempStorage
     */
    void setTempStorage ( String tempStorage );


    /**
     * 
     * @param dataStorage
     */
    void setDataStorage ( String dataStorage );

}

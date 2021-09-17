/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RuntimeConfiguration.class )
public interface RuntimeConfigurationMutable extends RuntimeConfiguration {

    /**
     * @param memoryLimit
     */
    void setMemoryLimit ( Long memoryLimit );


    /**
     * @param autoMemoryLimit
     */
    void setAutoMemoryLimit ( Boolean autoMemoryLimit );


    /**
     * @param debugPackages
     */
    void setDebugPackages ( Set<String> debugPackages );


    /**
     * @param tracePackages
     */
    void setTracePackages ( Set<String> tracePackages );

}

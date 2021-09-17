/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface ComponentConfigurationProvider <T extends ComponentConfig> {

    /**
     * 
     * @param componentId
     * @return the component configuration
     * @throws ComponentConfigurationException
     */
    T getConfiguration ( @NonNull UUID componentId ) throws ComponentConfigurationException;
}

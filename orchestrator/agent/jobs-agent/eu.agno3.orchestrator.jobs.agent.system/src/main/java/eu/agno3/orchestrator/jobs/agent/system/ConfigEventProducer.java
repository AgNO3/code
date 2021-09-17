/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface ConfigEventProducer extends SystemService {

    /**
     * @param anchor
     * @param service
     * @param config
     */
    void configApplied ( @Nullable StructuralObject anchor, ServiceStructuralObject service, ConfigurationInstance config );


    /**
     * @param anchor
     * @param service
     * @param config
     */
    void configFailed ( @Nullable StructuralObject anchor, ServiceStructuralObject service, ConfigurationInstance config );

}

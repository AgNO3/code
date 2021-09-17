/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;


/**
 * @author mbechler
 * 
 */
public interface ResourceLibraryRegistry {

    /**
     * @param type
     * @return the descriptor for the given named type
     * @throws ModelServiceException
     */
    ResourceLibraryDescriptor getDescriptor ( String type ) throws ModelServiceException;


    /**
     * @return the known resource library types
     */
    Set<String> getResourceLibraryTypes ();

}
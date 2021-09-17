/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.realm.InstanceStatus;


/**
 * @author mbechler
 *
 */
public class InstanceStatusCache extends InstanceStatus {

    private final Map<UUID, Set<@NonNull ResourceLibraryReference>> resourceLibraries = new HashMap<>();


    /**
     * @return the resourceLibraries
     */
    public Map<UUID, Set<@NonNull ResourceLibraryReference>> getResourceLibraries () {
        return this.resourceLibraries;
    }

}

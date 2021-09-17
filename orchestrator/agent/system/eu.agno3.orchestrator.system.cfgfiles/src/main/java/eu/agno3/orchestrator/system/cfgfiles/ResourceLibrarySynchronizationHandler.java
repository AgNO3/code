/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles;


import java.util.List;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;


/**
 * @author mbechler
 *
 */
public interface ResourceLibrarySynchronizationHandler {

    /**
     * @return the handled type
     */
    String getType ();


    /**
     * @param serviceTarget
     * @param hint
     * @return present resource library entries
     * @throws ResourceLibraryException
     */
    List<ResourceLibraryEntry> list ( StructuralObjectReference serviceTarget, String hint ) throws ResourceLibraryException;


    /**
     * @param serviceTarget
     * @param hint
     * @param update
     * @param add
     * @param delete
     * @return the new library entries
     * @throws ResourceLibraryException
     */
    List<ResourceLibraryEntry> synchronize ( StructuralObjectReference serviceTarget, String hint, Set<ResourceLibraryEntry> update,
            Set<ResourceLibraryEntry> add, Set<ResourceLibraryEntry> delete ) throws ResourceLibraryException;

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:storage" )
public interface FileshareStorageConfig extends ConfigurationObject {

    /**
     * 
     * @return storage used for local data, e.g. database, caches, sessions, elasticsearch index
     */
    String getLocalStorage ();


    /**
     * 
     * @return storage for file data (including incomplete uploads, temporary files)
     */
    String getFileStorage ();


    /**
     * @return the defined passtrough groups
     */
    @ReferencedObject
    @Valid
    Set<FilesharePassthroughGroup> getPassthroughGroups ();

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.util.UUID;

import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:storage:mount:local" )
public interface LocalMountEntry extends MountEntry {

    /**
     * 
     * @return the filesystem UUID to match
     */
    UUID getMatchUuid ();


    /**
     * 
     * @return the filesystem label to match
     */
    String getMatchLabel ();

}

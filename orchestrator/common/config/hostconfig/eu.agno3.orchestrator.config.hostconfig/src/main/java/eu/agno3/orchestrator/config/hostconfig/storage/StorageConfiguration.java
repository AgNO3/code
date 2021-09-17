/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:storage" )
public interface StorageConfiguration extends ConfigurationObject {

    /**
     * @return filesystems to mount
     */
    @Valid
    @ReferencedObject
    Set<MountEntry> getMountEntries ();


    /**
     * @return the storage to use for backups
     */
    String getBackupStorage ();
}

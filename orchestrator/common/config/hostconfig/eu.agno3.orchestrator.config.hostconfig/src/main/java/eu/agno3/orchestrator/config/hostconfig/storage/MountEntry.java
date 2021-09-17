/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:storage:mount" )
public interface MountEntry extends ConfigurationObject {

    /**
     * 
     * @return the storage alias
     */
    @ValidReferenceAlias
    @ObjectName
    String getAlias ();


    /**
     * 
     * @return the mount type
     */
    @NotNull
    MountType getMountType ();
}

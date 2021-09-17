/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( StaticRolesConfigObjectTypeDescriptor.TYPE_NAME )
public interface StaticRolesConfig extends ConfigurationObject {

    /**
     * @return the defined roles
     */
    @ReferencedObject
    Set<RoleConfig> getRoles ();

}

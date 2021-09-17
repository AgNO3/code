/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigMutable;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareAuthConfig.class )
public interface FileshareAuthConfigMutable extends FileshareAuthConfig {

    /**
     * @param authenticators
     */
    void setAuthenticators ( AuthenticatorsConfigMutable authenticators );


    /**
     * @param roleConfig
     */
    void setRoleConfig ( StaticRolesConfigMutable roleConfig );


    /**
     * @param noSynchronizationRoles
     */
    void setNoSynchronizationRoles ( Set<String> noSynchronizationRoles );

}

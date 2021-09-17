/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import eu.agno3.orchestrator.config.auth.StaticRolesConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorAuthenticationConfiguration.class )
public interface OrchestratorAuthenticationConfigurationMutable extends OrchestratorAuthenticationConfiguration {

    /**
     * @param roleConfig
     */
    void setRoleConfig ( StaticRolesConfigMutable roleConfig );

}

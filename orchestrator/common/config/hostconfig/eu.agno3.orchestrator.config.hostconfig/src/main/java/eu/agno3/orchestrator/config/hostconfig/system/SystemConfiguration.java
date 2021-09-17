/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.system;


import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * Basic system configuration
 * 
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:system" )
public interface SystemConfiguration extends ConfigurationObject {

    /**
     * 
     * @return whether to enable ssh server access
     */
    @NotNull ( groups = Materialized.class )
    Boolean getEnableSshAccess ();


    /**
     * @return whether to require ssh key authentication
     */
    @NotNull ( groups = Materialized.class )
    Boolean getSshKeyOnly ();


    /**
     * @return public keys allowed to connect via SSH
     */
    @Valid
    Set<PublicKeyEntry> getAdminSshPublicKeys ();


    /**
     * 
     * @return [0,100], controls the system's tendency to swap pages, higher means more swapping
     */
    @NotNull ( groups = Materialized.class )
    @Min ( value = 0 )
    @Max ( value = 100 )
    Integer getSwapiness ();


    /**
     * @return the agent runtime configuration
     */
    @ReferencedObject
    @Valid
    RuntimeConfiguration getAgentConfig ();

}

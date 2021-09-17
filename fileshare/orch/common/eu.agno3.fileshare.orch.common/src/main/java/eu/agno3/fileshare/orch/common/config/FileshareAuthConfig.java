/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfig;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( FileshareAuthConfigObjectTypeDescriptor.TYPE_NAME )
public interface FileshareAuthConfig extends ConfigurationObject {

    /**
     * 
     * @return the authenticator
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    AuthenticatorsConfig getAuthenticators ();


    /**
     * @return the static role configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    StaticRolesConfig getRoleConfig ();


    /**
     * @return roles which are not synchronized
     */
    Set<String> getNoSynchronizationRoles ();
}

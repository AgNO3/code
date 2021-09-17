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

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.terms.TermsConfiguration;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( FileshareUserConfigObjectTypeDescriptor.TYPE_NAME )
public interface FileshareUserConfig extends ConfigurationObject {

    /**
     * 
     * @return quota config
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareUserQuotaConfig getQuotaConfig ();


    /**
     * @return self service config
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareUserSelfServiceConfig getSelfServiceConfig ();


    /**
     * @return user trust level configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareUserTrustLevelConfig getUserTrustLevelConfig ();


    /**
     * @return the terms of use configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    TermsConfiguration getTermsConfig ();


    /**
     * @return the roles for which the storage of own files is disabled
     */
    Set<String> getNoSubjectRootRoles ();


    /**
     * @return the roles which are assigned by default when administratively creating a user
     */
    Set<String> getDefaultRoles ();

}

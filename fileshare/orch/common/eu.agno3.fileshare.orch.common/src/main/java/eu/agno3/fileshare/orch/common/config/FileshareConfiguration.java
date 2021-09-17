/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( FileshareConfigurationObjectTypeDescriptor.TYPE_NAME )
public interface FileshareConfiguration extends ConfigurationInstance {

    /**
     * 
     * @return the web configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareWebConfig getWebConfiguration ();


    /**
     * 
     * @return the user configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareUserConfig getUserConfiguration ();


    /**
     * 
     * @return the content configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareContentConfig getContentConfiguration ();


    /**
     * 
     * @return the notification configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareNotificationConfig getNotificationConfiguration ();


    /**
     * 
     * @return the security policy configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareSecurityPolicyConfig getSecurityPolicyConfiguration ();


    /**
     * @return the authentication configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareAuthConfig getAuthConfiguration ();


    /**
     * @return the storage configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareStorageConfig getStorageConfiguration ();


    /**
     * @return the advanced configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareAdvancedConfig getAdvancedConfiguration ();


    /**
     * @return the logger configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    FileshareLoggerConfig getLoggerConfiguration ();

}

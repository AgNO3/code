/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


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
@ObjectTypeName ( "urn:agno3:objects:1.0:orchestrator" )
public interface OrchestratorConfiguration extends ConfigurationInstance {

    /**
     * 
     * @return web endpoint configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    OrchestratorWebConfiguration getWebConfig ();


    /**
     * 
     * @return the authenticator
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    OrchestratorAuthenticationConfiguration getAuthenticationConfig ();


    /**
     * 
     * @return the event log configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    OrchestratorEventLogConfiguration getEventLogConfig ();


    /**
     * 
     * @return the advanced configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    OrchestratorAdvancedConfiguration getAdvancedConfig ();

}

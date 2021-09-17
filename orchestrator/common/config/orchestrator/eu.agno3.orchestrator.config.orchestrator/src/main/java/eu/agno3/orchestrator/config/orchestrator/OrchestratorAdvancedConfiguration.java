/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:orchestrator:advanced" )
public interface OrchestratorAdvancedConfiguration extends ConfigurationObject {

    /**
     * @return the runtime configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    RuntimeConfiguration getRuntimeConfig ();


    /**
     * 
     * @return the storage to use for temporary data
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    @NotEmpty ( groups = {
        Materialized.class
    } )
    String getTempStorage ();


    /**
     * 
     * @return the storage to use for persistent data
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    @NotEmpty ( groups = {
        Materialized.class
    } )
    String getDataStorage ();
}

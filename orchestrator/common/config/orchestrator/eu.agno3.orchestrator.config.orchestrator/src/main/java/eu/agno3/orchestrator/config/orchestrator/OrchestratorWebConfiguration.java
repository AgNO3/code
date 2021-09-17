/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.web.WebEndpointConfig;


/**
 * @author mbechler
 *
 */

@ObjectTypeName ( "urn:agno3:objects:1.0:orchestrator:web" )
public interface OrchestratorWebConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the theme library to use
     */
    String getThemeLibrary ();


    /**
     * 
     * @return web endpoint configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    WebEndpointConfig getWebEndpointConfig ();


    /**
     * 
     * @return web endpoint configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    WebEndpointConfig getApiEndpointConfig ();
}

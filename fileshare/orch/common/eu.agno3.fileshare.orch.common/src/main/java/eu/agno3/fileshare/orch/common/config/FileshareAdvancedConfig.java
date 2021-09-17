/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:advanced" )
public interface FileshareAdvancedConfig extends ConfigurationObject {

    /**
     * @return the runtime configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    RuntimeConfiguration getRuntimeConfiguration ();

}

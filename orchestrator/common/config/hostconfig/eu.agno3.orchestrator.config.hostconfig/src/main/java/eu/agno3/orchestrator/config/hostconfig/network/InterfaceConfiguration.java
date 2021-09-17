/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:network:interfaces" )
public interface InterfaceConfiguration extends ConfigurationObject {

    /**
     * @return interface configuration entries
     */
    @ReferencedObject
    @NotNull ( groups = Materialized.class )
    @NotEmpty ( groups = Materialized.class )
    @Valid
    Set<InterfaceEntry> getInterfaces ();
}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:passthroughGroup" )
public interface FilesharePassthroughGroup extends ConfigurationObject {

    /**
     * 
     * @return the group name
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    @NotEmpty ( groups = {
        Materialized.class
    } )
    @ObjectName
    @ValidReferenceAlias
    String getGroupName ();


    /**
     * 
     * @return the assigned security policy
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    @NotEmpty ( groups = {
        Materialized.class
    } )
    String getSecurityPolicy ();


    /**
     * @return whether to generally allow sharing
     */
    Boolean getAllowSharing ();
}

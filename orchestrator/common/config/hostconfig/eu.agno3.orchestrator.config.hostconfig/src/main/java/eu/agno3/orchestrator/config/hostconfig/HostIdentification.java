/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import eu.agno3.orchestrator.config.model.base.config.ImmutableType;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Abstract;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.runtime.validation.domain.ValidDomainName;
import eu.agno3.runtime.validation.domain.ValidHostNameLabel;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:identification" )
@ImmutableType ( HostIdentification.class )
public interface HostIdentification extends ConfigurationObject {

    /**
     * @return the hosts hostname
     */
    @Null ( groups = Abstract.class )
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ValidHostNameLabel
    String getHostName ();


    /**
     * 
     * @return the hosts domainname
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    @ValidDomainName
    String getDomainName ();
}

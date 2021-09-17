/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.List;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:user:quota" )
public interface FileshareUserQuotaConfig extends ConfigurationObject {

    /**
     * 
     * @return disable tracking of cumulative directory sizes if not needed for quotas
     */
    Boolean getDisableSizeTrackingWithoutQuota ();


    /**
     * @return whether to set quotas on user initialization
     */
    Boolean getEnableDefaultQuota ();


    /**
     * 
     * @return default quota
     */
    Long getGlobalDefaultQuota ();


    /**
     * 
     * @return rules for matching rules against a user quota
     */
    @ReferencedObject
    @Valid
    List<FileshareQuotaRule> getDefaultQuotaRules ();

}

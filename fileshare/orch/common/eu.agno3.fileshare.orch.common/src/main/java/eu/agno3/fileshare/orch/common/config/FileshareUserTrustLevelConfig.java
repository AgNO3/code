/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:user:trustLevels" )
public interface FileshareUserTrustLevelConfig extends ConfigurationObject {

    /**
     * 
     * @return the trustlevel to display for link shares
     */
    String getLinkTrustLevel ();


    /**
     * 
     * @return the trustlevel to display for mail shares
     */
    String getMailTrustLevel ();


    /**
     * 
     * @return the trustlevel to display for groups
     */
    String getGroupTrustLevel ();


    /**
     * 
     * @return defined trust levels
     */
    @ReferencedObject
    @Valid
    Set<FileshareUserTrustLevel> getTrustLevels ();

}

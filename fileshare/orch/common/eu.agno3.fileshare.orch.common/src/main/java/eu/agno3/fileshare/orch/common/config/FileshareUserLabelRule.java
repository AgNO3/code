/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:user:labelRule" )
public interface FileshareUserLabelRule extends ConfigurationObject {

    /**
     * 
     * @return the role to match
     */
    String getMatchRole ();


    /**
     * 
     * @return the label to assign
     */
    String getAssignLabel ();
}

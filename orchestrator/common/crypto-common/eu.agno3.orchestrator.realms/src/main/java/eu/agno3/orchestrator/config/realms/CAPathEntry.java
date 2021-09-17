/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:realms:capath" )
public interface CAPathEntry extends ConfigurationObject {

    /**
     * 
     * @return the target realm
     */
    String getTargetRealm ();


    /**
     * 
     * @return the next realm in the trust path
     */
    String getNextRealm ();

}

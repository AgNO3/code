/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.net.URI;

import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:storage:mount:nfs" )
public interface NFSMountEntry extends MountEntry {

    /**
     * 
     * @return keytab to use for authentication
     */
    String getAuthKeytab ();


    /**
     * 
     * @return realm to use for authentication
     */
    String getAuthRealm ();


    /**
     * 
     * @return nfs security
     */
    NFSSecurityType getSecurityType ();


    /**
     * 
     * @return nfs version
     */
    NFSVersion getNfsVersion ();


    /**
     * 
     * @return nfs target uri
     */
    URI getTarget ();

}

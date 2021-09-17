/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:authenticator:ldap:sync" )
public interface LDAPSyncOptions extends ConfigurationObject {

    /**
     * 
     * @return search page size for synchronization
     */
    Integer getPageSize ();


    /**
     * 
     * @return whether to remove users that were removed from the directory
     */
    Boolean getSynchronizeRemovals ();


    /**
     * 
     * @return whether to remove users based on their UUID (otherwise the DN is used)
     */
    Boolean getRemoveUsingUUID ();


    /**
     * @return the interval in which synchronization is performed
     */
    Duration getSyncInterval ();

}

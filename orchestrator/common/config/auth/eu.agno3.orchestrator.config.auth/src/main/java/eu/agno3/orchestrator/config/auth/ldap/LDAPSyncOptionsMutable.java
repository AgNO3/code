/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPSyncOptions.class )
public interface LDAPSyncOptionsMutable extends LDAPSyncOptions {

    /**
     * 
     * @param pageSize
     */
    void setPageSize ( Integer pageSize );


    /**
     * 
     * @param removeUsingUUID
     */
    void setRemoveUsingUUID ( Boolean removeUsingUUID );


    /**
     * 
     * @param synchronizeRemovals
     */
    void setSynchronizeRemovals ( Boolean synchronizeRemovals );


    /**
     * @param syncInterval
     */
    void setSyncInterval ( Duration syncInterval );

}

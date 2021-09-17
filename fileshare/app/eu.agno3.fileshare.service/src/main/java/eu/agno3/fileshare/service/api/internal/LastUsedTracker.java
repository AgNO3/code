/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2017 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public interface LastUsedTracker {

    /**
     * 
     * @param userId
     * @return last used entities with their last usage time
     */
    Map<EntityKey, DateTime> getLastUsedEntities ( UUID userId );


    /**
     * 
     * @param userId
     * @param entity
     */
    void trackUsage ( UUID userId, EntityKey entity );

}
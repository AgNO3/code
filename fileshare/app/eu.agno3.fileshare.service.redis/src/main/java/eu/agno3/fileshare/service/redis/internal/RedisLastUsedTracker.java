/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2017 by mbechler
 */
package eu.agno3.fileshare.service.redis.internal;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.api.internal.LastUsedTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.runtime.redis.RedisClientException;
import eu.agno3.runtime.redis.RedisClientProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = LastUsedTracker.class )
public class RedisLastUsedTracker implements LastUsedTracker {

    private static final Logger log = Logger.getLogger(RedisLastUsedTracker.class);

    private RedisClientProvider client;

    private VFSServiceInternal vfs;


    @Reference
    protected synchronized void setRedisClientProvider ( RedisClientProvider cp ) {
        this.client = cp;
    }


    protected synchronized void unsetRedisClientProvider ( RedisClientProvider cp ) {
        if ( this.client == cp ) {
            this.client = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.LastUsedTracker#getLastUsedEntities(java.util.UUID)
     */
    @Override
    public Map<EntityKey, DateTime> getLastUsedEntities ( UUID userId ) {
        try {
            RMapCache<String, Long> mc = getCache(userId); // $NON-NLS-1$
            Map<EntityKey, DateTime> lastUsed = new HashMap<>();
            for ( Entry<String, Long> e : mc.readAllEntrySet() ) {
                if ( e.getValue() != null ) {
                    lastUsed.put(this.vfs.parseEntityKey(e.getKey()), new DateTime(e.getValue()));
                }
            }
            return lastUsed;
        }
        catch ( RedisClientException e ) {
            log.warn("Failed to fetch last used entities", e); //$NON-NLS-1$
            return Collections.EMPTY_MAP;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.LastUsedTracker#trackUsage(java.util.UUID,
     *      eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public void trackUsage ( UUID userId, EntityKey entity ) {
        try {
            RMapCache<String, Long> mc = getCache(userId); // $NON-NLS-1$
            mc.trySetMaxSize(10);
            if ( log.isDebugEnabled() ) {
                log.debug("Tracking usage " + entity); //$NON-NLS-1$
            }
            mc.putAsync(entity.toString(), System.currentTimeMillis(), 100, TimeUnit.DAYS);
        }
        catch ( RedisClientException e ) {
            log.warn("Failed to track last used entities", e); //$NON-NLS-1$
        }
    }


    private RMapCache<String, Long> getCache ( UUID userId ) throws RedisClientException {
        MapOptions<String, Long> opts = MapOptions.defaults();
        return this.client.getClient().getMapCache("fav-last-used/" + userId, opts); //$NON-NLS-1$
    }

}

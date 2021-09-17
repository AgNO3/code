/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching.internal;


import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.caching.CacheConfig;
import eu.agno3.runtime.util.config.ConfigUtil;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration.MaxDepthExceededBehavior;


/**
 * @author mbechler
 * 
 */

@Component (
    service = CacheRegistrationImpl.class,
    immediate = true,
    configurationPid = CacheConfig.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class CacheRegistrationImpl {

    private static final Logger log = Logger.getLogger(CacheRegistrationImpl.class);

    private CacheManager cacheManager;
    private Cache cache;


    @Reference
    protected synchronized void setCacheManager ( CacheManager cm ) {
        this.cacheManager = cm;
    }


    protected synchronized void unsetCacheManager ( CacheManager cm ) {
        if ( this.cacheManager == cm ) {
            this.cacheManager = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {
        CacheConfiguration cfg = new CacheConfiguration();
        String name = (String) ctx.getProperties().get(CacheConfig.NAME);
        if ( name == null ) {
            throw new IllegalArgumentException("Cache name must be set"); //$NON-NLS-1$
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Activating cache  " + name); //$NON-NLS-1$
        }
        cfg.setName(name);

        configureLimits(ctx, cfg);

        configureTimeouts(ctx, cfg);

        configureSizeOf(ctx, cfg);

        String persistenceSpec = (String) ctx.getProperties().get(CacheConfig.PERSISTENCE);
        cfg.addPersistence( ( new PersistenceConfiguration() ).strategy(persistenceSpec));

        this.cache = new Cache(cfg);
        this.cacheManager.addCache(this.cache);
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Activating cache " + this.cache.getName()); //$NON-NLS-1$
        }
        this.cacheManager.removeCache(this.cache.getName());
    }


    private static void configureTimeouts ( ComponentContext ctx, CacheConfiguration cfg ) {
        String timeToIdleSpec = (String) ctx.getProperties().get(CacheConfig.TIME_TO_IDLE);
        if ( timeToIdleSpec != null ) {
            cfg.setTimeToIdleSeconds(Long.parseLong(timeToIdleSpec));
        }

        String timeToLiveSpec = (String) ctx.getProperties().get(CacheConfig.TIME_TO_IDLE);
        if ( timeToLiveSpec != null ) {
            cfg.setTimeToLiveSeconds(Long.parseLong(timeToLiveSpec));
        }
    }


    private static void configureLimits ( ComponentContext ctx, CacheConfiguration cfg ) {
        String maxBytesMemSpec = (String) ctx.getProperties().get(CacheConfig.MAX_HEAP_SIZE);
        if ( maxBytesMemSpec != null ) {
            cfg.setMaxBytesLocalHeap(maxBytesMemSpec);
        }

        String maxEntriesMemSpec = (String) ctx.getProperties().get(CacheConfig.MAX_HEAP_ENTRIES);
        if ( maxEntriesMemSpec != null ) {
            cfg.setMaxEntriesLocalHeap(Long.parseLong(maxEntriesMemSpec));
        }

        String maxBytesDiskSpec = (String) ctx.getProperties().get(CacheConfig.MAX_DISK_SIZE);
        if ( maxBytesDiskSpec != null ) {
            cfg.setMaxBytesLocalDisk(maxBytesDiskSpec);
        }

        String maxEntriesDiskSpec = (String) ctx.getProperties().get(CacheConfig.MAX_DISK_ENTRIES);
        if ( maxEntriesDiskSpec != null ) {
            cfg.setMaxEntriesLocalDisk(Long.parseLong(maxEntriesDiskSpec));
        }
    }


    /**
     * @param ctx
     * @param cfg
     */
    private static void configureSizeOf ( ComponentContext ctx, CacheConfiguration cfg ) {
        SizeOfPolicyConfiguration sizeOfPolicy = new SizeOfPolicyConfiguration();
        int maxDepth = ConfigUtil.parseInt(ctx.getProperties(), "sizeOfMaxDepth", -1); //$NON-NLS-1$
        if ( maxDepth > 0 ) {
            sizeOfPolicy.maxDepth(maxDepth);
        }
        String abortPolicy = ConfigUtil.parseString(ctx.getProperties(), "sizeOfAbortPolicy", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(abortPolicy) ) {
            sizeOfPolicy.maxDepthExceededBehavior(MaxDepthExceededBehavior.valueOf(abortPolicy.toUpperCase(Locale.ROOT)));
        }
        cfg.sizeOfPolicy(sizeOfPolicy);
    }

}

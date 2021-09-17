/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching.internal;


import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.caching.CacheManagerConfig;
import eu.agno3.runtime.util.osgi.DsUtil;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.management.ManagementService;
import net.sf.ehcache.transaction.manager.TransactionManagerLookup;


/**
 * @author mbechler
 * 
 */
@Component ( configurationPid = CacheManagerConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class CacheManagerRegistration {

    private static final Logger log = Logger.getLogger(CacheManagerRegistration.class);

    private CacheManager cm;
    private ServiceRegistration<CacheManager> reg;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        log.debug("Booting CacheManager"); //$NON-NLS-1$
        this.cm = CacheManager.newInstance(getConfiguration(ctx));
        ManagementService.registerMBeans(this.cm, ManagementFactory.getPlatformMBeanServer(), true, true, true, true, true);
        this.reg = DsUtil.registerSafe(ctx, CacheManager.class, this.cm, new Hashtable<String, Object>());
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        log.debug("Shutting down CacheManager"); //$NON-NLS-1$
        if ( this.reg != null ) {
            DsUtil.unregisterSafe(ctx, this.reg);
            this.reg = null;
        }

        if ( this.cm != null ) {
            this.cm.shutdown();
            this.cm = null;
        }
    }


    @Reference
    protected void setTransactionServiceManagerLookup ( TransactionManagerLookup tsl ) {
        // dep only
    }


    protected void unsetTransactionServiceManagerLookup ( TransactionManagerLookup tsl ) {
        // dep only
    }


    /**
     * @param ctx
     * @return the cache manager configuration
     */
    @SuppressWarnings ( "deprecation" )
    public static Configuration getConfiguration ( ComponentContext ctx ) {
        Configuration cfg = new Configuration();
        String name = (String) ctx.getProperties().get(CacheManagerConfig.NAME);
        if ( name != null ) {
            cfg.setName(name);
        }

        cfg.setDynamicConfig(true);

        // there is no more update check, leave this as a safeguard until it is removed
        cfg.setUpdateCheck(false);

        configureLimits(ctx, cfg);

        String diskPath = (String) ctx.getProperties().get(CacheManagerConfig.DISK_PATH);

        if ( diskPath != null ) {
            cfg.addDiskStore( ( new DiskStoreConfiguration() ).path(diskPath));
        }

        cfg.getTransactionManagerLookupConfiguration().className(TransactionServiceManagerLookup.class.getName());
        return cfg;
    }


    private static void configureLimits ( ComponentContext ctx, Configuration cfg ) {
        String maxBytesMemSpec = (String) ctx.getProperties().get(CacheManagerConfig.MAX_HEAP_SIZE);
        if ( maxBytesMemSpec != null ) {
            cfg.setMaxBytesLocalHeap(maxBytesMemSpec);
        }

        String maxBytesDiskSpec = (String) ctx.getProperties().get(CacheManagerConfig.MAX_DISK_SIZE);
        if ( maxBytesDiskSpec != null ) {
            cfg.setMaxBytesLocalDisk(maxBytesDiskSpec);
        }

    }

}

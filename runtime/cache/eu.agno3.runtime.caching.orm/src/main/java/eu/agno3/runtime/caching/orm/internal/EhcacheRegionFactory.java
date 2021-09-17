/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2014 by mbechler
 */
package eu.agno3.runtime.caching.orm.internal;


import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;

import org.apache.log4j.Logger;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.spi.RegionFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.caching.internal.CacheManagerRegistration;


/**
 * @author mbechler
 * 
 */
@Component ( service = RegionFactory.class, configurationPid = EhcacheRegionFactory.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class EhcacheRegionFactory extends org.hibernate.cache.ehcache.EhCacheRegionFactory {

    private static final Logger log = Logger.getLogger(EhcacheRegionFactory.class);

    /**
     * Configuration PID
     */
    public static final String PID = "db.orm.cache"; //$NON-NLS-1$

    private static final long serialVersionUID = -6872019942577187716L;
    private transient Configuration configuration;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.configuration = CacheManagerRegistration.getConfiguration(ctx);
        if ( this.configuration.getName() == null ) {
            this.configuration.name("ORM"); //$NON-NLS-1$
        }
        CacheConfiguration defaultCache = new CacheConfiguration();
        defaultCache.persistence( ( new PersistenceConfiguration() ).strategy(Strategy.LOCALTEMPSWAP));
        this.configuration.setClassLoader(this.getClass().getClassLoader());
        this.configuration.addDefaultCache(defaultCache);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.hibernate.cache.ehcache.EhCacheRegionFactory#start(org.hibernate.boot.spi.SessionFactoryOptions,
     *      java.util.Properties)
     */
    @Override
    public void start ( SessionFactoryOptions s, Properties p ) {
        if ( this.manager == null ) {
            log.debug("Starting cache manager for ORM"); //$NON-NLS-1$
            this.settings = s;
            this.manager = new CacheManager(this.configuration);
            this.mbeanRegistrationHelper.registerMBean(this.manager, p);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.cache.ehcache.EhCacheRegionFactory#stop()
     */
    @Override
    public void stop () {
        super.stop();

        this.manager = null;
    }

}

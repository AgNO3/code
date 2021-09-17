/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching.internal;


import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.caching.CacheService;
import eu.agno3.runtime.util.classloading.BundleDelegatingClassLoader;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.classloader.ClassLoaderAwareCache;


/**
 * @author mbechler
 * 
 */
@Component ( service = CacheService.class, servicefactory = true )
public class CacheServiceImpl implements CacheService {

    private CacheManager cacheManager;
    private ClassLoader bundleClassLoader;


    @Activate
    protected synchronized void activate ( final ComponentContext context ) {
        this.bundleClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run () {
                return new BundleDelegatingClassLoader(context.getUsingBundle());
            }

        });
    }


    @Deactivate
    protected synchronized void deactivate () {
        this.bundleClassLoader = null;
    }


    @Reference
    protected synchronized void setCacheManager ( CacheManager cm ) {
        this.cacheManager = cm;
    }


    protected synchronized void unsetCacheManager ( CacheManager cm ) {
        if ( this.cacheManager == cm ) {
            this.cacheManager = null;
        }
    }


    /**
     * 
     * @param name
     * @return the cache using the calling bundles classloader
     */
    @Override
    public Ehcache getCache ( String name ) {
        Ehcache ehcache = this.cacheManager.getCache(name);

        if ( ehcache == null ) {
            throw new NullPointerException(String.format("Cache %s is NULL, not configured properly?", name)); //$NON-NLS-1$
        }

        return new ClassLoaderAwareCache(ehcache, this.bundleClassLoader);
    }


    /**
     * @param name
     * @param cl
     * @return the cache using the given classloader
     */
    @Override
    public Ehcache getCache ( String name, ClassLoader cl ) {
        return new ClassLoaderAwareCache(this.cacheManager.getCache(name), cl);
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching;


import net.sf.ehcache.Ehcache;


/**
 * @author mbechler
 * 
 */
public interface CacheService {

    /**
     * 
     * @param name
     * @param cl
     * @return the cache using the given classloader
     */
    Ehcache getCache ( String name, ClassLoader cl );


    /**
     * @param name
     * @param cl
     * @return the cache using the calling bundles classloader
     */
    Ehcache getCache ( String name );

}

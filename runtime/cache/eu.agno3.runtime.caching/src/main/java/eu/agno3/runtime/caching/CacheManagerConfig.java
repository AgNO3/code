/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching;


/**
 * @author mbechler
 * 
 */
public final class CacheManagerConfig {

    private CacheManagerConfig () {}

    /**
     * Configuration PID
     */
    public static final String PID = "cacheManager"; //$NON-NLS-1$

    /**
     * Cache name
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * Disk path
     */
    public static final String DISK_PATH = "diskPath"; //$NON-NLS-1$

    /**
     * Maximum in memory size of cache
     */
    public static final String MAX_HEAP_SIZE = "maxHeapSize"; //$NON-NLS-1$

    /**
     * Maximum on disk size of cache
     */
    public static final String MAX_DISK_SIZE = "maxDiskSize"; //$NON-NLS-1$

}

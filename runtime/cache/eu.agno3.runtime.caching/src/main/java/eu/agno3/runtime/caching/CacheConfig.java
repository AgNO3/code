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
public final class CacheConfig {

    private CacheConfig () {}

    /**
     * Configuration PID
     */
    public static final String PID = "cache"; //$NON-NLS-1$

    /**
     * Cache name
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * Persistence (none, localTempSwap)
     */
    public static final String PERSISTENCE = "persistence"; //$NON-NLS-1$

    /**
     * Maximum in memory size of cache
     */
    public static final String MAX_HEAP_SIZE = "maxHeapSize"; //$NON-NLS-1$

    /**
     * Maximum number of in memory entries
     */
    public static final String MAX_HEAP_ENTRIES = "maxHeapEntries"; //$NON-NLS-1$

    /**
     * Maximum on disk size of cache
     */
    public static final String MAX_DISK_SIZE = "maxDiskSize"; //$NON-NLS-1$

    /**
     * Maximum number of on disk entries
     */
    public static final String MAX_DISK_ENTRIES = "maxDiskEntries"; //$NON-NLS-1$

    /**
     * Maximum number of seconds an entry will be held without beeing accessed
     */
    public static final String TIME_TO_IDLE = "timeToIdle"; //$NON-NLS-1$

    /**
     * Maximum number of seconds an entry will be held
     */
    public static final String TIME_TO_LIVE = "timeToLive"; //$NON-NLS-1$
}

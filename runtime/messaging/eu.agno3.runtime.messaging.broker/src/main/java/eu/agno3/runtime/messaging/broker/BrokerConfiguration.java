/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker;


/**
 * @author mbechler
 * 
 */
public final class BrokerConfiguration {

    private BrokerConfiguration () {}

    /**
     * Configuration PID
     */
    public static final String PID = "messaging.broker"; //$NON-NLS-1$

    /**
     * Broker name
     */
    public static final String BROKER_NAME_ATTR = "name"; //$NON-NLS-1$

    /**
     * Memory limit (MB)
     */
    public static final String MEMORY_LIMIT_ATTR = "memLimit"; //$NON-NLS-1$

    /**
     * Data directory
     */
    public static final String DATA_DIRECTORY_ATTR = "dataDir"; //$NON-NLS-1$

    /**
     * Size limit for data store (MB)
     */
    public static final String DATA_SIZE_LIMIT_ATTR = "maxDataSize"; //$NON-NLS-1$

    /**
     * Enable messaging statistics
     */
    public static final String ENABLE_STATS_ATTR = "enableStats"; //$NON-NLS-1$

    /**
     * Enable message persistence
     */
    public static final String PERSISTENT_ATTR = "persistent"; //$NON-NLS-1$

    /**
     * Temp directory
     */
    public static final String TMP_DIRECTORY_ATTR = "tmpDir"; //$NON-NLS-1$

    /**
     * Size limit for tmp store (MB)
     */
    public static final String TMP_SIZE_LIMIT_ATTR = "maxTmpSize"; //$NON-NLS-1$

    /**
     * Clear all persistent messages on startup (development)
     */
    public static final String CLEAR_ON_START_ATTR = "clearOnStart"; //$NON-NLS-1$

}

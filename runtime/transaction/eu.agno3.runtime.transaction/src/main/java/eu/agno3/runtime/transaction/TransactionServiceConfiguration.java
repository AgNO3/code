/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.transaction;


/**
 * @author mbechler
 * 
 */
public final class TransactionServiceConfiguration {

    /**
     * Configuration PID
     */
    public static final String PID = "txservice"; //$NON-NLS-1$


    private TransactionServiceConfiguration () {}

    /**
     * Transaction manager implementation to use
     */
    public static final String TM_SERVICE_ATTR = "tmService"; //$NON-NLS-1$

    /**
     * Maximum allowable transaction timeout
     */
    public static final String MAX_TIMEOUT_ATTR = "maxTransactionTimeout"; //$NON-NLS-1$

    /**
     * Default transaction timeout
     */
    public static final String DEFAULT_TIMEOUT_ATTR = "defaultTransactionTimeout"; //$NON-NLS-1$

    /**
     * Maximum allowable concurrently active transactions
     */
    public static final String MAX_ACTIVE_ATTR = "maxActive"; //$NON-NLS-1$

    /**
     * Disable transaction logging (recovery method, not debugging, never disable in production)
     */
    public static final String DISABLE_LOGGING_ATTR = "disableLogging"; //$NON-NLS-1$

    /**
     * Join subtransactions if possible (default: true)
     */
    public static final String JOIN_SUBTRANSACTION_ATTR = "joinSubtransactions"; //$NON-NLS-1$

    /**
     * Forced shutdown on deactivation
     */
    public static final String FORCE_SHUTDOWN_ATTR = "forcedShutdown"; //$NON-NLS-1$

    /**
     * Basename for transaction log files
     */
    public static final String LOG_BASENAME_ATTR = "logBaseName"; //$NON-NLS-1$

    /**
     * Directory to store transaction log files
     */
    public static final String LOG_BASEDIR_ATTR = "logBaseDir"; //$NON-NLS-1$

    /**
     * Checkpoint transaction log every x transactions
     */
    public static final String LOG_CHECKPOINT_INTERVAL_ATTR = "logCheckpointInterval"; //$NON-NLS-1$

    /**
     * Wait for 2PC Acks in parallel (default: true)
     */
    public static final String THREADED_2PC_ATTR = "threaded2PC"; //$NON-NLS-1$

    /**
     * 
     */
    public static final Object TM_UNIQUE_NAME = "uniqueName"; //$NON-NLS-1$

}

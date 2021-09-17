/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.transaction.internal;


/**
 * @author mbechler
 * 
 */
final class AtomikosConfigProperties {

    static final String LOG_BASE_DIR = "com.atomikos.icatch.log_base_dir"; //$NON-NLS-1$
    static final String CHECKPOINT_INTERVAL = "com.atomikos.icatch.checkpoint_interval"; //$NON-NLS-1$
    static final String LOG_BASE_NAME = "com.atomikos.icatch.log_base_name"; //$NON-NLS-1$
    static final String ENABLE_LOGGING = "com.atomikos.icatch.enable_logging"; //$NON-NLS-1$
    static final String MAX_ACTIVES = "com.atomikos.icatch.max_actives"; //$NON-NLS-1$
    static final String DEFAULT_JTA_TIMEOUT = "com.atomikos.icatch.default_jta_timeout"; //$NON-NLS-1$
    static final String MAX_TIMEOUT = "com.atomikos.icatch.max_timeout"; //$NON-NLS-1$
    static final String THREADED_2PC = "com.atomikos.icatch.threaded_2pc"; //$NON-NLS-1$
    static final String FORCE_SHUTDOWN_ON_VM_EXIT = "com.atomikos.icatch.force_shutdown_on_vm_exit"; //$NON-NLS-1$
    static final String SERIAL_JTA_TRANSACTIONS = "com.atomikos.icatch.serial_jta_transactions"; //$NON-NLS-1$
    static final String TM_UNIQUE_NAME = "com.atomikos.icatch.tm_unique_name"; //$NON-NLS-1$
    static final String TX_SERVICE = "com.atomikos.icatch.service"; //$NON-NLS-1$
    static final String OUTPUT_DIR = "com.atomikos.icatch.output_dir"; //$NON-NLS-1$
    static final String REGISTERED = "com.atomikos.icatch.registered"; //$NON-NLS-1$


    private AtomikosConfigProperties () {}

}

/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.transaction.internal;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Dictionary;
import java.util.Properties;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.atomikos.datasource.RecoverableResource;
import com.atomikos.datasource.TransactionalResource;
import com.atomikos.icatch.TransactionServicePlugin;
import com.atomikos.icatch.admin.LogAdministrator;
import com.atomikos.icatch.config.Configuration;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.JtaTransactionServicePlugin;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.icatch.provider.ConfigProperties;

import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.transaction.TransactionService;
import eu.agno3.runtime.transaction.TransactionServiceConfiguration;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    TransactionService.class
}, immediate = true, property = {
    TransactionServiceConfiguration.TM_SERVICE_ATTR + "=" + AtomikosTransactionService.DEFAULT_TM_SERVICE
}, configurationPid = TransactionServiceConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AtomikosTransactionService implements TransactionService {

    protected static final String DEFAULT_TM_SERVICE = "com.atomikos.icatch.standalone.UserTransactionServiceFactory"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(AtomikosTransactionService.class);

    private static final long MAX_SHUTDOWN_WAIT = 30000;

    private UserTransactionServiceImp uts = new UserTransactionServiceImp();

    private boolean forceShutdown = true;

    private int defaultTransactionTimeout;


    @Activate
    protected void activate ( ComponentContext context ) {

        log.info("Starting transaction manager"); //$NON-NLS-1$

        Properties tsConfig = new Properties();
        tsConfig.setProperty(
            AtomikosConfigProperties.TM_UNIQUE_NAME,
            String.format("UserTransactionServer-%d", context.getBundleContext().getBundle().getBundleId())); //$NON-NLS-1$

        System.setProperty(AtomikosConfigProperties.REGISTERED, Boolean.TRUE.toString());
        configureTransactionService(context.getProperties(), tsConfig);
        Configuration.getConfigProperties().applyUserSpecificProperties(tsConfig);
        ConfigProperties cfg = new ConfigProperties(tsConfig);
        checkLock(cfg);

        try {
            this.defaultTransactionTimeout = (int) ( cfg.getAsLong(AtomikosConfigProperties.DEFAULT_JTA_TIMEOUT) / 1000 );
            this.uts.init(tsConfig);
        }
        catch ( Exception e ) {
            log.error("Failed to start transaction service:", e); //$NON-NLS-1$
        }

    }


    @Modified
    protected void modified ( ComponentContext context ) {
        log.warn("Ignoring reconfiguration"); //$NON-NLS-1$
    }


    /**
     * @param configProperties
     */
    protected void checkLock ( ConfigProperties configProperties ) {
        try {
            File parent = new File(configProperties.getLogBaseDir());
            if ( !parent.exists() ) {
                parent.mkdir();
            }
            File lockFile = new File(configProperties.getLogBaseDir(), configProperties.getLogBaseName() + ".lck"); //$NON-NLS-1$
            try ( FileOutputStream fos = new FileOutputStream(lockFile);
                  FileLock l = fos.getChannel().tryLock() ) {
                if ( l == null ) {
                    log.error("Transaction log already in use, running multiple instances?"); //$NON-NLS-1$
                    System.exit(-2);
                }
            }
        }
        catch ( IOException e ) {
            log.error("Error checking lockfile", e); //$NON-NLS-1$
            System.exit(-1);
        }
    }


    @Override
    public TransactionContext ensureTransacted () {
        TransactionManager transactionManager = this.getTransactionManager();
        try {
            if ( transactionManager.getStatus() != Status.STATUS_ACTIVE ) {
                transactionManager.begin();
                return new TransactionContextImpl(transactionManager);
            }
        }
        catch (
            SystemException |
            NotSupportedException e ) {
            log.warn("Failed to start transaction", e); //$NON-NLS-1$
        }

        return new NOPTransactionContext();
    }


    protected UserTransactionManager getUserTransactionManager () {
        UserTransactionManager utm = new UserTransactionManager();
        utm.setStartupTransactionService(false);
        utm.setForceShutdown(this.forceShutdown);
        try {
            if ( this.defaultTransactionTimeout > 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Setting transaction timeout to %d s", this.defaultTransactionTimeout)); //$NON-NLS-1$
                }
                utm.setTransactionTimeout(this.defaultTransactionTimeout);
            }
            utm.init();
        }
        catch ( SystemException e ) {
            log.error("Failed to initialize transaction manager:", e); //$NON-NLS-1$
        }

        return utm;
    }


    @Override
    public TransactionManager getTransactionManager () {
        return this.getUserTransactionManager();
    }


    @Override
    public UserTransaction createUserTransaction () {
        return new UserTransactionImp();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.transaction.TransactionService#registerResource(com.atomikos.datasource.TransactionalResource)
     */
    @Override
    public void registerResource ( TransactionalResource res ) {
        this.uts.registerResource(res);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.transaction.TransactionService#unregisterResource(com.atomikos.datasource.TransactionalResource)
     */
    @Override
    public void unregisterResource ( TransactionalResource res ) {
        this.uts.removeResource(res);
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        log.info("Stopping transaction manager"); //$NON-NLS-1$
        this.uts.shutdown(MAX_SHUTDOWN_WAIT);
        this.uts = null;
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindLogAdministrator ( LogAdministrator admin ) {
        this.uts.registerLogAdministrator(admin);
    }


    protected synchronized void unbindLogAdministrator ( LogAdministrator admin ) {
        this.uts.removeLogAdministrator(admin);
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindTSListener ( TransactionServicePlugin tsl ) {
        this.uts.registerTransactionServicePlugin(tsl);
    }


    protected synchronized void unbindTSListener ( TransactionServicePlugin tsl ) {
        this.uts.registerTransactionServicePlugin(tsl);
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindRecoverableResource ( RecoverableResource res ) {
        this.uts.registerResource(res);
    }


    protected synchronized void unbindRecoverableResource ( RecoverableResource res ) {
        this.uts.removeResource(res);
    }


    protected static void configureTransactionService ( Dictionary<String, Object> componentConfig, Properties tsConfig ) {

        String tmUniqueNameSpec = (String) componentConfig.get(TransactionServiceConfiguration.TM_UNIQUE_NAME);
        if ( tmUniqueNameSpec != null ) {
            // need truncate the name, otherwise we end up with transaction IDs that are rejected
            tsConfig.setProperty(AtomikosConfigProperties.TM_UNIQUE_NAME, tmUniqueNameSpec.substring(0, Math.min(40, tmUniqueNameSpec.length())));
        }

        String tmServiceSpec = (String) componentConfig.get(TransactionServiceConfiguration.TM_SERVICE_ATTR);
        if ( tmServiceSpec != null ) {
            tsConfig.setProperty(AtomikosConfigProperties.TX_SERVICE, tmServiceSpec);
        }

        configureTransactionServiceLimits(componentConfig, tsConfig);

        String joinSubtransactionsSpec = (String) componentConfig.get(TransactionServiceConfiguration.JOIN_SUBTRANSACTION_ATTR);
        if ( joinSubtransactionsSpec != null && joinSubtransactionsSpec.equals(Boolean.FALSE.toString()) ) {
            tsConfig.setProperty(AtomikosConfigProperties.SERIAL_JTA_TRANSACTIONS, Boolean.FALSE.toString());
        }

        String forcedShutdownSpec = (String) componentConfig.get(TransactionServiceConfiguration.FORCE_SHUTDOWN_ATTR);
        if ( forcedShutdownSpec != null && forcedShutdownSpec.equals(Boolean.TRUE.toString()) ) {
            tsConfig.setProperty(AtomikosConfigProperties.FORCE_SHUTDOWN_ON_VM_EXIT, Boolean.TRUE.toString());
        }

        configureTransactionServiceLogging(componentConfig, tsConfig);

        String threaded2PCSpec = (String) componentConfig.get(TransactionServiceConfiguration.THREADED_2PC_ATTR);
        if ( threaded2PCSpec != null && threaded2PCSpec.equals(Boolean.TRUE.toString()) ) {
            tsConfig.setProperty(AtomikosConfigProperties.THREADED_2PC, Boolean.TRUE.toString());
        }
        else {
            tsConfig.setProperty(AtomikosConfigProperties.THREADED_2PC, Boolean.FALSE.toString());
        }

        tsConfig.setProperty(JtaTransactionServicePlugin.AUTOMATIC_RESOURCE_REGISTRATION_PROPERTY_NAME, Boolean.FALSE.toString());
        tsConfig.setProperty(AtomikosConfigProperties.REGISTERED, Boolean.TRUE.toString());

    }


    /**
     * @param componentConfig
     * @param tsConfig
     */
    private static void configureTransactionServiceLimits ( Dictionary<String, Object> componentConfig, Properties tsConfig ) {
        String maxTimeoutSpec = (String) componentConfig.get(TransactionServiceConfiguration.MAX_TIMEOUT_ATTR);
        if ( maxTimeoutSpec != null ) {
            tsConfig.setProperty(AtomikosConfigProperties.MAX_TIMEOUT, maxTimeoutSpec);
        }

        String defaultTimeoutSpec = (String) componentConfig.get(TransactionServiceConfiguration.DEFAULT_TIMEOUT_ATTR);
        if ( defaultTimeoutSpec != null ) {
            tsConfig.setProperty(AtomikosConfigProperties.DEFAULT_JTA_TIMEOUT, defaultTimeoutSpec);
        }
        else {
            tsConfig.setProperty(AtomikosConfigProperties.DEFAULT_JTA_TIMEOUT, String.valueOf(30000));
        }

        String maxActiveSpec = (String) componentConfig.get(TransactionServiceConfiguration.MAX_ACTIVE_ATTR);
        if ( maxActiveSpec != null ) {
            tsConfig.setProperty(AtomikosConfigProperties.MAX_ACTIVES, maxActiveSpec);
        }
    }


    /**
     * @param componentConfig
     * @param tsConfig
     */
    private static void configureTransactionServiceLogging ( Dictionary<String, Object> componentConfig, Properties tsConfig ) {
        String disableLoggingSpec = (String) componentConfig.get(TransactionServiceConfiguration.DISABLE_LOGGING_ATTR);
        if ( disableLoggingSpec != null && disableLoggingSpec.equals(Boolean.TRUE.toString()) ) {
            tsConfig.setProperty(ConfigProperties.ENABLE_LOGGING_PROPERTY_NAME, Boolean.FALSE.toString());
        }

        String logBaseNameSpec = (String) componentConfig.get(TransactionServiceConfiguration.LOG_BASENAME_ATTR);
        if ( logBaseNameSpec != null ) {
            tsConfig.setProperty(ConfigProperties.LOG_BASE_NAME_PROPERTY_NAME, logBaseNameSpec);
        }
        else {
            tsConfig.setProperty(ConfigProperties.LOG_BASE_NAME_PROPERTY_NAME, "tmlog"); //$NON-NLS-1$
        }

        String logBaseDirSpec = (String) componentConfig.get(TransactionServiceConfiguration.LOG_BASEDIR_ATTR);
        if ( logBaseDirSpec != null ) {
            tsConfig.setProperty(ConfigProperties.LOG_BASE_DIR_PROPERTY_NAME, logBaseDirSpec);
            tsConfig.setProperty(AtomikosConfigProperties.OUTPUT_DIR, logBaseDirSpec);
        }
        else {
            tsConfig.setProperty(ConfigProperties.LOG_BASE_DIR_PROPERTY_NAME, "/var/tmp/tm"); //$NON-NLS-1$
            tsConfig.setProperty(AtomikosConfigProperties.OUTPUT_DIR, "/var/tmp/tm"); //$NON-NLS-1$
        }

        String checkpointIntervalSpec = (String) componentConfig.get(TransactionServiceConfiguration.LOG_CHECKPOINT_INTERVAL_ATTR);
        if ( checkpointIntervalSpec != null ) {
            tsConfig.setProperty(AtomikosConfigProperties.CHECKPOINT_INTERVAL, checkpointIntervalSpec);
        }
    }
}

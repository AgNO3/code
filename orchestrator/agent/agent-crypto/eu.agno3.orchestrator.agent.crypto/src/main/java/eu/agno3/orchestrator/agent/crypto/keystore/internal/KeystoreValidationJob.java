/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@DisallowConcurrentExecution
@Component (
    service = TriggeredJob.class,
    property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.agent.crypto.keystore.internal.KeystoreValidationJob",
    configurationPid = "keystore.validation",
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KeystoreValidationJob implements TriggeredJob {

    private static final Logger log = Logger.getLogger(KeystoreValidationJob.class);
    private Duration interval = Duration.standardMinutes(30);

    private KeystoresManager ksManager;
    private TruststoresManager tsManager;
    private boolean checkRevocation;
    private Duration expiryWarnPeriod = Duration.standardDays(30);
    private TrustChecker trustChecker;


    @Reference
    protected synchronized void setKeystoresManager ( KeystoresManager ksm ) {
        this.ksManager = ksm;
    }


    protected synchronized void unsetKeystoresManager ( KeystoresManager ksm ) {
        if ( this.ksManager == ksm ) {
            this.ksManager = null;
        }
    }


    @Reference
    protected synchronized void setTrustChecker ( TrustChecker tc ) {
        this.trustChecker = tc;
    }


    protected synchronized void unsetTrustChecker ( TrustChecker tc ) {
        if ( this.trustChecker == tc ) {
            this.trustChecker = null;
        }
    }


    @Reference
    protected synchronized void setTruststoresManager ( TruststoresManager tsm ) {
        this.tsManager = tsm;
    }


    protected synchronized void unsetTruststoresManager ( TruststoresManager tsm ) {
        if ( this.tsManager == tsm ) {
            this.tsManager = null;
        }
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.checkRevocation = ConfigUtil.parseBoolean(cfg, "checkRevocation", true); //$NON-NLS-1$
        this.expiryWarnPeriod = ConfigUtil.parseDuration(cfg, "expiryWarnPeriod", Duration.standardDays(30)); //$NON-NLS-1$
        this.interval = ConfigUtil.parseDuration(cfg, "checkInterval", Duration.standardMinutes(30)); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever((int) this.interval.getStandardMinutes())).build();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        log.debug("Running key checks"); //$NON-NLS-1$
        for ( String keyStore : this.ksManager.getKeyStores() ) {
            try ( KeystoreManager km = this.ksManager.getKeyStoreManager(keyStore) ) {
                String validationTs = this.ksManager.getValidationTruststoreName(keyStore);
                TrustConfiguration cfg = null;
                if ( validationTs != null && this.tsManager.hasTrustStore(validationTs) ) {
                    try {
                        cfg = this.tsManager.getTrustConfig(validationTs, this.checkRevocation);
                    }
                    catch ( TruststoreManagerException e ) {
                        log.warn("Failed to get validation truststore " + validationTs, e); //$NON-NLS-1$
                    }
                }

                for ( KeyStoreEntry ksEntry : km.listKeys() ) {
                    checkKeyEntry(keyStore, cfg, ksEntry);
                }
            }
            catch ( KeystoreManagerException e ) {
                log.error("Failed to obtain keystore manager for " + keyStore, e); //$NON-NLS-1$
            }

        }

    }


    /**
     * @param keyStore
     * @param cfg
     * @param ksEntry
     */
    private void checkKeyEntry ( String keyStore, TrustConfiguration cfg, KeyStoreEntry ksEntry ) {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Checking chain of %s:%s", keyStore, ksEntry.getAlias())); //$NON-NLS-1$
        }
        X509Certificate[] certificateChain = (X509Certificate[]) ksEntry.getCertificateChain();
        validateChainSimple(keyStore, ksEntry, certificateChain);
        if ( cfg != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Checking against truststore " + cfg.getId()); //$NON-NLS-1$
                }
                this.trustChecker.validateChain(cfg, Arrays.asList(certificateChain), null, null, null);
            }
            catch ( CryptoException e ) {
                log.warn(String.format("Certificate validation failed for %s in %s (against %s)", ksEntry.getAlias(), keyStore, cfg.getId()), e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param keyStore
     * @param ksEntry
     * @param certificateChain
     */
    private void validateChainSimple ( String keyStore, KeyStoreEntry ksEntry, X509Certificate[] certificateChain ) {
        if ( certificateChain == null || certificateChain.length == 0 ) {
            return;
        }

        for ( X509Certificate cert : certificateChain ) {
            validateCertificate(keyStore, ksEntry.getAlias(), cert);
        }

    }


    /**
     * @param string
     * @param keyStore
     * @param cert
     */
    private void validateCertificate ( String keyStore, String keyAlias, X509Certificate cert ) {
        DateTime notBefore = new DateTime(cert.getNotBefore());
        DateTime notAfter = new DateTime(cert.getNotAfter());
        if ( notBefore.isAfterNow() ) {
            log.error(String.format("Certificate %s is not yet valid %s", makeCertDisplay(keyStore, keyAlias, cert), notBefore)); //$NON-NLS-1$
        }

        if ( notAfter.isBeforeNow() ) {
            log.error(String.format("Certificate %s is expired ", makeCertDisplay(keyStore, keyAlias, cert))); //$NON-NLS-1$
        }
        else if ( notAfter.minus(this.expiryWarnPeriod).isBeforeNow() ) {
            log.error(String.format("Certificate %s is going to expire soon", makeCertDisplay(keyStore, keyAlias, cert))); //$NON-NLS-1$
        }
    }


    /**
     * @param keyAlias
     * @param keyStore
     * @param cert
     * @return a string representation identifying the cert
     */
    private static String makeCertDisplay ( String keyStore, String keyAlias, X509Certificate cert ) {
        return String.format("%s/%s - %s [%s]", keyStore, keyAlias, cert.getSubjectX500Principal().getName(), cert.getSerialNumber().toString(16)); //$NON-NLS-1$
    }
}

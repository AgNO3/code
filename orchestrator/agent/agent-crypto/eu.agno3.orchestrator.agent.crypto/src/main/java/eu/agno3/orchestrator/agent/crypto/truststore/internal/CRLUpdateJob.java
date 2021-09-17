/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.util.Dictionary;

import org.apache.log4j.Logger;
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

import eu.agno3.orchestrator.agent.crypto.truststore.CRLUpdater;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 *
 */
@DisallowConcurrentExecution
@Component (
    service = TriggeredJob.class,
    property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.agent.crypto.truststore.internal.CRLUpdateJob",
    configurationPid = "truststore.crlupdate",
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class CRLUpdateJob implements TriggeredJob {

    private static final Logger log = Logger.getLogger(CRLUpdateJob.class);
    private Duration interval = Duration.standardMinutes(30);

    private TruststoresManager tsManager;
    private CRLUpdater crlUpdater;


    @Reference
    protected synchronized void setTruststoresManager ( TruststoresManager tsm ) {
        this.tsManager = tsm;
    }


    protected synchronized void unsetTruststoresManager ( TruststoresManager tsm ) {
        if ( this.tsManager == tsm ) {
            this.tsManager = null;
        }
    }


    @Reference
    protected synchronized void setCRLUpdater ( CRLUpdater cu ) {
        this.crlUpdater = cu;
    }


    protected synchronized void unsetCRLUpdater ( CRLUpdater cu ) {
        if ( this.crlUpdater == cu ) {
            this.crlUpdater = null;
        }
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {

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
        log.debug("Running CRL updates"); //$NON-NLS-1$

        for ( String truststore : this.tsManager.getTrustStores() ) {

            try {
                TruststoreManager trustStoreManager = this.tsManager.getTrustStoreManager(truststore);
                this.crlUpdater.updateCRLsFromDistributionPoints(trustStoreManager);
            }
            catch ( TruststoreManagerException e ) {
                log.error("Failed to update CRLs for " + truststore, e); //$NON-NLS-1$
            }
        }
    }
}

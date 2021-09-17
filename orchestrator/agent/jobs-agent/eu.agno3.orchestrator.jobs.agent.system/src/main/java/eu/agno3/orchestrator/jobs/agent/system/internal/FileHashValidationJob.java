/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 5, 2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.system.file.hashtracking.FileHashValidator;
import eu.agno3.orchestrator.system.file.hashtracking.ValidationResult;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 *
 */
@DisallowConcurrentExecution
@Component (
    service = TriggeredJob.class,
    property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.agent.crypto.truststore.internal.CRLUpdateJob" )
public class FileHashValidationJob implements TriggeredJob {

    private static final Logger log = Logger.getLogger(FileHashValidationJob.class);
    private FileHashValidator hashValidator;


    @Reference
    protected synchronized void setHashValidator ( FileHashValidator fhv ) {
        this.hashValidator = fhv;
    }


    protected synchronized void unsetHashValidator ( FileHashValidator fhv ) {
        if ( this.hashValidator == fhv ) {
            this.hashValidator = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> trigger ) {
        return trigger.withSchedule(SimpleScheduleBuilder.repeatHourlyForever(1)).startNow().build();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        log.debug("Running file hash validation"); //$NON-NLS-1$
        try {
            Map<Path, ValidationResult> validationErrors = this.hashValidator.getMismatchingEntries();
            if ( !validationErrors.isEmpty() ) {
                log.warn("The following files have been modified locally:"); //$NON-NLS-1$
                for ( Entry<Path, ValidationResult> entry : validationErrors.entrySet() ) {
                    log.warn(String.format("%s: %s", entry.getKey(), entry.getValue().name())); //$NON-NLS-1$
                }
            }
            else {
                log.debug("No hash mismatches found"); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to get file hashes to check", e); //$NON-NLS-1$
        }
    }

}

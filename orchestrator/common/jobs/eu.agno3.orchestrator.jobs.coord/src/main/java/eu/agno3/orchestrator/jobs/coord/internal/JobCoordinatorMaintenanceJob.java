/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@DisallowConcurrentExecution
@Component (
    service = TriggeredJob.class,
    property = JobProperties.JOB_TYPE + "=eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorMaintenanceJob" )
public class JobCoordinatorMaintenanceJob implements TriggeredJob {

    private static final Logger log = Logger.getLogger(JobCoordinatorMaintenanceJob.class);
    private JobCoordinator coordinator;
    private TransactionService transactionService;


    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        synchronized ( coord ) {
            this.coordinator = coord;
        }

    }


    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            synchronized ( coord ) {
                this.coordinator = null;
            }
        }
    }


    @Reference
    protected synchronized void setTransactionManager ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionManager ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext ctx ) throws JobExecutionException {
        log.trace("Running coordinator maintenance"); //$NON-NLS-1$
        JobCoordinator coord = this.coordinator;
        if ( coord == null ) {
            return;
        }

        if ( !coord.needsMaintenance() ) {
            log.trace("Skipping"); //$NON-NLS-1$
            return;
        }

        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            synchronized ( coord ) {
                coord.run();
            }
            log.trace("Committing"); //$NON-NLS-1$
            tx.commit();
        }
        catch ( Exception e ) {
            log.warn("Job maintenance job failed", e); //$NON-NLS-1$
            throw new JobExecutionException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.scheduler.TriggeredJob#buildTrigger(org.quartz.TriggerBuilder)
     */
    @Override
    public Trigger buildTrigger ( TriggerBuilder<Trigger> tb ) {
        return tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1)).build();
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.compound;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.compound.CompoundJob;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.jobs.server.JobOutputTracker;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.jobs.compound.CompoundJob" )
@JobType ( CompoundJob.class )
public class CompoundRunnableFactory implements JobRunnableFactory<CompoundJob> {

    private JobCoordinator jobCoordinator;
    private JobOutputTracker jobOutputTracker;
    private JobProgressTracker jobProgressTracker;


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator coord ) {
        this.jobCoordinator = coord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator coord ) {
        if ( this.jobCoordinator == coord ) {
            this.jobCoordinator = null;
        }
    }


    @Reference
    protected synchronized void setJobOutputTracker ( JobOutputTracker jot ) {
        this.jobOutputTracker = jot;
    }


    protected synchronized void unsetJobOutputTracker ( JobOutputTracker jot ) {
        if ( this.jobOutputTracker == jot ) {
            this.jobOutputTracker = null;
        }
    }


    @Reference
    protected synchronized void setJobProgressTracker ( JobProgressTracker jpt ) {
        this.jobProgressTracker = jpt;
    }


    protected synchronized void unsetJobProgressTracker ( JobProgressTracker jpt ) {
        if ( this.jobProgressTracker == jpt ) {
            this.jobProgressTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( CompoundJob j ) throws JobRunnableException {
        return new CompoundJobRunnable(j, this);
    }


    synchronized JobCoordinator getCoordinator () throws InterruptedException {
        if ( this.jobCoordinator == null ) {
            throw new InterruptedException();
        }
        return this.jobCoordinator;
    }


    synchronized JobOutputTracker getOutputTracker () throws InterruptedException {
        if ( this.jobCoordinator == null ) {
            throw new InterruptedException();
        }
        return this.jobOutputTracker;
    }


    synchronized JobProgressTracker getProgressTracker () throws InterruptedException {
        if ( this.jobCoordinator == null ) {
            throw new InterruptedException();
        }
        return this.jobProgressTracker;
    }

}

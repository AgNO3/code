/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.exec;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * 
 * @author mbechler
 * 
 */
class JobWrapper implements Runnable {

    private static final Logger log = Logger.getLogger(JobWrapper.class);

    private JobRunnable delegate;
    private JobStateTracker jst;
    private Job job;

    @NonNull
    private JobOutputHandler outputHandler;


    /**
     * 
     * @param jst
     * @param j
     * @param r
     * @param outputHandler
     */
    public JobWrapper ( JobStateTracker jst, Job j, JobRunnable r, @NonNull JobOutputHandler outputHandler ) {
        this.jst = jst;
        this.job = j;
        this.delegate = r;
        this.outputHandler = outputHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {

        JobInfo found = null;
        try {
            found = this.jst.getJobState(this.job);

            if ( found != null
                    && ( found.getState() == JobState.CANCELLED || found.getState() == JobState.FAILED || found.getState() == JobState.FINISHED ) ) {
                this.jst.updateJobState(this.job, found.getState());
                return;
            }
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to get current state", e); //$NON-NLS-1$
            unknownFailure();
            return;
        }

        prepare();
        try {
            doExecute();
        }
        catch ( Exception e ) {
            log.warn("Job execution failed:", e); //$NON-NLS-1$
            this.outputHandler.logLineError(String.format("Exception %s in job: %s", e.getClass().getName(), e.getMessage()), e); //$NON-NLS-1$
            unknownFailure();
        }
        finish();
    }


    private void unknownFailure () {
        try {
            this.jst.updateJobState(this.job, JobState.FAILED);
        }
        catch ( JobQueueException e1 ) {
            log.warn("Failed to set job state to failed:", e1); //$NON-NLS-1$
        }
    }


    private void doExecute () throws Exception {
        try {
            JobState st = this.delegate.run(this.outputHandler);
            if ( log.isDebugEnabled() ) {
                log.debug("Job finished: " + this.job); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Setting job to final state " + st); //$NON-NLS-1$
            }
            this.jst.updateJobState(this.job, st);
        }
        catch ( InterruptedException e ) {
            // clear interrupted flag
            Thread.interrupted();
            log.warn("Job was interrupted"); //$NON-NLS-1$
            log.debug("Job was interrupted:", e); //$NON-NLS-1$
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to set job state to finished:", e); //$NON-NLS-1$
        }

    }


    private void finish () {
        this.outputHandler.eof();
        this.outputHandler.end();
    }


    private void prepare () {
        if ( log.isDebugEnabled() ) {
            log.debug("Job starting: " + this.job); //$NON-NLS-1$
        }
        try {
            this.jst.updateJobState(this.job, JobState.RUNNING);
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to set job state to running:", e); //$NON-NLS-1$
        }
        this.outputHandler.start();
    }

}
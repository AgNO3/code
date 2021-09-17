/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.compound;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.compound.CompoundJob;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;
import eu.agno3.orchestrator.jobs.server.JobOutputBuffer;


/**
 * @author mbechler
 *
 */
public class CompoundJobRunnable implements JobRunnable {

    private static final Logger log = Logger.getLogger(CompoundJobRunnable.class);

    private static final int SLEEP_TIME = 100;
    private CompoundJob j;

    private List<Job> started = new ArrayList<>();
    private Set<UUID> finished = new HashSet<>();
    private Map<UUID, JobProgressInfo> progress = new HashMap<>();
    private Map<UUID, Map<JobOutputLevel, Long>> outputPositions = new HashMap<>();

    private float totalProgress;

    private CompoundRunnableFactory rf;


    /**
     * @param j
     * @param rf
     */
    public CompoundJobRunnable ( CompoundJob j, CompoundRunnableFactory rf ) {
        this.j = j;
        if ( this.j.getWeights() != null && this.j.getWeights().size() != this.j.getJobs().size() ) {
            throw new IllegalArgumentException("Size of weight list must match job list"); //$NON-NLS-1$
        }
        this.rf = rf;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
        boolean error = false;
        Job errJob = null;
        try {
            for ( Job job : this.j.getJobs() ) {
                if ( !doRunJob(job, outHandler) ) {
                    error = true;
                    errJob = job;
                    break;
                }
            }

            // wait a bit to allow for late output
            long timeout = System.currentTimeMillis() + 30 * 1000;
            boolean eof = false;
            while ( !eof && timeout > System.currentTimeMillis() ) {
                log.debug("Waiting for remaining output"); //$NON-NLS-1$
                eof = pumpJobsInternal(outHandler);
                if ( !eof ) {
                    Thread.sleep(1000);
                }
            }
            if ( !eof ) {
                outHandler.logLineError("Timeout waiting for delegate job output"); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.warn("Compound job failure", e); //$NON-NLS-1$
            throw e;
        }

        if ( error ) {
            throw new JobRunnableException("Failed to execute subjob " + errJob); //$NON-NLS-1$
        }

        return JobState.FINISHED;
    }


    /**
     * @param outHandler
     * @param j2
     * @return
     * @throws JobQueueException
     * @throws InterruptedException
     */
    private boolean doRunJob ( Job job, JobOutputHandler outHandler ) throws JobQueueException, InterruptedException {

        if ( log.isDebugEnabled() ) {
            log.debug("Queuing job " + job); //$NON-NLS-1$
        }

        this.rf.getCoordinator().queueJob(job);

        this.outputPositions.put(job.getJobId(), makeOutputMap());
        this.started.add(job);

        while ( checkJob(job, outHandler) ) {
            pumpJobs(outHandler);
        }

        JobState state = this.rf.getCoordinator().getJobInfo(job).getState();
        if ( EnumSet.of(JobState.CANCELLED, JobState.FAILED, JobState.STALLED, JobState.TIMEOUT, JobState.UNKNOWN).contains(state) ) {
            log.warn("Subjob state is " + state); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @return
     */
    private static Map<JobOutputLevel, Long> makeOutputMap () {
        Map<JobOutputLevel, Long> outputPositions = new HashMap<>();
        for ( JobOutputLevel l : JobOutputLevel.values() ) {
            outputPositions.put(l, Long.valueOf(0));
        }
        return outputPositions;
    }


    /**
     * @param jobId
     * @param outHandler
     * @param progress
     * @param outputPositions
     * @throws InterruptedException
     */
    private void pumpJobs ( JobOutputHandler outHandler ) throws InterruptedException {
        DateTime nextIter = DateTime.now().plusMillis(SLEEP_TIME);

        pumpJobsInternal(outHandler);

        long sleepTime = nextIter.getMillis() - DateTime.now().getMillis();
        if ( sleepTime > 0 ) {
            Thread.sleep(sleepTime);
        }
    }


    /**
     * @param outHandler
     * @return
     * @throws InterruptedException
     */
    private boolean pumpJobsInternal ( JobOutputHandler outHandler ) throws InterruptedException {
        boolean allEof = true;
        int idx = 0;
        for ( Job job : this.started ) {
            UUID jobId = job.getJobId();
            allEof &= pumpOutput(jobId, outHandler, this.outputPositions.get(jobId));
            this.progress.put(jobId, pumpProgress(jobId, idx, outHandler, this.progress.get(jobId)));
            idx += 1;

            if ( !allEof ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Breaking, current job has not produced all it's output " + job.getJobId()); //$NON-NLS-1$
                }
                return false;
            }
        }
        return allEof;
    }


    /**
     * @param jobId
     * @param outHandler
     * @param pos
     * @throws InterruptedException
     */
    private boolean pumpOutput ( UUID jobId, JobOutputHandler outHandler, Map<JobOutputLevel, Long> pos ) throws InterruptedException {
        JobOutputBuffer output = this.rf.getOutputTracker().getOutput(jobId);
        if ( output != null ) {

            for ( JobOutputLevel l : JobOutputLevel.values() ) {
                pumpJobOutput(outHandler, jobId, pos, output, l);
            }
            if ( log.isDebugEnabled() && output.isEof() ) {
                log.debug("Completed output for " + jobId); //$NON-NLS-1$
            }
            return output.isEof();
        }
        return false;
    }


    /**
     * @param outHandler
     * @param jobId
     * @param pos
     * @param output
     * @param l
     */
    private static void pumpJobOutput ( JobOutputHandler outHandler, UUID jobId, Map<JobOutputLevel, Long> pos, JobOutputBuffer output,
            JobOutputLevel l ) {

        String newOutput = output.getLevelOutput(l, pos.get(l));
        if ( !StringUtils.isBlank(newOutput) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Job %s @ %d", jobId, pos.get(l))); //$NON-NLS-1$
                log.trace(String.format("Job %s: %s", jobId, newOutput)); //$NON-NLS-1$
            }
            pumpOutputBuffer(l, outHandler, newOutput);
            pos.put(l, pos.get(l) + newOutput.length());
        }
    }


    /**
     * @param l
     * @param outHandler
     * @param newOutput
     */
    private static void pumpOutputBuffer ( JobOutputLevel l, JobOutputHandler outHandler, String newOutput ) {
        outHandler.logBuffer(l, newOutput);
    }


    /**
     * @param jobId
     * @param outHandler
     * @param oldProgress
     * @return
     * @throws InterruptedException
     */
    private JobProgressInfo pumpProgress ( UUID jobId, int idx, JobOutputHandler outHandler, JobProgressInfo oldProgress )
            throws InterruptedException {

        if ( this.finished.contains(jobId) ) {
            return oldProgress;
        }

        JobProgressInfo progressInfo = this.rf.getProgressTracker().getProgressInfo(jobId);
        if ( progressInfo != null && progressInfo.getLastUpdate() != null
                && ( oldProgress == null || !progressInfo.getLastUpdate().equals(oldProgress.getLastUpdate()) ) ) {

            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "Got progress event for %s: %.2f: %s (%s -> %s)", //$NON-NLS-1$
                    jobId,
                    progressInfo.getProgress(),
                    progressInfo.getStateMessage(),
                    progressInfo.getLastUpdate(),
                    oldProgress != null ? oldProgress.getLastUpdate() : null));
            }

            if ( JobState.FINISHED == progressInfo.getState() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Finished job " + jobId); //$NON-NLS-1$
                }
                this.finished.add(jobId);
            }

            float newTotalProgress = this.mapProgress(progressInfo.getProgress(), idx);
            if ( newTotalProgress > this.totalProgress ) {
                JobProgressInfoImpl info = new JobProgressInfoImpl();
                info.setLastUpdate(progressInfo.getLastUpdate());
                info.setStateMessage(progressInfo.getStateMessage());
                info.setStateMessageContext(progressInfo.getStateMessageContext());
                info.setProgress(newTotalProgress);
                if ( this.finished.size() == this.j.getJobs().size() ) {
                    info.setState(JobState.FINISHED);
                }
                else {
                    info.setState(JobState.RUNNING);
                }
                outHandler.setProgress(info);
                this.totalProgress = newTotalProgress;
                log.trace(String.format(
                    "New progress: %.2f: %s", //$NON-NLS-1$
                    info.getProgress(),
                    info.getStateMessage()));
            }
        }
        return progressInfo;
    }


    /**
     * @param idx
     * @param progress2
     * @return
     */
    private float mapProgress ( float localProg, int idx ) {
        return sumWeights(idx) + ( localProg * localWeight(idx) );
    }


    /**
     * @param idx
     * @return
     */
    private float sumWeights ( int idx ) {
        if ( this.j.getWeights() == null ) {
            return idx;
        }

        float sum = 0.0f;
        for ( int i = 0; i < idx; i++ ) {
            sum += this.j.getWeights().get(i) * 100.f;
        }

        return sum;
    }


    /**
     * @param idx
     * @return
     */
    private float localWeight ( int idx ) {
        if ( this.j.getWeights() == null ) {
            return 1.0f / this.j.getJobs().size();
        }
        return this.j.getWeights().get(idx);
    }


    /**
     * @param job
     * @param outHandler
     * @return
     * @throws JobQueueException
     * @throws InterruptedException
     */
    private boolean checkJob ( Job job, JobOutputHandler outHandler ) throws JobQueueException, InterruptedException {
        DateTime now = DateTime.now();
        if ( now.isAfter(this.j.getDeadline()) ) {
            outHandler.logLineError("Job has reached it's deadline " + job, null); //$NON-NLS-1$
            return false;
        }

        return checkJobState(this.rf.getCoordinator().getJobInfo(job), outHandler);
    }


    /**
     * @param jobInfo
     * @param outHandler
     * @return
     * @throws InterruptedException
     */
    private boolean checkJobState ( JobInfo jobInfo, JobOutputHandler outHandler ) throws InterruptedException {
        switch ( jobInfo.getState() ) {
        case CANCELLED:
        case FAILED:
        case STALLED:
        case TIMEOUT:
            return handleJobError(jobInfo, outHandler);
        case NEW:
        case QUEUED:
        case RUNNABLE:
        case SUSPENDED:
        case RESUMED:
        case RUNNING:
        case UNKNOWN:
            return true;
        case FINISHED:
            return false;
        }

        return false;
    }


    /**
     * @param jobInfo
     * @param outHandler
     * @return
     * @throws InterruptedException
     */
    private boolean handleJobError ( JobInfo jobInfo, JobOutputHandler outHandler ) throws InterruptedException {
        try {
            if ( jobInfo.getState() == JobState.STALLED || jobInfo.getState() == JobState.TIMEOUT ) {
                this.rf.getCoordinator().cancelJob(jobInfo.getJobId());
            }
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to cancel failed job", e); //$NON-NLS-1$
        }
        return false;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobSuspendHandler;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseResults;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.result.ExceptionResult;
import eu.agno3.orchestrator.system.base.execution.result.InterruptedResult;


/**
 * @author mbechler
 * 
 */
public class SystemJobRunnableAdapter implements JobRunnable {

    private static final Logger log = Logger.getLogger(SystemJobRunnableAdapter.class);

    private Job systemJob;
    private Runner runner;

    private ExecutionConfig config;

    private JobSuspendHandler suspendHandler;
    private SuspendData suspended;


    /**
     * @param runner
     * @param suspended
     * @param config
     * @param suspendHandler
     */
    public SystemJobRunnableAdapter ( Runner runner, SuspendData suspended, ExecutionConfig config, JobSuspendHandler suspendHandler ) {
        this.runner = runner;
        this.suspended = suspended;
        this.config = config;
        this.suspendHandler = suspendHandler;
    }


    /**
     * @param runner
     * @param j
     * @param config
     * @param suspendHandler
     */
    public SystemJobRunnableAdapter ( Runner runner, Job j, ExecutionConfig config, JobSuspendHandler suspendHandler ) {
        this.runner = runner;
        this.systemJob = j;
        this.config = config;
        this.suspendHandler = suspendHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * 
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws InterruptedException, ExecutionException {
        try {
            this.config.ensureEnv();
        }
        catch ( ExecutionException e ) {
            throw new ExecutionException("Environment setup failed", e); //$NON-NLS-1$
        }
        this.runner.registerEventListener(new ExecutorEventListenerOutputAdapter(outHandler));
        return doRun(outHandler);
    }


    private JobState doRun ( JobOutputHandler outHandler ) throws ExecutionException {
        Result r;
        try ( SystemJobOutputAdapter out = new SystemJobOutputAdapter(outHandler) ) {
            if ( this.systemJob != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Running job " + this.systemJob.getClass().getName()); //$NON-NLS-1$
                }
                r = this.runner.run(this.systemJob, out, this.config, this.suspendHandler);
            }
            else if ( this.suspended != null ) {
                r = this.runner.resume(this.suspended, out, this.config, this.suspendHandler);
            }
            else {
                throw new ExecutionException("Job is null"); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Job result is " + r); //$NON-NLS-1$
            }

            if ( r.suspended() ) {
                outHandler.logLineInfo("Job is suspended"); //$NON-NLS-1$
                return JobState.SUSPENDED;
            }

            if ( r instanceof InterruptedResult ) {
                outHandler.logLineInfo("Job is interrupted"); //$NON-NLS-1$
                return JobState.CANCELLED;
            }

            if ( r.failed() ) {
                ExceptionResult exResult = findExceptionResult(r);
                if ( exResult != null ) {
                    outHandler.logLineError("Job result indicates failure " + r, exResult.getException()); //$NON-NLS-1$
                    throw exResult.getException();
                }
                outHandler.logLineError("Job result indicates failure " + r); //$NON-NLS-1$
                throw new ExecutionException("Job failed"); //$NON-NLS-1$
            }

            return JobState.FINISHED;
        }
    }


    /**
     * @param r
     */
    private ExceptionResult findExceptionResult ( Result r ) {

        if ( r instanceof ExceptionResult ) {
            return (ExceptionResult) r;
        }
        else if ( r instanceof PhaseResults ) {
            return handlePhaseResults((PhaseResults) r);
        }
        else if ( r instanceof UnitResults ) {
            return handleUnitResults((UnitResults) r);
        }

        return null;

    }


    /**
     * @param r
     * @return
     */
    private ExceptionResult handleUnitResults ( UnitResults r ) {
        for ( ExecutionUnit<?, ?, ?> unit : r.getExecutedUnits() ) {
            ExceptionResult foundEx = locateExceptionResult(r, unit);
            if ( foundEx != null ) {
                return foundEx;
            }
        }
        return null;
    }


    /**
     * @param r
     * @param unit
     * @return
     * @throws ResultReferenceException
     */
    private ExceptionResult locateExceptionResult ( UnitResults r, ExecutionUnit<?, ?, ?> unit ) {
        try {
            Result res = r.getResult(unit);
            if ( res != null && res.failed() ) {
                ExceptionResult foundEx = findExceptionResult(res);
                if ( foundEx != null ) {
                    return foundEx;
                }
            }
        }
        catch ( ResultReferenceException e ) {
            log.warn("Failed to get unit result", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param r
     * @return
     */
    private ExceptionResult handlePhaseResults ( PhaseResults r ) {
        for ( Phase p : Phase.values() ) {
            try {
                Result res = r.getPhaseResult(p);
                if ( res != null && res.failed() ) {
                    ExceptionResult foundEx = findExceptionResult(res);
                    if ( foundEx != null ) {
                        return foundEx;
                    }
                }
            }
            catch ( IndexOutOfBoundsException e ) {
                log.debug("No results for phase " + p, e); //$NON-NLS-1$
                continue;
            }
        }
        return null;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Executor;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobIterator;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseExecutor;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.SuspendResult;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.events.EnterPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.LeavePhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.LeaveUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.ResumeEvent;
import eu.agno3.orchestrator.system.base.execution.events.SuspendEvent;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionInterruptedException;
import eu.agno3.orchestrator.system.base.execution.impl.context.JobExecutorContextImpl;
import eu.agno3.orchestrator.system.base.execution.impl.phase.Executors;
import eu.agno3.orchestrator.system.base.execution.result.ExceptionResult;
import eu.agno3.orchestrator.system.base.execution.result.InterruptedResult;


/**
 * @author mbechler
 * 
 */
public class DefaultExecutorImpl implements Executor {

    private static final Logger log = Logger.getLogger(DefaultExecutorImpl.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Executor#runPhase(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.execution.Phase, eu.agno3.orchestrator.system.base.execution.JobIterator,
     *      boolean)
     */
    @Override
    public boolean runPhase ( Context context, Phase phase, JobIterator toRun, boolean keepGoing ) {
        return runPhase(context, phase, toRun, null, keepGoing);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.system.base.execution.Executor#runPhase(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.execution.Phase, eu.agno3.orchestrator.system.base.execution.JobIterator,
     *      boolean)
     */
    @Override
    public boolean runPhase ( Context context, Phase phase, JobIterator toRun, SuspendData resume, boolean keepGoing ) {
        PhaseExecutor executor = Executors.getPhaseExecutors().get(phase);

        Iterator<ExecutionUnit<?, ?, ?>> iterate;
        if ( resume == null ) {
            context.publishEvent(new EnterPhaseEvent(context, phase));
            iterate = toRun.iterate(phase);
        }
        else {
            iterate = resume.getRemainIterator();
        }

        boolean wasSuspended = false;

        while ( iterate.hasNext() ) {
            ExecutionUnit<?, ?, ?> eu = iterate.next();
            if ( EnumSet.of(Phase.PREPARE, Phase.EXECUTE).contains(phase) && !eu.isSatisfied(context) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Unsatisfied EU " + eu); //$NON-NLS-1$
                }
                continue;
            }

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Running %s for %s", phase, eu)); //$NON-NLS-1$
            }

            Result r;
            context.publishEvent(new EnterUnitEvent(context, eu));
            try {

                r = executor.execute(eu, context);
            }
            catch ( ExecutionInterruptedException e ) {
                log.debug("Exceution interrupted", e); //$NON-NLS-1$
                context.getOutput().info("Job was canceled"); //$NON-NLS-1$
                r = new InterruptedResult();
            }
            catch ( ExecutionException e ) {
                context.getOutput().error(String.format("Error in phase %s", phase), e); //$NON-NLS-1$
                log.debug("Caught exception:", e); //$NON-NLS-1$
                r = new ExceptionResult(e);
            }
            catch ( Throwable t ) {
                log.warn("Uncaught exception", t); //$NON-NLS-1$
                r = new ExceptionResult(new ExecutionException("Uncaught exception", t)); //$NON-NLS-1$
            }

            context.publishEvent(new LeaveUnitEvent(context, eu, r));

            if ( r.failed() && !keepGoing ) {
                break;
            }

            if ( r.suspended() ) {
                SuspendResult s = (SuspendResult) r;
                Iterator<ExecutionUnit<?, ?, ?>> remaining;
                try {
                    remaining = context.suspend(s.getSuspendAfter(), iterate);
                }
                catch ( ExecutionException e ) {
                    log.error("Suspend failed", e); //$NON-NLS-1$
                    UnitResults ur = (UnitResults) context.getResult();
                    context.publishEvent(new LeavePhaseEvent(context, phase, new SuspendFailedUnitResults(ur)));
                    return false;
                }
                if ( !remaining.hasNext() ) {
                    context.publishEvent(new SuspendEvent(context));
                    return true;
                }

                iterate = remaining;
                wasSuspended = true;
                continue;
            }
        }

        if ( wasSuspended ) {
            context.publishEvent(new SuspendEvent(context));
            return true;
        }

        if ( ! ( context.getResult() instanceof UnitResults ) ) {
            log.error("Context does not provide unit results"); //$NON-NLS-1$
            context.publishEvent(new LeavePhaseEvent(context, phase, null));
        }
        else {
            context.publishEvent(new LeavePhaseEvent(context, phase, (UnitResults) context.getResult()));
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Executor#run(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.execution.Job)
     */
    @Override
    public boolean run ( Context context, Job job ) {
        return run(context, job, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Executor#run(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.execution.Job)
     */
    @Override
    public boolean run ( Context context, Job toRun, SuspendData resume ) {
        List<Phase> runPhases;

        if ( resume == null ) {
            runPhases = Arrays.asList(Phase.VALIDATE, Phase.PREPARE, Phase.EXECUTE);
        }
        else {
            context.publishEvent(new ResumeEvent(context));
            runPhases = Arrays.asList(Phase.RESUME, Phase.EXECUTE);
        }

        Phase failedIn = null;
        for ( Phase p : runPhases ) {
            if ( this.runPhase(context, p, toRun.getExecutionUnits(), p == Phase.EXECUTE ? resume : null, false) ) {
                this.runPhase(context, Phase.SUSPEND, toRun.getExecutionUnits(), false);
                return true;
            }

            Result result = context.getResult();
            if ( result == null || result.failed() ) {
                failedIn = p;
                break;
            }

        }

        // always run cleanup
        this.runPhase(context, Phase.CLEANUP, toRun.getExecutionUnits(), true);

        context.pushExecutorContext(new JobExecutorContextImpl(context, toRun));
        // next run failure units
        JobIterator failureRun = toRun.getExecutionUnits().failure(failedIn);
        for ( Phase p : Arrays.asList(Phase.PREPARE, Phase.EXECUTE) ) {
            this.runPhase(context, p, failureRun, false);
        }
        this.runPhase(context, Phase.CLEANUP, failureRun, true);
        context.popExecutorContext();
        return false;
    }
}

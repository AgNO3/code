/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Executor;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobExecutorContext;
import eu.agno3.orchestrator.system.base.execution.JobIterator;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseResults;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultResolver;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.events.AbstractPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.LeavePhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.StartJobEvent;
import eu.agno3.orchestrator.system.base.execution.output.Out;
import eu.agno3.orchestrator.system.base.execution.result.ExceptionResult;


/**
 * @author mbechler
 * 
 */
public class JobExecutorContextImpl implements JobExecutorContext {

    private static final Logger log = Logger.getLogger(JobExecutorContextImpl.class);

    private PhaseResults results = new ExecutionResult();
    private Context context;
    private Job job;


    /**
     * @param context
     * @param job
     */
    public JobExecutorContextImpl ( Context context, Job job ) {
        this.context = context;
        this.job = job;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#save(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void save ( JobSuspendData data ) {
        data.setJob(this.job);
        data.setPhaseResults(this.results);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#restore(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void restore ( JobSuspendData data ) {

        this.job = data.getJob();
        this.results = data.getPhaseResults();

        this.publishEvent(new StartJobEvent(this.context, data.getJob()));

        if ( data.getPhase() != null ) {
            PhaseExecutorContextImpl execContext = new PhaseExecutorContextImpl(this, data.getPhase());
            execContext.restore(data);
            this.context.pushExecutorContext(execContext);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getResultResolver()
     */
    @Override
    public ResultResolver getResultResolver () {
        throw new UnsupportedOperationException("Not in a execution phase"); //$NON-NLS-1$
    }


    /**
     * @return the results
     */
    @Override
    public PhaseResults getResult () {
        return this.results;
    }


    @Override
    public Context getContext () {
        return this.context;
    }


    /**
     * 
     * @return the executing job
     */
    public Job getJob () {
        return this.job;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getOutput()
     */
    @Override
    public Out getOutput () {
        return this.context.getJobOutput();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#publishEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void publishEvent ( ExecutorEvent ev ) {
        if ( ev instanceof EnterPhaseEvent ) {
            this.context.pushExecutorContext(new PhaseExecutorContextImpl(this, ( (AbstractPhaseEvent) ev ).getPhase()));
        }

        // ensure proper context by this ordering
        this.context.notifyListeners(ev);

        if ( ev instanceof LeavePhaseEvent ) {
            LeavePhaseEvent eev = (LeavePhaseEvent) ev;
            UnitResults r = eev.getResult();
            this.results.add(eev.getPhase(), r);
            this.context.popExecutorContext();

            if ( eev.getPhase() == Phase.EXECUTE && ( r == null || r.failed() ) ) {
                log.warn("Failure in execute phase, rolling back"); //$NON-NLS-1$

                if ( r == null ) {
                    log.warn("Result is NULL, rolling back all units"); //$NON-NLS-1$
                }
                else {
                    try {
                        for ( ExecutionUnit<?, ?, ?> u : r.getExecutedUnits() ) {
                            Result result = r.getResult(u);
                            if ( result.failed() ) {
                                Throwable t = result instanceof ExceptionResult ? ( (ExceptionResult) result ).getException() : null;
                                String msg = String.format(
                                    "Failed unit %s with %s: %s", //$NON-NLS-1$
                                    u.getClass().getSimpleName(),
                                    result.getClass().getSimpleName(),
                                    result);
                                if ( t != null ) {
                                    log.warn(msg, t);
                                }
                                else {
                                    log.warn(msg);
                                }
                            }
                        }
                    }
                    catch ( Exception e ) {
                        log.error("Failed to dump results", e); //$NON-NLS-1$
                    }
                }
                Runner runner = this.context.getRunner();
                Executor executor = runner.getExecutor();
                JobIterator executionUnits = this.getJob().getExecutionUnits();
                List<ExecutionUnit<?, ?, ?>> executedUnits = r != null ? r.getExecutedUnits() : Collections.EMPTY_LIST;
                executor.runPhase(this.context, Phase.ROLLBACK, executionUnits.reversedIterator(executedUnits), true);
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getStateMessage()
     */
    @Override
    public String getStateMessage () {
        return "{job.run}"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#setStateMessage(java.lang.String)
     */
    @Override
    public void setStateMessage ( String s ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getStateContext()
     */
    @Override
    public Map<String, String> getStateContext () {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("jobName", this.getJob().getName()); //$NON-NLS-1$
        return ctx;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#addStateContext(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void addStateContext ( String key, String val ) {
        // ignore
    }

}

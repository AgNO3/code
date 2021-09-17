/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnitFactory;
import eu.agno3.orchestrator.system.base.execution.Executor;
import eu.agno3.orchestrator.system.base.execution.ExecutorEventListener;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobSuspendHandler;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultResolver;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.events.CompleteJobEvent;
import eu.agno3.orchestrator.system.base.execution.events.StartJobEvent;
import eu.agno3.orchestrator.system.base.execution.impl.context.BaseContextImpl;
import eu.agno3.orchestrator.system.base.execution.impl.context.JobExecutorContextImpl;
import eu.agno3.orchestrator.system.base.execution.job.impl.JobBuilderImpl;
import eu.agno3.orchestrator.system.base.execution.output.Out;
import eu.agno3.orchestrator.system.base.execution.output.OutLoggerBridge;


/**
 * @author mbechler
 * 
 */
public class RunnerImpl implements Runner {

    private Executor executor = new DefaultExecutorImpl();
    private ExecutionUnitFactory executionUnitFactory = new DefaultExecutionUnitFactory();
    private Set<ExecutorEventListener> listeners = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#getExecutor()
     */
    @Override
    public Executor getExecutor () {
        return this.executor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#getExecutionUnitFactory()
     */
    @Override
    public ExecutionUnitFactory getExecutionUnitFactory () {
        return this.executionUnitFactory;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#makeJobBuilder()
     */
    @Override
    public JobBuilder makeJobBuilder () {
        return new JobBuilderImpl(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#makeResultResolver(eu.agno3.orchestrator.system.base.execution.UnitResults)
     */
    @Override
    public ResultResolver makeResultResolver ( UnitResults results ) {
        return new ResultResolverImpl(results);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#registerEventListener(eu.agno3.orchestrator.system.base.execution.ExecutorEventListener)
     */
    @Override
    public void registerEventListener ( ExecutorEventListener listener ) {
        this.listeners.add(listener);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Runner#resume(eu.agno3.orchestrator.system.base.execution.SuspendData)
     */
    @Override
    public Result resume ( SuspendData resume ) {
        Out out = new OutLoggerBridge(Logger.getLogger(resume.getJob().getName()));
        ExecutionConfig cfg = new ExecutionConfigImpl();
        return resume(resume, out, cfg, null);
    }


    @Override
    public Result resume ( SuspendData resume, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler ) {
        Context ctx = this.createContext(resume.getJob(), out, cfg, suspendHandler);
        return this.run(ctx, resume.getJob(), resume);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Runner#run(eu.agno3.orchestrator.system.base.execution.Job)
     */
    @Override
    public Result run ( Job job ) {
        Out out = new OutLoggerBridge(Logger.getLogger(job.getName()));
        ExecutionConfig cfg = new ExecutionConfigImpl();
        return this.run(this.createContext(job, out, cfg, null), job);
    }


    @Override
    public Result run ( Job job, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler ) {
        return this.run(this.createContext(job, out, cfg, suspendHandler), job);
    }


    /**
     * @param cfg
     * @return a context for the job
     */
    protected Context createContext ( Job job, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler ) {
        Context c = new BaseContextImpl(this, out, cfg, suspendHandler, job.getFlags());
        for ( ExecutorEventListener l : this.listeners ) {
            c.registerEventListener(l);
        }
        return c;
    }


    /**
     * Testing only
     * 
     * @param ctx
     * 
     * @param phase
     * @param job
     * @return the result
     */
    public Result runPhase ( Context ctx, Phase phase, Job job ) {
        ctx.pushExecutorContext(new JobExecutorContextImpl(ctx, job));
        this.getExecutor().runPhase(ctx, phase, job.getExecutionUnits(), false);
        return ctx.popExecutorContext().getResult();
    }


    /**
     * 
     * @param ctx
     * @param job
     * @return the result
     */
    public Result run ( Context ctx, Job job ) {
        return run(ctx, job, null);
    }


    /**
     * 
     * @param ctx
     * @param job
     * @param resume
     * @return the result
     */
    public Result run ( Context ctx, Job job, SuspendData resume ) {
        ctx.pushExecutorContext(new JobExecutorContextImpl(ctx, job));
        if ( resume == null ) {
            ctx.publishEvent(new StartJobEvent(ctx, job));
        }
        else {
            ctx.restore(resume);
        }
        if ( this.getExecutor().run(ctx, job, resume) ) {
            return new JobSuspendResult(ctx.getExecutorContext().getResult());
        }
        Result r = ctx.getExecutorContext().getResult();
        ctx.publishEvent(new CompleteJobEvent(ctx, job, r));
        ctx.popExecutorContext();
        return r;
    }

}

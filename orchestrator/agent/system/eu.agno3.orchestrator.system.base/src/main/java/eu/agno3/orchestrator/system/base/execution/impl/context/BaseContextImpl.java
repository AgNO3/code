/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.ExecutorEventListener;
import eu.agno3.orchestrator.system.base.execution.JobExecutorContext;
import eu.agno3.orchestrator.system.base.execution.JobSuspendHandler;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public class BaseContextImpl implements Context {

    private static final Logger log = Logger.getLogger(BaseContextImpl.class);

    private final Deque<JobExecutorContext> contextStack = new LinkedList<>();
    private final Runner runner;
    private final Out jobOutput;
    private final Set<ExecutorEventListener> listeners = new HashSet<>();
    private final Set<String> flags;
    private ExecutionConfig executionCfg;

    private JobSuspendHandler suspendHandler;


    /**
     * 
     * @param r
     * @param out
     * @param cfg
     * @param flags
     */
    public BaseContextImpl ( Runner r, Out out, ExecutionConfig cfg, String... flags ) {
        this(r, out, cfg, null, flags);
    }


    /**
     * @param r
     * @param out
     * @param cfg
     * @param suspendHandler
     * @param flags
     */
    public BaseContextImpl ( Runner r, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler, String... flags ) {
        this.runner = r;
        this.jobOutput = out;
        this.executionCfg = cfg;
        this.suspendHandler = suspendHandler;
        this.flags = new HashSet<>(Arrays.asList(flags));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Context#canSuspend()
     */
    @Override
    public boolean canSuspend () {
        return this.suspendHandler != null;
    }


    @Override
    public JobExecutorContext getExecutorContext () {
        JobExecutorContext ctx = this.contextStack.peek();
        if ( ctx == null ) {
            throw new IllegalStateException("Context stack unavailable"); //$NON-NLS-1$
        }
        return ctx;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Context#getFlag(java.lang.String)
     */
    @Override
    public boolean getFlag ( String flag ) {
        return this.flags.contains(flag);
    }


    @Override
    public void pushExecutorContext ( JobExecutorContext r ) {
        this.contextStack.push(r);
    }


    @Override
    public JobExecutorContext popExecutorContext () {
        return this.contextStack.pop();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ResultResolver#fetchResult(eu.agno3.orchestrator.system.base.execution.ResultReference)
     */
    @Override
    public <T extends Result> T fetchResult ( ResultReference<T> ref ) throws ResultReferenceException {
        return this.getExecutorContext().getResultResolver().fetchResult(ref);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#publishEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void publishEvent ( ExecutorEvent ev ) {
        this.getExecutorContext().publishEvent(ev);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getResult()
     */
    @Override
    public Result getResult () {
        return this.getExecutorContext().getResult();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getRunner()
     */
    @Override
    public Runner getRunner () {
        return this.runner;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getOutput()
     */
    @Override
    public Out getOutput () {
        return this.getExecutorContext().getOutput();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getConfig()
     */
    @Override
    public ExecutionConfig getConfig () {
        return this.getExecutionConfig();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getJobOutput()
     */
    @Override
    public Out getJobOutput () {
        return this.jobOutput;
    }


    /**
     * @return the executionCfg
     */
    public ExecutionConfig getExecutionConfig () {
        return this.executionCfg;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#registerEventListener(eu.agno3.orchestrator.system.base.execution.ExecutorEventListener)
     */
    @Override
    public void registerEventListener ( ExecutorEventListener l ) {
        this.listeners.add(l);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#notifyListeners(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void notifyListeners ( ExecutorEvent ev ) {
        for ( ExecutorEventListener l : this.listeners ) {
            try {
                l.onEvent(ev);
            }
            catch ( Exception e ) {
                log.warn("Event listener threw exception " + ev.getClass().getName(), e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getStateMessage()
     */
    @Override
    public String getStateMessage () {
        return this.getExecutorContext().getStateMessage();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#setStateMessage(java.lang.String)
     */
    @Override
    public void setStateMessage ( String s ) {
        this.getExecutorContext().setStateMessage(s);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#getStateContext()
     */
    @Override
    public Map<String, String> getStateContext () {
        return this.getExecutorContext().getStateContext();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Context#addStateContext(java.lang.String, java.lang.String)
     */
    @Override
    public void addStateContext ( String key, String val ) {
        this.getExecutorContext().addStateContext(key, val);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * 
     * 
     *
     * @see eu.agno3.orchestrator.system.base.execution.Context#suspend(int, java.util.Iterator)
     */
    @Override
    public Iterator<ExecutionUnit<?, ?, ?>> suspend ( int after, Iterator<ExecutionUnit<?, ?, ?>> cur ) throws ExecutionException {

        log.debug("Suspending job"); //$NON-NLS-1$

        int rem = after;
        List<ExecutionUnit<?, ?, ?>> remaining = new ArrayList<>();
        while ( rem > 0 && cur.hasNext() ) {
            remaining.add(cur.next());
            rem--;
        }

        if ( rem > 0 ) {
            throw new IllegalArgumentException("Not enough units left"); //$NON-NLS-1$
        }

        JobSuspendData suspend = new JobSuspendData(cur);
        for ( JobExecutorContext ctx : this.contextStack ) {
            ctx.save(suspend);
        }
        this.suspendHandler.suspended(suspend);
        return remaining.iterator();
    }


    @Override
    public Iterator<ExecutionUnit<?, ?, ?>> restore ( SuspendData data ) {
        if ( ! ( data instanceof JobSuspendData ) ) {
            throw new IllegalArgumentException();
        }
        getExecutorContext().restore((JobSuspendData) data);
        return data.getRemainIterator();
    }
}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.JobExecutorContext;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultResolver;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public class UnitExecutionContext implements JobExecutorContext {

    private static final Logger log = Logger.getLogger(UnitExecutionContext.class);

    private JobExecutorContext parent;
    private ExecutionUnit<?, ?, ?> unit;

    private String localMessage;
    private HashMap<String, String> localContext = new HashMap<>();


    /**
     * @param parent
     * @param unit
     */
    public UnitExecutionContext ( JobExecutorContext parent, ExecutionUnit<?, ?, ?> unit ) {
        this.parent = parent;
        this.unit = unit;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#restore(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void restore ( JobSuspendData data ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#save(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void save ( JobSuspendData data ) {

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getResult()
     */
    @Override
    public Result getResult () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getResultResolver()
     */
    @Override
    public ResultResolver getResultResolver () {
        return this.parent.getResultResolver();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getOutput()
     */
    @Override
    public Out getOutput () {
        if ( this.unit instanceof Named ) {
            return this.parent.getOutput().getChild( ( (Named) this.unit ).getName());
        }
        return this.parent.getOutput().getChild(this.unit.getClass().getSimpleName());
    }


    /**
     * @return the unit
     */
    public ExecutionUnit<?, ?, ?> getUnit () {
        return this.unit;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#publishEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void publishEvent ( ExecutorEvent ev ) {
        log.trace(ev);
        this.parent.publishEvent(ev);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getContext()
     */
    @Override
    public Context getContext () {
        return this.parent.getContext();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getStateMessage()
     */
    @Override
    public String getStateMessage () {
        if ( this.localMessage != null ) {
            return this.localMessage;
        }
        return this.parent.getStateMessage();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#setStateMessage(java.lang.String)
     */
    @Override
    public void setStateMessage ( String s ) {
        this.localMessage = s;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getStateContext()
     */
    @Override
    public Map<String, String> getStateContext () {
        Map<String, String> res = new HashMap<>(this.parent.getStateContext());
        res.putAll(this.localContext);
        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#addStateContext(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void addStateContext ( String key, String val ) {
        this.localContext.put(key, val);
    }

}

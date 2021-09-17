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
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.JobExecutorContext;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.ResultResolver;
import eu.agno3.orchestrator.system.base.execution.events.AbstractUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.LeaveUnitEvent;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public class PhaseExecutorContextImpl implements JobExecutorContext {

    private static final Logger log = Logger.getLogger(PhaseExecutorContextImpl.class);

    private ResultResolver resultResolver;
    private PhaseResult results;
    private Phase phase;
    private JobExecutorContext parent;


    /**
     * @param parent
     * @param phase
     */
    public PhaseExecutorContextImpl ( JobExecutorContext parent, Phase phase ) {
        this.parent = parent;
        this.phase = phase;
        this.results = new PhaseResult();
        this.resultResolver = parent.getContext().getRunner().makeResultResolver(this.results);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#save(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void save ( JobSuspendData data ) {
        data.setPhase(this.phase);
        data.setPhaseResult(this.results);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#restore(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
     */
    @Override
    public void restore ( JobSuspendData data ) {
        this.publishEvent(new EnterPhaseEvent(this.parent.getContext(), data.getPhase()));
        this.phase = data.getPhase();
        this.results = data.getPhaseResult();
        this.resultResolver = this.parent.getContext().getRunner().makeResultResolver(this.results);
    }


    /**
     * @return the resultResolver
     */
    @Override
    public ResultResolver getResultResolver () {
        return this.resultResolver;
    }


    /**
     * @return the phase
     */
    public Phase getPhase () {
        return this.phase;
    }


    /**
     * @return the result collection
     */
    @Override
    public PhaseResult getResult () {
        return this.results;
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
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getOutput()
     */
    @Override
    public Out getOutput () {
        return this.parent.getOutput().getChild(this.phase.toString());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#publishEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void publishEvent ( ExecutorEvent ev ) {
        log.trace(ev);

        if ( ev instanceof EnterUnitEvent ) {
            AbstractUnitEvent eev = (AbstractUnitEvent) ev;
            this.getContext().pushExecutorContext(new UnitExecutionContext(this, eev.getUnit()));
        }

        // ensure proper context in listeners by this ordering
        this.parent.publishEvent(ev);

        if ( ev instanceof LeaveUnitEvent ) {
            LeaveUnitEvent eev = (LeaveUnitEvent) ev;
            this.results.add(eev.getUnit(), eev.getResult());
            this.getContext().popExecutorContext();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobExecutorContext#getStateMessage()
     */
    @Override
    public String getStateMessage () {
        return "{phase.run}"; //$NON-NLS-1$
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
        Map<String, String> res = new HashMap<>(this.parent.getStateContext());
        res.put("phase", this.phase.name()); //$NON-NLS-1$
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
        // ignore
    }

}

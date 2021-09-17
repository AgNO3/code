/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.Iterator;

import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseResults;
import eu.agno3.orchestrator.system.base.execution.SuspendData;


/**
 * @author mbechler
 *
 */
public class JobSuspendData implements SuspendData {

    /**
     * 
     */
    private static final long serialVersionUID = -6708701145767435974L;
    private PhaseResult phaseResult;
    private Phase phase;
    private PhaseResults phaseResults;
    private Job job;
    private Iterator<ExecutionUnit<?, ?, ?>> remainIterator;


    /**
     * @param cur
     */
    public JobSuspendData ( Iterator<ExecutionUnit<?, ?, ?>> cur ) {
        this.remainIterator = cur;
    }


    /**
     * @return the remainIterator
     */
    @Override
    public Iterator<ExecutionUnit<?, ?, ?>> getRemainIterator () {
        return this.remainIterator;
    }


    /**
     * @param job
     */
    void setJob ( Job job ) {
        this.job = job;
    }


    /**
     * @return the job
     */
    @Override
    public Job getJob () {
        return this.job;
    }


    /**
     * @param results
     */
    public void setPhaseResults ( PhaseResults results ) {
        this.phaseResults = results;
    }


    /**
     * @return the phaseResults
     */
    public PhaseResults getPhaseResults () {
        return this.phaseResults;
    }


    /**
     * @param phase
     *            the phase to set
     */
    void setPhase ( Phase phase ) {
        this.phase = phase;
    }


    /**
     * @return the phase
     */
    public Phase getPhase () {
        return this.phase;
    }


    /**
     * @param results
     */
    void setPhaseResult ( PhaseResult results ) {
        this.phaseResult = results;
    }


    /**
     * @return the phaseResult
     */
    public PhaseResult getPhaseResult () {
        return this.phaseResult;
    }

}

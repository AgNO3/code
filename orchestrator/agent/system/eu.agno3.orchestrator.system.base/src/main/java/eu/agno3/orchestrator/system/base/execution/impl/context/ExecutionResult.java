/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.EnumMap;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseResults;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.result.AbstractAggregateResult;


/**
 * @author mbechler
 * 
 */
public class ExecutionResult extends AbstractAggregateResult implements PhaseResults {

    /**
     * 
     */
    private static final long serialVersionUID = -5373909260235552250L;
    private Map<Phase, UnitResults> phaseResults = new EnumMap<>(Phase.class);


    /**
     * 
     */
    public ExecutionResult () {}


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.PhaseResults#add(eu.agno3.orchestrator.system.base.execution.Phase,
     *      eu.agno3.orchestrator.system.base.execution.UnitResults)
     */
    @Override
    public void add ( Phase p, UnitResults r ) {
        this.phaseResults.put(p, r);
        super.add(r);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#suspended()
     */
    @Override
    public boolean suspended () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.PhaseResults#getPhaseResult(eu.agno3.orchestrator.system.base.execution.Phase)
     */
    @Override
    public UnitResults getPhaseResult ( Phase p ) {
        UnitResults r = this.phaseResults.get(p);

        if ( r == null ) {
            throw new IndexOutOfBoundsException("No result for the given phase available"); //$NON-NLS-1$
        }

        return r;
    }
}

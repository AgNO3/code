/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.context;


import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.result.AbstractAggregateResult;


/**
 * @author mbechler
 * 
 */
public class PhaseResult extends AbstractAggregateResult implements UnitResults {

    /**
     * 
     */
    private static final long serialVersionUID = 3628632906658736294L;

    private List<ExecutionUnit<?, ?, ?>> ranEUs = new LinkedList<>();
    private Map<ExecutionUnit<?, ?, ?>, Result> euResults = new HashMap<>();


    /**
     * Add an execution result
     * 
     * @param eu
     * @param r
     */
    @Override
    public void add ( ExecutionUnit<?, ?, ?> eu, Result r ) {
        this.ranEUs.add(eu);
        this.euResults.put(eu, r);
        super.add(r);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.UnitResults#getExecutedUnits()
     */
    @Override
    public List<ExecutionUnit<?, ?, ?>> getExecutedUnits () {
        return Collections.unmodifiableList(this.ranEUs);
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
     * @see eu.agno3.orchestrator.system.base.execution.UnitResults#getResult(eu.agno3.orchestrator.system.base.execution.ExecutionUnit)
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends Result> T getResult ( ExecutionUnit<T, ?, ?> eu ) throws ResultReferenceException {
        Result r = this.euResults.get(eu);

        if ( r == null ) {
            throw new ResultReferenceException("No result for the given EU"); //$NON-NLS-1$
        }

        return (T) r;
    }

}

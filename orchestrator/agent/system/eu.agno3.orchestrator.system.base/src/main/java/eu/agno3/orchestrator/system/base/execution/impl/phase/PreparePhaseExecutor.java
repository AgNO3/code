/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.phase;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.PhaseExecutor;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public class PreparePhaseExecutor implements PhaseExecutor {

    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * 
     * @see eu.agno3.orchestrator.system.base.execution.PhaseExecutor#execute(eu.agno3.orchestrator.system.base.execution.ExecutionUnit,
     *      eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public Result execute ( ExecutionUnit<?, ?, ?> unit, Context context ) throws ExecutionException {
        return unit.prepare(context);
    }

}

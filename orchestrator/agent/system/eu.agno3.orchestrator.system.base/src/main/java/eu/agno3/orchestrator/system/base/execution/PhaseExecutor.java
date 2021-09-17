/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public interface PhaseExecutor {

    /**
     * @param unit
     *            Unit to execute
     * @param context
     * @return the unit's result
     * @throws ExecutionException
     */
    Result execute ( ExecutionUnit<?, ?, ?> unit, Context context ) throws ExecutionException;
}

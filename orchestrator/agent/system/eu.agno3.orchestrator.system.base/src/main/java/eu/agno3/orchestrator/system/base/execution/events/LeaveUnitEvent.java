/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Result;


/**
 * @author mbechler
 * 
 */
public class LeaveUnitEvent extends AbstractUnitEvent implements ResultEvent {

    private Result result;


    /**
     * @param ctx
     * @param eu
     * @param r
     */
    public LeaveUnitEvent ( Context ctx, ExecutionUnit<?, ?, ?> eu, Result r ) {
        super(ctx, eu);
        this.result = r;
    }


    /**
     * @return the result
     */
    @Override
    public Result getResult () {
        return this.result;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Leave unit %s with result %s", this.getUnit(), this.result); //$NON-NLS-1$
    }
}

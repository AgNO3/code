/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.UnitResults;


/**
 * @author mbechler
 * 
 */
public class LeavePhaseEvent extends AbstractPhaseEvent implements ResultEvent {

    private UnitResults result;


    /**
     * @param ctx
     * @param p
     * @param result
     * 
     */
    public LeavePhaseEvent ( Context ctx, Phase p, UnitResults result ) {
        super(ctx, p);
        this.result = result;
    }


    /**
     * @return the result
     */
    @Override
    public UnitResults getResult () {
        return this.result;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Leave %s with result %s", this.getPhase(), this.result); //$NON-NLS-1$
    }

}

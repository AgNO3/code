/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Phase;


/**
 * @author mbechler
 * 
 */
public class EnterPhaseEvent extends AbstractPhaseEvent {

    /**
     * @param ctx
     * @param p
     */
    public EnterPhaseEvent ( Context ctx, Phase p ) {
        super(ctx, p);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "Enter " + this.getPhase(); //$NON-NLS-1$
    }
}

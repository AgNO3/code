/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;


/**
 * @author mbechler
 * 
 */
public class EnterUnitEvent extends AbstractUnitEvent {

    /**
     * @param ctx
     * @param eu
     */
    public EnterUnitEvent ( Context ctx, ExecutionUnit<?, ?, ?> eu ) {
        super(ctx, eu);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "Enter unit " + this.getUnit(); //$NON-NLS-1$
    }
}

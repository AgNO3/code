/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Phase;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractPhaseEvent extends AbstractExecutorEvent {

    private Phase phase;


    /**
     * @param context
     * @param p
     */
    public AbstractPhaseEvent ( Context context, Phase p ) {
        super(context);
        this.phase = p;
    }


    /**
     * @return the phase
     */
    public Phase getPhase () {
        return this.phase;
    }

}
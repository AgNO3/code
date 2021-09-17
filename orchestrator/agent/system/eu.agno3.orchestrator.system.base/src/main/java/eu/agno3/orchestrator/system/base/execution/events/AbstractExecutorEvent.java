/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractExecutorEvent implements ExecutorEvent {

    private Context context;


    protected AbstractExecutorEvent ( Context context ) {
        this.context = context;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutorEvent#getContext()
     */
    @Override
    public final Context getContext () {
        return this.context;
    }

}

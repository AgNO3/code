/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;


/**
 * @author mbechler
 *
 */
public class SuspendEvent extends AbstractExecutorEvent {

    /**
     * @param ctx
     */
    public SuspendEvent ( Context ctx ) {
        super(ctx);
    }

}

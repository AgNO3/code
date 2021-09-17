/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Job;


/**
 * @author mbechler
 * 
 */
public class StartJobEvent extends AbstractJobEvent {

    /**
     * 
     * @param ctx
     * @param j
     */
    public StartJobEvent ( Context ctx, Job j ) {
        super(ctx, j);
    }

}

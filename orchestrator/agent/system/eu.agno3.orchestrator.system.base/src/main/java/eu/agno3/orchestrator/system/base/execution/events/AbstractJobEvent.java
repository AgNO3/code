/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Job;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractJobEvent extends AbstractExecutorEvent {

    private Job job;


    /**
     * @param ctx
     * @param j
     */
    public AbstractJobEvent ( Context ctx, Job j ) {
        super(ctx);
        this.job = j;
    }


    /**
     * @return the job
     */
    public Job getJob () {
        return this.job;
    }

}
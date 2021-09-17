/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.Result;


/**
 * @author mbechler
 * 
 */
public class CompleteJobEvent extends AbstractJobEvent implements ResultEvent {

    private Result result;


    /**
     * 
     * @param ctx
     * @param j
     * @param r
     */
    public CompleteJobEvent ( Context ctx, Job j, Result r ) {
        super(ctx, j);
        this.result = r;
    }


    /**
     * @return the result
     */
    @Override
    public Result getResult () {
        return this.result;
    }

}

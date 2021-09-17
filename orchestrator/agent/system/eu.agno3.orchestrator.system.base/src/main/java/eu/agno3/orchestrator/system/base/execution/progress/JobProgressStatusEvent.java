/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.events.AbstractExecutorEvent;


/**
 * @author mbechler
 * 
 */
public class JobProgressStatusEvent extends AbstractExecutorEvent {

    private JobProgressInfo info;


    /**
     * @param context
     */
    protected JobProgressStatusEvent ( Context context, JobProgressInfo info ) {
        super(context);
        this.info = info;
    }


    /**
     * @return the info
     */
    public JobProgressInfo getProgressInfo () {
        return this.info;
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobTarget;


/**
 * @author mbechler
 * 
 */
public interface QueueFactory {

    /**
     * @param group
     * @param jst
     * @return a queue for the given group
     */
    InternalQueue createForGroup ( JobGroup group, JobStateTracker jst );


    /**
     * @param groupQueue
     * @param target
     * @param jst
     * @return a queue for the given target
     */
    InternalQueue createTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst );


    /**
     * @param target
     * @return whether this is a local target
     */
    boolean isLocal ( JobTarget target );

}

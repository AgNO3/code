/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.queue;


import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;


/**
 * @author mbechler
 * 
 */
public class QueueFactoryImpl implements QueueFactory {

    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.QueueFactory#createForGroup(eu.agno3.orchestrator.jobs.JobGroup,
     *      eu.agno3.orchestrator.jobs.coord.JobStateTracker)
     */
    @Override
    public InternalQueue createForGroup ( JobGroup group, JobStateTracker jst ) {
        return new GroupQueue(group, this, jst);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.QueueFactory#createTargetQueue(eu.agno3.orchestrator.jobs.coord.InternalQueue,
     *      eu.agno3.orchestrator.jobs.JobTarget, eu.agno3.orchestrator.jobs.coord.JobStateTracker)
     */
    @Override
    public InternalQueue createTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst ) {
        return new LocalTargetQueue(groupQueue, target, jst);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.QueueFactory#isLocal(eu.agno3.orchestrator.jobs.JobTarget)
     */
    @Override
    public boolean isLocal ( JobTarget target ) {
        return true;
    }
}

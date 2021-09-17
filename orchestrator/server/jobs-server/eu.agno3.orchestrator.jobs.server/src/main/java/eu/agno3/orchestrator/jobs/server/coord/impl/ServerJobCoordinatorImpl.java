/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.impl;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.coord.ExecutorFactory;
import eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl;
import eu.agno3.orchestrator.jobs.server.coord.db.impl.DatabaseJobStateTrackerImpl;
import eu.agno3.orchestrator.jobs.state.JobStateListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    JobCoordinator.class, JobStateListener.class, JobForExecutionProvider.class
} )
public class ServerJobCoordinatorImpl extends JobCoordinatorImpl {

    /**
     */
    public ServerJobCoordinatorImpl () {
        super();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.init();
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindJobGroup ( JobGroup g ) {
        this.addJobGroup(g);
    }


    protected synchronized void unbindJobGroup ( JobGroup g ) {
        this.removeJobGroup(g);
    }


    @Reference
    protected synchronized void setJobStateTracker ( DatabaseJobStateTrackerImpl jst ) {
        super.setJobStateTracker(jst);
    }


    protected synchronized void unsetJobStateTracker ( DatabaseJobStateTrackerImpl jst ) {
        super.unsetJobStateTracker(jst);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#setExecutorFactory(eu.agno3.orchestrator.jobs.coord.ExecutorFactory)
     */
    @Override
    @Reference
    protected synchronized void setExecutorFactory ( ExecutorFactory ef ) {
        super.setExecutorFactory(ef);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#unsetExecutorFactory(eu.agno3.orchestrator.jobs.coord.ExecutorFactory)
     */
    @Override
    protected synchronized void unsetExecutorFactory ( ExecutorFactory ef ) {
        super.unsetExecutorFactory(ef);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#setQueueFactory(eu.agno3.orchestrator.jobs.coord.QueueFactory)
     */
    @Override
    @Reference
    protected synchronized void setQueueFactory ( QueueFactory queueFactory ) {
        super.setQueueFactory(queueFactory);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#unsetQueueFactory(eu.agno3.orchestrator.jobs.coord.QueueFactory)
     */
    @Override
    protected synchronized void unsetQueueFactory ( QueueFactory qf ) {
        super.unsetQueueFactory(qf);
    }

}

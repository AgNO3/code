/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.config;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.jobs.ConfigApplyJob;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.jobs.server.JobOutputTracker;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;
import eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.config.model.jobs.ConfigApplyJob" )
@JobType ( ConfigApplyJob.class )
public class ConfigApplyJobRunnableFactory extends CompoundRunnableFactory {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#setJobCoordinator(eu.agno3.orchestrator.jobs.JobCoordinator)
     */
    @Override
    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator coord ) {
        super.setJobCoordinator(coord);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#unsetJobCoordinator(eu.agno3.orchestrator.jobs.JobCoordinator)
     */
    @Override
    protected synchronized void unsetJobCoordinator ( JobCoordinator coord ) {
        super.unsetJobCoordinator(coord);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#setJobOutputTracker(eu.agno3.orchestrator.jobs.server.JobOutputTracker)
     */
    @Override
    @Reference
    protected synchronized void setJobOutputTracker ( JobOutputTracker jot ) {
        super.setJobOutputTracker(jot);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#unsetJobOutputTracker(eu.agno3.orchestrator.jobs.server.JobOutputTracker)
     */
    @Override
    protected synchronized void unsetJobOutputTracker ( JobOutputTracker jot ) {
        super.unsetJobOutputTracker(jot);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#setJobProgressTracker(eu.agno3.orchestrator.jobs.server.JobProgressTracker)
     */
    @Override
    @Reference
    protected synchronized void setJobProgressTracker ( JobProgressTracker jpt ) {
        super.setJobProgressTracker(jpt);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.compound.CompoundRunnableFactory#unsetJobProgressTracker(eu.agno3.orchestrator.jobs.server.JobProgressTracker)
     */
    @Override
    protected synchronized void unsetJobProgressTracker ( JobProgressTracker jpt ) {
        super.unsetJobProgressTracker(jpt);
    }

}

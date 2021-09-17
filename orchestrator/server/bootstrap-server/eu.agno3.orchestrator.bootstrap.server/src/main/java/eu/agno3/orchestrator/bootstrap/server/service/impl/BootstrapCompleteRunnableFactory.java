/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.service.impl;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.bootstrap.jobs.BootstrapCompleteJob;
import eu.agno3.orchestrator.bootstrap.server.service.BootstrapServerService;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.bootstrap.jobs.BootstrapCompleteJob" )
@JobType ( BootstrapCompleteJob.class )
public class BootstrapCompleteRunnableFactory implements JobRunnableFactory<BootstrapCompleteJob> {

    private BootstrapServerService bootstrapService;


    @Reference
    protected synchronized void setBootstrapService ( BootstrapServerService bs ) {
        this.bootstrapService = bs;
    }


    protected synchronized void unsetBootstrapService ( BootstrapServerService bs ) {
        if ( this.bootstrapService == bs ) {
            this.bootstrapService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( BootstrapCompleteJob j ) throws JobRunnableException {
        return new BootstrapCompleteRunnable(this.bootstrapService, j.getContext());
    }

}

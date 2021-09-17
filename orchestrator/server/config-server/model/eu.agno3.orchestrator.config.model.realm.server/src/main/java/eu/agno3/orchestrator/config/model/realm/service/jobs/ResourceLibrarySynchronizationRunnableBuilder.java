/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.jobs.ResourceLibrarySynchronizationJob;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.config.model.jobs.ResourceLibrarySynchronizationJob" )
@JobType ( ResourceLibrarySynchronizationJob.class )
public class ResourceLibrarySynchronizationRunnableBuilder implements JobRunnableFactory<ResourceLibrarySynchronizationJob> {

    private ResourceLibraryServerService resLibraryService;


    @Reference
    protected synchronized void setResourceLibraryService ( ResourceLibraryServerService rls ) {
        this.resLibraryService = rls;
    }


    protected synchronized void unsetResourceLibraryService ( ResourceLibraryServerService rls ) {
        if ( this.resLibraryService == rls ) {
            this.resLibraryService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( ResourceLibrarySynchronizationJob j ) throws JobRunnableException {
        return new ResourceLibrarySynchronizationRunnable(this.resLibraryService, j.getService(), j.getLibrary(), j.getHint());
    }

}

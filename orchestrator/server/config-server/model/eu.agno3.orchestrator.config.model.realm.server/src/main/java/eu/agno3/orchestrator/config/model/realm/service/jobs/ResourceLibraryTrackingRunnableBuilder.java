/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.jobs.ResourceLibraryTrackingJob;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.config.model.jobs.ResourceLibraryTrackingJob" )
@JobType ( ResourceLibraryTrackingJob.class )
public class ResourceLibraryTrackingRunnableBuilder implements JobRunnableFactory<ResourceLibraryTrackingJob> {

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
    public JobRunnable getRunnableForJob ( ResourceLibraryTrackingJob j ) throws JobRunnableException {
        ResourceLibraryServerService rls = this.resLibraryService;
        StructuralObject anchor = j.getAnchor();
        DateTime lastModified = j.getLastModified();
        if ( rls == null || anchor == null || lastModified == null ) {
            throw new JobRunnableException("Invalid state or parameters"); //$NON-NLS-1$
        }
        return new ResourceLibraryTrackingRunnable(rls, anchor, lastModified);
    }

}

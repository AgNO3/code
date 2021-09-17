/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 18, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryTrackingRunnable implements JobRunnable {

    private static final Logger log = Logger.getLogger(ResourceLibraryTrackingRunnable.class);

    @NonNull
    private final ResourceLibraryServerService resLibraryService;
    @NonNull
    private final StructuralObject anchor;
    @NonNull
    private final DateTime lastModified;


    /**
     * @param resLibraryService
     * @param anchor
     * @param lastModified
     */
    public ResourceLibraryTrackingRunnable ( @NonNull ResourceLibraryServerService resLibraryService, @NonNull StructuralObject anchor,
            @NonNull DateTime lastModified ) {
        this.resLibraryService = resLibraryService;
        this.anchor = anchor;
        this.lastModified = lastModified;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
        try {
            log.debug("Tracking resource library synchronization"); //$NON-NLS-1$
            this.resLibraryService.trackSynchronized(this.anchor, this.lastModified);
        }
        catch (
            ModelServiceException |
            ModelObjectNotFoundException e ) {
            log.warn("Failed to track library synchronization", e); //$NON-NLS-1$
            return JobState.FAILED;
        }
        return JobState.FINISHED;
    }

}

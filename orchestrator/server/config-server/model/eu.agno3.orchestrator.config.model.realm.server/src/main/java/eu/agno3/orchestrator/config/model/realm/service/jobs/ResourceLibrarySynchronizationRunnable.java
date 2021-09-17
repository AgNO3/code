/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 *
 */
public class ResourceLibrarySynchronizationRunnable implements JobRunnable {

    private final ResourceLibraryServerService resLibraryService;
    private final ServiceStructuralObject service;
    private final ResourceLibrary library;
    private final String hint;


    /**
     * @param resLibraryService
     * @param service
     * @param library
     * @param hint
     */
    public ResourceLibrarySynchronizationRunnable ( ResourceLibraryServerService resLibraryService, ServiceStructuralObject service,
            ResourceLibrary library, String hint ) {
        this.resLibraryService = resLibraryService;
        this.service = service;
        this.library = library;
        this.hint = hint;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {

        ServiceStructuralObject serviceObj = this.service;
        ResourceLibrary resLibrary = this.library;
        if ( serviceObj == null || resLibrary == null ) {
            outHandler.logLineError("Missing service/library"); //$NON-NLS-1$
            return JobState.FAILED;
        }

        this.resLibraryService.synchronizeServiceLibraries(serviceObj, resLibrary, this.hint, outHandler);
        return JobState.FINISHED;
    }
}

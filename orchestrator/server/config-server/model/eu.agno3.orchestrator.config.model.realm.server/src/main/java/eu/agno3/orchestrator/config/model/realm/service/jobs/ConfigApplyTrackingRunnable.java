/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 18, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceStateServerService;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 *
 */
public class ConfigApplyTrackingRunnable implements JobRunnable {

    @NonNull
    private final InstanceStateServerService instanceState;
    @NonNull
    private final StructuralObject anchor;


    /**
     * @param serviceService
     * @param anchor
     */
    public ConfigApplyTrackingRunnable ( @NonNull InstanceStateServerService serviceService, @NonNull StructuralObject anchor ) {
        this.instanceState = serviceService;
        this.anchor = anchor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
        if ( this.anchor instanceof InstanceStructuralObject ) {
            this.instanceState.handleInstanceConfigApplied((@NonNull InstanceStructuralObject) this.anchor);
        }
        return JobState.FINISHED;
    }

}

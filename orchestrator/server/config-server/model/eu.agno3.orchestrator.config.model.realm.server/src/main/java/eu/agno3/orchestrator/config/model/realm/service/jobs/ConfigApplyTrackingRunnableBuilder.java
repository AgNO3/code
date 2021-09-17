/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.jobs;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.jobs.ConfigApplyTrackingJob;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceStateServerService;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.config.model.jobs.ConfigApplyTrackingJob" )
@JobType ( ConfigApplyTrackingJob.class )
public class ConfigApplyTrackingRunnableBuilder implements JobRunnableFactory<ConfigApplyTrackingJob> {

    private InstanceStateServerService instanceStateService;


    @Reference
    protected synchronized void setServiceService ( InstanceStateServerService iss ) {
        this.instanceStateService = iss;
    }


    protected synchronized void unsetServiceService ( InstanceStateServerService iss ) {
        if ( this.instanceStateService == iss ) {
            this.instanceStateService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( ConfigApplyTrackingJob j ) throws JobRunnableException {
        InstanceStateServerService iss = this.instanceStateService;
        StructuralObject anchor = j.getAnchor();
        if ( iss == null || anchor == null ) {
            throw new JobRunnableException("Invalid state or parameters"); //$NON-NLS-1$
        }
        return new ConfigApplyTrackingRunnable(iss, anchor);
    }

}

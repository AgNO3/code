/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.monitor.jobs.DisableServiceJob;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.system.monitor.jobs.DisableServiceJob" )
@JobType ( value = DisableServiceJob.class )
public class DisableServiceJobRunnableFactory implements JobRunnableFactory<DisableServiceJob> {

    private ServiceManager serviceManager;


    /**
     * @return the serviceManager
     */
    public ServiceManager getServiceManager () {
        return this.serviceManager;
    }


    // break cycle
    @Reference ( policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( DisableServiceJob j ) throws JobRunnableException {
        return new DisableServiceRunnable(j);
    }

    private class DisableServiceRunnable implements JobRunnable {

        private DisableServiceJob job;


        /**
         * @param j
         * 
         */
        public DisableServiceRunnable ( DisableServiceJob j ) {
            this.job = j;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
            outHandler.logLineInfo("Disabling service " + this.job.getService()); //$NON-NLS-1$
            ServiceManager sm = getServiceManager();

            if ( sm == null ) {
                outHandler.logLineError("Service manager unavailable"); //$NON-NLS-1$
                return JobState.FAILED;
            }
            sm.getServiceManager(StructuralObjectReferenceImpl.fromObject(this.job.getService()), BaseServiceManager.class)
                    .disable(this.job.getService().getId());
            outHandler.logLineInfo("Disabled service " + this.job.getService()); //$NON-NLS-1$
            return JobState.FINISHED;
        }

    }

}

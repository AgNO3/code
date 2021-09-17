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
import eu.agno3.orchestrator.system.monitor.jobs.EnableServiceJob;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.system.monitor.jobs.EnableServiceJob" )
@JobType ( value = EnableServiceJob.class )
public class EnableServiceJobRunnableFactory implements JobRunnableFactory<EnableServiceJob> {

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
    public JobRunnable getRunnableForJob ( EnableServiceJob j ) throws JobRunnableException {
        return new EnableServiceRunnable(j);
    }

    private class EnableServiceRunnable implements JobRunnable {

        private EnableServiceJob job;


        /**
         * @param j
         * 
         */
        public EnableServiceRunnable ( EnableServiceJob j ) {
            this.job = j;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
            outHandler.logLineInfo("Enabling service " + this.job.getService()); //$NON-NLS-1$
            ServiceManager sm = getServiceManager();

            if ( sm == null ) {
                outHandler.logLineError("Service manager unavailable"); //$NON-NLS-1$
                return JobState.FAILED;
            }
            sm.getServiceManager(StructuralObjectReferenceImpl.fromObject(this.job.getService()), BaseServiceManager.class)
                    .enable(this.job.getService().getId());
            outHandler.logLineInfo("Enabled service " + this.job.getService()); //$NON-NLS-1$
            return JobState.FINISHED;
        }

    }

}

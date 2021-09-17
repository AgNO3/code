/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.backup.BackupManager;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.backups.jobs.RestoreJob;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.system.backups.jobs.RestoreJob" )
@JobType ( value = RestoreJob.class )
public class RestoreJobRunnableFactory implements JobRunnableFactory<RestoreJob> {

    private BackupManager backupManager;


    /**
     * @return the backupManager
     */
    BackupManager getBackupManager () {
        return this.backupManager;
    }


    @Reference
    protected synchronized void setBackupManager ( BackupManager bm ) {
        this.backupManager = bm;
    }


    protected synchronized void unsetBackupManager ( BackupManager bm ) {
        if ( this.backupManager == bm ) {
            this.backupManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( RestoreJob j ) throws JobRunnableException {
        return new RestoreJobRunnable(j.getBackupId());
    }

    private class RestoreJobRunnable implements JobRunnable {

        private UUID backupId;


        /**
         * @param backupId
         * 
         */
        public RestoreJobRunnable ( UUID backupId ) {
            this.backupId = backupId;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
            outHandler.logLineInfo("Restoring backup " + this.backupId); //$NON-NLS-1$
            try {
                getBackupManager().restore(this.backupId);
            }
            catch ( Exception e ) {
                outHandler.logLineError("Failed to restore backup: " + e.getMessage(), e); //$NON-NLS-1$
                throw e;
            }
            outHandler.logLineInfo("Restored backup " + this.backupId); //$NON-NLS-1$
            return JobState.FINISHED;
        }

    }

}

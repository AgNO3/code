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
import eu.agno3.orchestrator.system.backups.jobs.BackupJob;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.system.backups.jobs.BackupJob" )
@JobType ( value = BackupJob.class )
public class BackupJobRunnableFactory implements JobRunnableFactory<BackupJob> {

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
    public JobRunnable getRunnableForJob ( BackupJob j ) throws JobRunnableException {
        return new BackupJobRunnable();
    }

    private class BackupJobRunnable implements JobRunnable {

        /**
         * 
         */
        public BackupJobRunnable () {}


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
            UUID backupId = getBackupManager().makeBackup();
            outHandler.logLineInfo("Created backup " + backupId); //$NON-NLS-1$
            return JobState.FINISHED;
        }

    }

}

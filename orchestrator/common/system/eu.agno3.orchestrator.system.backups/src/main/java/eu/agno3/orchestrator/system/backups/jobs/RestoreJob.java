/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.backups.jobs;


import java.util.UUID;

import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class RestoreJob extends JobImpl {

    private UUID backupId;


    /**
     * 
     */
    public RestoreJob () {
        super(new BackupJobGroup());
    }


    /**
     * @return the backupId
     */
    public UUID getBackupId () {
        return this.backupId;
    }


    /**
     * @param backupId
     *            the backupId to set
     */
    public void setBackupId ( UUID backupId ) {
        this.backupId = backupId;
    }

}

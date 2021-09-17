/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.backups.jobs;


import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class BackupJob extends JobImpl {

    /**
     * 
     */
    public BackupJob () {
        super(new BackupJobGroup());
    }

}

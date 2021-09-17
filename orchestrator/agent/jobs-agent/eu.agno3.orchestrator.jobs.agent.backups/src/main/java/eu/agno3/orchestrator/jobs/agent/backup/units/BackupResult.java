/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.util.UUID;

import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;


/**
 * @author mbechler
 *
 */
public class BackupResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = -2551997430625697461L;

    private UUID backupId;


    /**
     * 
     */
    public BackupResult () {}


    /**
     * 
     * @param backupId
     */
    public BackupResult ( UUID backupId ) {
        this.backupId = backupId;
    }


    /**
     * @return the backupId
     */
    public UUID getBackupId () {
        return this.backupId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return Status.SUCCESS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#failed()
     */
    @Override
    public boolean failed () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#suspended()
     */
    @Override
    public boolean suspended () {
        return false;
    }

}

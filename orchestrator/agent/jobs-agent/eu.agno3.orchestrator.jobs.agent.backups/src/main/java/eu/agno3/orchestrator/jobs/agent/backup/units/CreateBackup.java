/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;


/**
 * @author mbechler
 *
 */
public class CreateBackup extends AbstractExecutionUnit<BackupResult, CreateBackup, CreateBackupConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 209063370270201247L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public BackupResult prepare ( Context context ) throws ExecutionException {
        getBackupManager(context);
        return new BackupResult();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public BackupResult execute ( Context context ) throws ExecutionException {
        BackupManager bm = getBackupManager(context);
        try {
            return new BackupResult(bm.makeBackup());
        }
        catch ( BackupException e ) {
            throw new ExecutionException("Failed to create backup", e); //$NON-NLS-1$
        }
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static BackupManager getBackupManager ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(BackupManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get backup manager", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public CreateBackupConfigurator createConfigurator () {
        return new CreateBackupConfigurator(this);
    }

}

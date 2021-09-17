/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.backup;


import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;


/**
 * @author mbechler
 *
 */
public class KeystoreBackupUnit implements BackupUnit {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnit#getId()
     */
    @Override
    public String getId () {
        return "keystores"; //$NON-NLS-1$
    }

}

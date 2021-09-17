/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;


/**
 * @author mbechler
 *
 */
public class CreateBackupConfigurator extends AbstractConfigurator<BackupResult, CreateBackup, CreateBackupConfigurator> {

    /**
     * @param unit
     */
    protected CreateBackupConfigurator ( CreateBackup unit ) {
        super(unit);
    }

}

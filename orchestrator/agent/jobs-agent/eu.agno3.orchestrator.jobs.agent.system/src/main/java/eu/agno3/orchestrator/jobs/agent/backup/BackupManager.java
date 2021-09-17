/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface BackupManager extends SystemService {

    /**
     * @return the generated backup id
     * @throws BackupException
     */
    UUID makeBackup () throws BackupException;


    /**
     * @param backupId
     * @throws BackupException
     */
    void restore ( UUID backupId ) throws BackupException;


    /**
     * @param backupId
     * @param remapService
     * @throws BackupException
     */
    void restore ( UUID backupId, Map<UUID, UUID> remapService ) throws BackupException;


    /**
     * @return the stored backups
     * @throws BackupException
     */
    List<BackupInfo> list () throws BackupException;


    /**
     * @param backupId
     * @throws BackupException
     */
    void remove ( UUID backupId ) throws BackupException;

}

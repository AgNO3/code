/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;


/**
 * @author mbechler
 *
 */
public class ConfigFileBackupUnit extends AbstractFileBackupUnit implements BackupUnit {

    private final String subPath;


    /**
     * @param id
     * 
     */
    public ConfigFileBackupUnit ( String id ) {
        super(id);
        this.subPath = null;
    }


    /**
     * @param id
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * 
     */
    public ConfigFileBackupUnit ( String id, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        super(id, owner, group, filePerms, dirPerms);
        this.subPath = null;
    }


    /**
     * @param id
     * @param subPath
     * 
     */
    public ConfigFileBackupUnit ( String id, String subPath ) {
        super(id);
        this.subPath = subPath;
    }


    /**
     * @param id
     * @param subPath
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * 
     */
    public ConfigFileBackupUnit ( String id, String subPath, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        super(id, owner, group, filePerms, dirPerms);
        this.subPath = subPath;
    }


    /**
     * @return the subPath
     */
    public String getSubPath () {
        return this.subPath;
    }

}

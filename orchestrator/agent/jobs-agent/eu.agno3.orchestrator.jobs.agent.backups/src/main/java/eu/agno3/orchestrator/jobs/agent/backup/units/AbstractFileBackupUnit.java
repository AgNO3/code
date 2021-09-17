/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
public class AbstractFileBackupUnit implements BackupUnit {

    private final String id;

    private final UserPrincipal owner;
    private final GroupPrincipal group;

    private final Set<PosixFilePermission> filePermissions;
    private final Set<PosixFilePermission> dirPermissions;

    private Set<String> excludePatterns = new HashSet<>();


    /**
     * 
     */
    protected AbstractFileBackupUnit ( String id, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        this.id = id;
        this.owner = owner;
        this.group = group;
        this.filePermissions = filePerms;
        this.dirPermissions = dirPerms;
    }


    /**
     * 
     */
    protected AbstractFileBackupUnit ( String id ) {
        this.id = id;
        this.owner = null;
        this.group = null;
        this.filePermissions = FileSecurityUtils.getGroupReadFilePermissions();
        this.dirPermissions = FileSecurityUtils.getGroupReadDirPermissions();
    }


    /**
     * @param pattern
     * @return this
     */
    public AbstractFileBackupUnit exclude ( String pattern ) {
        this.excludePatterns.add(pattern);
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnit#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * @return the owner
     */
    public UserPrincipal getOwner () {
        return this.owner;
    }


    /**
     * @return the group
     */
    public GroupPrincipal getGroup () {
        return this.group;
    }


    /**
     * @return the dirPermissions
     */
    public Set<PosixFilePermission> getDirPermissions () {
        return this.dirPermissions;
    }


    /**
     * @return the filePermissions
     */
    public Set<PosixFilePermission> getFilePermissions () {
        return this.filePermissions;
    }


    /**
     * @return the excludePatterns
     */
    public Set<String> getExcludePatterns () {
        return this.excludePatterns;
    }
}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.units;


import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class FilesBackupUnit extends AbstractFileBackupUnit {

    private final Path path;


    /**
     * @param id
     * @param p
     * 
     */
    public FilesBackupUnit ( String id, Path p ) {
        super(id);
        this.path = p;
    }


    /**
     * @param id
     * @param p
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * 
     */
    public FilesBackupUnit ( String id, Path p, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        super(id, owner, group, filePerms, dirPerms);
        this.path = p;
    }


    /**
     * @return the path
     */
    public Path getPath () {
        return this.path;
    }
}

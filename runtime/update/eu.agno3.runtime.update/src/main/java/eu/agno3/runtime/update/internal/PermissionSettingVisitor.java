/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.02.2016 by mbechler
 */
package eu.agno3.runtime.update.internal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/**
 * @author mbechler
 *
 */
final class PermissionSettingVisitor extends SimpleFileVisitor<Path> {

    private UserPrincipal owner;
    private GroupPrincipal group;
    private Set<PosixFilePermission> filePerms;
    private Set<PosixFilePermission> dirPerms;


    /**
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * 
     */
    public PermissionSettingVisitor ( UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        this.owner = owner;
        this.group = group;
        this.filePerms = filePerms;
        this.dirPerms = dirPerms;
    }


    @Override
    public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
        if ( !Files.isRegularFile(file) ) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        PosixFileAttributeView av = Files.getFileAttributeView(file, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if ( this.owner != null ) {
            av.setOwner(this.owner);
        }
        if ( this.group != null ) {
            av.setGroup(this.group);
        }
        av.setPermissions(this.filePerms);
        return FileVisitResult.CONTINUE;
    }


    @Override
    public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
        super.postVisitDirectory(dir, exc);
        PosixFileAttributeView av = Files.getFileAttributeView(dir, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if ( this.owner != null ) {
            av.setOwner(this.owner);
        }
        if ( this.group != null ) {
            av.setGroup(this.group);
        }
        av.setPermissions(this.dirPerms);
        return FileVisitResult.CONTINUE;
    }
}
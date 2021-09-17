/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 31, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.acl.util;


import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;


/**
 * @author mbechler
 *
 */
public final class ACLGroupSyncUtil {

    private static final Logger log = Logger.getLogger(ACLGroupSyncUtil.class);


    /**
     * 
     */
    private ACLGroupSyncUtil () {}


    /**
     * Add explicit user ACL entries for all users having group access to the given path
     * 
     * @param p
     * @throws IOException
     */
    public static void syncACL ( Path p ) throws IOException {

        PosixFileAttributeView posixAttrsView = Files.getFileAttributeView(p, PosixFileAttributeView.class);
        PosixFileAttributes posixAttrs = posixAttrsView.readAttributes();
        GroupPrincipal group = posixAttrs.group();
        Set<PosixFilePermission> groupPerms = EnumSet.copyOf(posixAttrs.permissions());
        groupPerms.retainAll(EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE));

        Set<UserPrincipal> members;
        try {
            members = UnixAccountUtil.getMembers(group);
        }
        catch ( UnixAccountException e ) {
            log.warn("Failed to lookup members of group " + group.getName(), e); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Group permissions are " + groupPerms); //$NON-NLS-1$
            log.debug("Group is " + group); //$NON-NLS-1$
            log.debug("Group members are " + members); //$NON-NLS-1$
        }

        Set<AclEntryPermission> mappedPermissions = mapPermissions(groupPerms);
        if ( log.isDebugEnabled() ) {
            log.debug("Mapped permissions to " + mappedPermissions); //$NON-NLS-1$
        }
        List<AclEntry> acl = new LinkedList<>();
        if ( !groupPerms.isEmpty() ) {
            for ( UserPrincipal up : members ) {
                if ( up instanceof GroupPrincipal ) {
                    log.warn("Contains a group " + up); //$NON-NLS-1$
                }
                acl.add(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPermissions(mappedPermissions).setPrincipal(up).build());
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Resuting ACL is " + acl); //$NON-NLS-1$
        }

        ACLUtil.setACL(p, acl);
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * 
     * @param p
     * @throws IOException
     */
    public static void syncACLRecursive ( Path p ) throws IOException {
        if ( !ACLUtil.aclsSupported(p) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No ACL support on " + p); //$NON-NLS-1$
            }
            return;
        }

        Files.walkFileTree(p, EnumSet.noneOf(FileVisitOption.class), 5, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                syncACL(file);
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                if ( exc != null ) {
                    getLog().warn("Recursive ACL sync failed " + file, exc); //$NON-NLS-1$
                }
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
                if ( exc != null ) {
                    getLog().warn("Recursive ACL sync failed " + dir, exc); //$NON-NLS-1$
                }
                syncACL(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }


    /**
     * @param permissions
     * @return
     */
    private static Set<AclEntryPermission> mapPermissions ( Set<PosixFilePermission> permissions ) {
        Set<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        if ( permissions.contains(PosixFilePermission.GROUP_READ) ) {
            perms.add(AclEntryPermission.READ_DATA);
        }
        if ( permissions.contains(PosixFilePermission.GROUP_WRITE) ) {
            perms.add(AclEntryPermission.WRITE_DATA);
        }
        if ( permissions.contains(PosixFilePermission.GROUP_EXECUTE) ) {
            perms.add(AclEntryPermission.EXECUTE);
        }
        return perms;
    }

}

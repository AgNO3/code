/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base32;
import org.apache.log4j.Logger;


/**
 * Utilities for safe file operations
 * 
 * Even NIO.2 does not allow to operate on file descriptors - only on paths.
 * This means we must ensure that filesytem paths cannot be replaced by an
 * attacker while operations are performed. This leads to the conclusion that
 * when operating in hostile locations (locations where other users have write
 * access - this can be gained if they have write access to any parent) only
 * one single atomic operation can be performed safely.
 * 
 * To workaround the following is required:
 * - perform all necessary operations inside a safe scratch area
 * - atomically move the created file to its new location
 * 
 * This code relies on the following assumptions:
 * - /tmp and /var/tmp are safe locations (they MUST have the sticky bit set)
 * - The superusers name is "root"
 * - the user.name system property holds the executing processes username
 * 
 * @author mbechler
 */
public final class FileSecurityUtils {

    private static final Logger log = Logger.getLogger(FileSecurityUtils.class);

    /**
     * Whitelist of paths with sticky bit set
     * 
     * Java does not allow checking of the +t bit, so we can just assume it is set
     */
    private static final Set<Path> SECURE_WHITELIST = new HashSet<>();
    private static final UserPrincipal MY_PRINCIPAL;
    private static final UserPrincipal ROOT_PRINCIPAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String OWNER_ONLY_FILE_MASK = "rw-------"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> OWNER_ONLY_FILE_PERMISSIONS = PosixFilePermissions.fromString(OWNER_ONLY_FILE_MASK);

    private static final String OWNER_ONLY_DIR_MASK = "rwx------"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> OWNER_ONLY_DIR_PERMISSIONS = PosixFilePermissions.fromString(OWNER_ONLY_DIR_MASK);

    private static final String GROUP_READ_FILE_MASK = "rw-r-----"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> GROUP_READ_FILE_PERMISSIONS = PosixFilePermissions.fromString(GROUP_READ_FILE_MASK);

    private static final String GROUP_READ_DIR_MASK = "rwxr-x---"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> GROUP_READ_DIR_PERMISSIONS = PosixFilePermissions.fromString(GROUP_READ_DIR_MASK);

    private static final String GROUP_WRITE_FILE_MASK = "rw-rw----"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> GROUP_WRITE_FILE_PERMISSIONS = PosixFilePermissions.fromString(GROUP_WRITE_FILE_MASK);

    private static final String GROUP_WRITE_DIR_MASK = "rwxrwx---"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> GROUP_WRITE_DIR_PERMISSIONS = PosixFilePermissions.fromString(GROUP_WRITE_DIR_MASK);

    private static final String WORLD_READ_FILE_MASK = "rw-r--r--"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> WORLD_READABLE_FILE_PERMISSIONS = PosixFilePermissions.fromString(WORLD_READ_FILE_MASK);

    private static final String WORLD_READ_DIR_MASK = "rwxr-xr-x"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> WORLD_READABLE_DIR_PERMISSIONS = PosixFilePermissions.fromString(WORLD_READ_DIR_MASK);

    private static final boolean RUNNING_AS_ROOT = "root".equals( //$NON-NLS-1$
        System.getProperty("user.name")); //$NON-NLS-1$

    /**
     * 
     */
    private static final String USER_NAME_PROP = "user.name"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String ROOT_USER = "root"; //$NON-NLS-1$


    static {

        try {
            MY_PRINCIPAL = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty(USER_NAME_PROP));
        }
        catch ( IOException e ) {
            throw new UnsupportedOperationException("Failed to get my user principal:", e); //$NON-NLS-1$
        }

        try {
            ROOT_PRINCIPAL = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(ROOT_USER);
        }
        catch ( IOException e ) {
            throw new UnsupportedOperationException("Failed to get root principal:", e); //$NON-NLS-1$
        }

        // SECURITY: assuming these to have the sticky bit set...
        Path tmp = FileSystems.getDefault().getPath("/tmp"); //$NON-NLS-1$
        Path varTmp = FileSystems.getDefault().getPath("/var/tmp"); //$NON-NLS-1$

        if ( Files.isDirectory(tmp, LinkOption.NOFOLLOW_LINKS) && Files.isWritable(tmp) ) {
            SECURE_WHITELIST.add(tmp);
        }
        if ( Files.isDirectory(varTmp, LinkOption.NOFOLLOW_LINKS) && Files.isWritable(varTmp) ) {
            SECURE_WHITELIST.add(varTmp);
        }
    }


    private FileSecurityUtils () {

    }


    /**
     * @return a secure random tag
     */
    public static String generateRandomTag () {
        byte[] randomBinary = new byte[16];
        RANDOM.nextBytes(randomBinary);
        Base32 enc = new Base32();
        return enc.encodeAsString(randomBinary);
    }


    /**
     * @return file permissions which only allow access by the process owner
     */
    public static Set<PosixFilePermission> getOwnerOnlyFilePermissions () {
        return OWNER_ONLY_FILE_PERMISSIONS;
    }


    /**
     * @return directory permission which only allow access by the process owner
     */
    public static Set<PosixFilePermission> getOwnerOnlyDirPermissions () {
        return OWNER_ONLY_DIR_PERMISSIONS;
    }


    /**
     * @return file permissions that allow full access by owner and read for the group
     */
    public static Set<PosixFilePermission> getGroupReadFilePermissions () {
        return GROUP_READ_FILE_PERMISSIONS;
    }


    /**
     * @return dir permissions that allow full access by owner and read for the group
     */
    public static Set<PosixFilePermission> getGroupReadDirPermissions () {
        return GROUP_READ_DIR_PERMISSIONS;
    }


    /**
     * 
     * @return file permissions that allow full access by owner and read for everyone else
     */
    public static Set<PosixFilePermission> getWorldReadableFilePermissions () {
        return WORLD_READABLE_FILE_PERMISSIONS;
    }


    /**
     * @return dir permissions that allow full access by owner and read for everyone else
     */
    public static Set<PosixFilePermission> getWorldReadableDirPermissions () {
        return WORLD_READABLE_DIR_PERMISSIONS;
    }


    /**
     * @return dir permissions that allow full access by owner and write for the group
     */
    public static Set<PosixFilePermission> getGroupWriteDirPermissions () {
        return GROUP_WRITE_DIR_PERMISSIONS;
    }


    /**
     * 
     * @return file permissions that allow full access by owner and write for the group
     */
    public static Set<PosixFilePermission> getGroupWriteFilePermissions () {
        return GROUP_WRITE_FILE_PERMISSIONS;
    }


    /**
     * 
     * @param p
     *            a directory
     * @param symlinkLimit
     * @return whether the given path is secure, meaning that other user except root has write access to any directory
     *         in the hierarchy.
     * @throws IOException
     */
    private static boolean isSecureLocation ( Path p, int symlinkLimit ) throws IOException {
        if ( symlinkLimit < 0 ) {
            log.warn("Symbolic link limit reached"); //$NON-NLS-1$
            return false;
        }

        Path absPath = p.toAbsolutePath();

        for ( int i = 0; i < absPath.getNameCount(); i++ ) {
            Path root = p.getRoot();
            if ( root == null ) {
                log.warn("No root file sytem found for " + p); //$NON-NLS-1$
                return false;
            }
            Path prefix = root.resolve(absPath.subpath(0, i + 1));

            if ( log.isTraceEnabled() ) {
                log.trace("Check prefix " + prefix); //$NON-NLS-1$
            }
            if ( !checkFileSystemPermissionSupport(prefix) ) {
                return false;
            }

            if ( isInsecure(symlinkLimit, prefix) ) {
                return false;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Found to be secure " + p); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * @param symlinkLimit
     * @param prefix
     * @throws IOException
     */
    private static boolean isInsecure ( int symlinkLimit, Path prefix ) throws IOException {

        if ( Files.isSymbolicLink(prefix) ) {
            Path sl = Files.readSymbolicLink(prefix);
            if ( !sl.isAbsolute() ) {
                sl = prefix.resolveSibling(sl);
            }

            if ( !isSecureLocation(sl, symlinkLimit - 1) ) {
                return true;
            }
        }
        else {
            if ( SECURE_WHITELIST.contains(prefix) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Is whitelisted and assumed secure: " + prefix); //$NON-NLS-1$
                }
                return false;
            }
            else if ( !hasSecurePermissions(prefix) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param prefix
     * @throws IOException
     */
    private static boolean checkFileSystemPermissionSupport ( Path prefix ) throws IOException {
        FileStore fs = Files.getFileStore(prefix);

        if ( !fs.supportsFileAttributeView(PosixFileAttributeView.class) ) {
            log.warn("Filesystem does not support POSIX attributes"); //$NON-NLS-1$
            return false;
        }

        if ( fs.supportsFileAttributeView(AclFileAttributeView.class) ) {
            log.warn("Filesystems with ACL support currently unimplemented"); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    private static boolean hasSecurePermissions ( Path p ) throws IOException {
        PosixFileAttributeView posixAttrsView = Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        PosixFileAttributes attrs = posixAttrsView.readAttributes();

        Set<PosixFilePermission> permissions = attrs.permissions();

        if ( isOtherWritable(p, permissions) ) {
            return false;
        }

        if ( isGroupWritable(p, permissions) ) {
            return false;
        }

        if ( !hasSafeOwner(p, attrs) ) {
            return false;
        }

        return true;
    }


    /**
     * @param p
     * @param attrs
     */
    private static boolean hasSafeOwner ( Path p, PosixFileAttributes attrs ) {
        if ( ! ( attrs.owner().equals(MY_PRINCIPAL) || attrs.owner().equals(ROOT_PRINCIPAL) ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Is not owned by us or uid=0: " + p); //$NON-NLS-1$
            }
            return false;
        }
        return true;
    }


    /**
     * @param p
     * @param permissions
     */
    private static boolean isGroupWritable ( Path p, Set<PosixFilePermission> permissions ) {
        if ( permissions.contains(PosixFilePermission.GROUP_WRITE) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Is group writeable: " + p); //$NON-NLS-1$
            }
            return true;
        }
        return false;
    }


    /**
     * @param p
     * @param permissions
     */
    private static boolean isOtherWritable ( Path p, Set<PosixFilePermission> permissions ) {
        if ( permissions.contains(PosixFilePermission.OTHERS_WRITE) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Is world writeable: " + p); //$NON-NLS-1$
            }
            return true;
        }
        return false;
    }


    /**
     * 
     * @param p
     *            a directory
     * @return whether the given path is secure, meaning that other user except root has write access to any directory
     *         in the hierarchy.
     * @throws IOException
     */
    public static boolean isSecureLocation ( Path p ) throws IOException {
        return isSecureLocation(p.toAbsolutePath(), 16);
    }


    /**
     * @param p
     * @return a path on this filesystem where files cannot be unlinked by other users
     * @throws IOException
     */
    public static Path findSecureTempPath ( Path p ) throws IOException {

        while ( !Files.exists(p, LinkOption.NOFOLLOW_LINKS) ) {
            return findSecureTempPath(p.getParent());
        }

        return findSecureTempPath(p, Files.getFileStore(p));
    }


    /**
     * @param p
     * @param fileStore
     * @return
     * @throws IOException
     */
    static Path findSecureTempPath ( Path p, FileStore fileStore ) throws IOException {

        if ( !fileStore.equals(Files.getFileStore(p)) ) {
            throw new IOException(String.format(
                "Target path is not in the given filesystem. (path: %s, fileStore: %s)", //$NON-NLS-1$
                p.toString(),
                fileStore));
        }

        if ( !Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) ) {
            return findSecureTempPath(p.getParent(), fileStore);
        }

        return findSecureTempPathInternal(p, fileStore);
    }


    /**
     * @param p
     * @param fileStore
     * @return
     * @throws IOException
     */
    private static Path findSecureTempPathInternal ( Path p, FileStore fileStore ) throws IOException {
        if ( isSecureLocation(p) && Files.isWritable(p) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Is a writeable secure location " + p); //$NON-NLS-1$
            }
            return p;
        }

        if ( p.getParent() != null && Files.getFileStore(p.getParent()).equals(fileStore) ) {
            return findSecureTempPath(p.getParent(), fileStore);
        }

        throw new IOException("No secure location exists on the filesystem " + fileStore); //$NON-NLS-1$
    }


    /**
     * @param p
     * @throws IOException
     */
    public static void assertSecureLocation ( Path p ) throws IOException {
        if ( !isSecureLocation(p) ) {
            throw new IOException("Is not a secure location " + p); //$NON-NLS-1$
        }
    }


    /**
     * @return whether we are running as root
     */
    public static boolean isRunningAsRoot () {
        return RUNNING_AS_ROOT;
    }


    /**
     * @param f
     * @param owner
     * @param group
     * @param perms
     * @throws IOException
     */
    public static void setPermissions ( Path f, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> perms ) throws IOException {
        PosixFileAttributeView attrView = Files.getFileAttributeView(f, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if ( owner != null ) {
            attrView.setOwner(owner);
        }
        if ( group != null ) {
            attrView.setGroup(group);
        }
        if ( perms != null ) {
            attrView.setPermissions(perms);
        }
    }


    /**
     * @param p
     * @param owner
     * @param group
     * @param perms
     * @return the created path
     * @throws IOException
     */
    public static Path createDirectories ( Path p, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> perms ) throws IOException {
        if ( Files.exists(p) ) {
            return p;
        }

        if ( !Files.exists(p.getParent()) ) {
            createDirectories(p.getParent(), owner, group, perms);
        }

        Files.createDirectory(p, PosixFilePermissions.asFileAttribute(getOwnerOnlyDirPermissions()));
        setPermissions(p, owner, group, perms);
        return p;
    }

}

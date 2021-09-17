/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public final class FileAttributeUtils {

    /**
     * 
     */
    private static final int XATTR_BUFFER_SIZE = 4096;

    private static final Logger log = Logger.getLogger(FileAttributeUtils.class);

    private static final Map<String, Class<? extends FileAttributeView>> VIEW_CLASSES = new HashMap<>();

    static {
        VIEW_CLASSES.put("basic", BasicFileAttributeView.class); //$NON-NLS-1$
        VIEW_CLASSES.put("owner", FileOwnerAttributeView.class); //$NON-NLS-1$
        VIEW_CLASSES.put("posix", PosixFileAttributeView.class); //$NON-NLS-1$
        VIEW_CLASSES.put("dos", DosFileAttributeView.class); //$NON-NLS-1$
        VIEW_CLASSES.put("acl", AclFileAttributeView.class); //$NON-NLS-1$
        VIEW_CLASSES.put("user", UserDefinedFileAttributeView.class); //$NON-NLS-1$

    }


    private FileAttributeUtils () {}


    /**
     * @param file
     * @return the set of supported file attribute views for the given file
     * @throws IOException
     */
    public static Set<Class<? extends FileAttributeView>> getSupportedFileAttributeViews ( Path file ) throws IOException {
        Set<Class<? extends FileAttributeView>> views = new HashSet<>();
        for ( Entry<String, Class<? extends FileAttributeView>> e : VIEW_CLASSES.entrySet() ) {
            if ( Files.getFileStore(file).supportsFileAttributeView(e.getValue()) ) {
                views.add(e.getValue());
            }
            else {
                if ( log.isTraceEnabled() ) {
                    log.trace("Unsupported " + e.getValue().getName()); //$NON-NLS-1$
                }
            }
        }
        return views;
    }


    /**
     * Copy filesystem attributes
     * 
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copyAttributes ( Path from, Path to ) throws IOException {
        copyAttributes(from, to, true);
    }


    /**
     * Copy filesystem attributes
     * 
     * As we cannot operate on file descriptors we cannot do this in a really safe way.
     * Only secure locations are allowed for from and to (this also means no group or
     * other writable files), this prevents malicious interference from other users.
     * Still, this code will cause a mess if attributes are changed during the copy
     * process.
     * 
     * @param from
     * @param to
     * @param safe
     *            if safe is true, an error will be raised if some attributes of the source are not supported by the
     *            target
     * @throws IOException
     */
    public static void copyAttributes ( Path from, Path to, boolean safe ) throws IOException {

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Copying attributes from %s to %s", from, to)); //$NON-NLS-1$
        }

        assertSecureLocations(from, to);

        Set<Class<? extends FileAttributeView>> fromSupported = getSupportedFileAttributeViews(from);
        Set<Class<? extends FileAttributeView>> toSupported = getSupportedFileAttributeViews(to);
        Collection<Class<? extends FileAttributeView>> commonSupported = CollectionUtils.intersection(fromSupported, toSupported);

        if ( safe ) {
            checkCompatability(fromSupported, toSupported);
        }

        UserPrincipal owner = getOwner(from, commonSupported);

        copyAttributeTypes(from, to, commonSupported);

        if ( owner != null ) {
            FileOwnerAttributeView ownerViewTo = Files.getFileAttributeView(to, FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            ownerViewTo.setOwner(owner);
        }

        if ( !commonSupported.isEmpty() ) {
            throw new IOException("Failed to copy file attributes, not all attributes were copied " + commonSupported); //$NON-NLS-1$
        }
    }


    /**
     * @param from
     * @param commonSupported
     * @return
     * @throws IOException
     */
    private static UserPrincipal getOwner ( Path from, Collection<Class<? extends FileAttributeView>> commonSupported ) throws IOException {
        UserPrincipal owner = null;
        if ( commonSupported.contains(FileOwnerAttributeView.class)
                && ! ( commonSupported.contains(PosixFileAttributeView.class) || commonSupported.contains(AclFileAttributeView.class) ) ) {
            FileOwnerAttributeView ownerViewFrom = Files.getFileAttributeView(from, FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            owner = ownerViewFrom.getOwner();
        }
        else if ( commonSupported.contains(PosixFileAttributeView.class) || commonSupported.contains(AclFileAttributeView.class) ) {
            commonSupported.remove(FileOwnerAttributeView.class);
        }
        return owner;
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void assertSecureLocations ( Path from, Path to ) throws IOException {
        if ( !FileSecurityUtils.isSecureLocation(from) ) {
            throw new IOException("Source file is not in a secure location"); //$NON-NLS-1$
        }

        if ( !FileSecurityUtils.isSecureLocation(to) ) {
            throw new IOException("Target file is not in a secure location"); //$NON-NLS-1$
        }
    }


    /**
     * @param from
     * @param to
     * @param commonSupported
     * @throws IOException
     */
    private static void copyAttributeTypes ( Path from, Path to, Collection<Class<? extends FileAttributeView>> commonSupported ) throws IOException {
        copyBasicAttributes(from, to);
        commonSupported.remove(BasicFileAttributeView.class);

        if ( commonSupported.contains(PosixFileAttributeView.class) ) {
            copyPosixAttributes(from, to);
            commonSupported.remove(PosixFileAttributeView.class);
        }

        if ( commonSupported.contains(UserDefinedFileAttributeView.class) ) {
            copyUserAttributes(from, to);
            commonSupported.remove(UserDefinedFileAttributeView.class);
        }

        if ( commonSupported.contains(AclFileAttributeView.class) ) {
            copyACLAttributes(from, to);
            commonSupported.remove(AclFileAttributeView.class);
        }

        if ( commonSupported.contains(DosFileAttributeView.class) ) {
            copyDOSAttributes(from, to);
            commonSupported.remove(DosFileAttributeView.class);
        }
    }


    /**
     * @param fromSupported
     * @param toSupported
     * @throws IOException
     */
    private static void checkCompatability ( Set<Class<? extends FileAttributeView>> fromSupported,
            Set<Class<? extends FileAttributeView>> toSupported ) throws IOException {
        for ( Class<? extends FileAttributeView> criticalView : VIEW_CLASSES.values() ) {
            if ( fromSupported.contains(criticalView) && !toSupported.contains(criticalView) ) {
                throw new IOException("Copying attributes will drop security critical attributes from " + criticalView.getName()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void copyDOSAttributes ( Path from, Path to ) throws IOException {
        DosFileAttributeView dosViewFrom = Files.getFileAttributeView(from, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        DosFileAttributeView dosViewTo = Files.getFileAttributeView(to, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

        DosFileAttributes fromAttrs = dosViewFrom.readAttributes();
        dosViewTo.setArchive(fromAttrs.isArchive());
        dosViewTo.setHidden(fromAttrs.isHidden());
        dosViewTo.setReadOnly(fromAttrs.isReadOnly());
        dosViewTo.setSystem(fromAttrs.isSystem());
        dosViewTo.setTimes(fromAttrs.lastModifiedTime(), fromAttrs.lastAccessTime(), fromAttrs.creationTime());
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void copyACLAttributes ( Path from, Path to ) throws IOException {
        AclFileAttributeView aclViewFrom = Files.getFileAttributeView(from, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        AclFileAttributeView aclViewTo = Files.getFileAttributeView(to, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        aclViewTo.setAcl(aclViewFrom.getAcl());
        aclViewTo.setOwner(aclViewFrom.getOwner());
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void copyUserAttributes ( Path from, Path to ) throws IOException {
        UserDefinedFileAttributeView userViewFrom = Files.getFileAttributeView(from, UserDefinedFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        UserDefinedFileAttributeView userViewTo = Files.getFileAttributeView(to, UserDefinedFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

        for ( String attr : userViewFrom.list() ) {
            long attrSize = userViewFrom.size(attr);
            long attrPos = 0;
            ByteBuffer buf = ByteBuffer.allocate(XATTR_BUFFER_SIZE);
            while ( attrPos < attrSize ) {
                attrPos = userViewFrom.read(attr, buf);
                userViewTo.write(attr, buf);
                buf.clear();
            }
        }
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void copyPosixAttributes ( Path from, Path to ) throws IOException {
        PosixFileAttributeView posixViewFrom = Files.getFileAttributeView(from, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        PosixFileAttributeView posixViewTo = Files.getFileAttributeView(to, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        PosixFileAttributes fromAttrs = posixViewFrom.readAttributes();
        posixViewTo.setPermissions(fromAttrs.permissions());
        posixViewTo.setGroup(fromAttrs.group());
        posixViewTo.setOwner(fromAttrs.owner());
    }


    /**
     * @param from
     * @param to
     * @throws IOException
     */
    private static void copyBasicAttributes ( Path from, Path to ) throws IOException {
        BasicFileAttributeView basicViewFrom = Files.getFileAttributeView(from, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributeView basicViewTo = Files.getFileAttributeView(to, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributes fromAttrs = basicViewFrom.readAttributes();
        basicViewTo.setTimes(fromAttrs.lastModifiedTime(), fromAttrs.lastAccessTime(), fromAttrs.creationTime());
    }
}

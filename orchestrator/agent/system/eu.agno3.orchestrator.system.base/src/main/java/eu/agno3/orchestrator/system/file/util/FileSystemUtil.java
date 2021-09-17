/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 31, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.file.FileStore;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class FileSystemUtil {

    private static final Logger log = Logger.getLogger(FileSystemUtil.class);

    private static final Class<?> UNIX_FILE_STORE;
    private static final MethodHandle GET_ROOT_PATH;
    private static final MethodHandle GET_MOUNT_ENTRY;
    private static final MethodHandle HAS_OPTION;
    private static final MethodHandle FSTYPE;


    static {
        Class<?> ufs = null;
        Class<?> ume = null;
        MethodHandle file = null;
        MethodHandle entry = null;
        MethodHandle hasOption = null;
        MethodHandle fstype = null;
        try {
            ufs = Class.forName("sun.nio.fs.UnixFileStore"); //$NON-NLS-1$
            ume = Class.forName("sun.nio.fs.UnixMountEntry"); //$NON-NLS-1$

            Method fileMethod = ufs.getDeclaredMethod("file"); //$NON-NLS-1$
            fileMethod.setAccessible(true);

            Method entryMethod = ufs.getDeclaredMethod("entry"); //$NON-NLS-1$
            entryMethod.setAccessible(true);

            Method hasOptionMethod = ume.getDeclaredMethod("hasOption", String.class); //$NON-NLS-1$
            hasOptionMethod.setAccessible(true);

            Method fstypeMethod = ume.getDeclaredMethod("fstype"); //$NON-NLS-1$
            fstypeMethod.setAccessible(true);

            file = MethodHandles.lookup().unreflect(fileMethod);
            entry = MethodHandles.lookup().unreflect(entryMethod);
            hasOption = MethodHandles.lookup().unreflect(hasOptionMethod);
            fstype = MethodHandles.lookup().unreflect(fstypeMethod);
        }
        catch (
            ClassNotFoundException |
            NoSuchMethodException |
            SecurityException |
            IllegalAccessException e ) {
            log.error("Incompatible JVM", e); //$NON-NLS-1$
        }

        UNIX_FILE_STORE = ufs;
        GET_ROOT_PATH = file;
        GET_MOUNT_ENTRY = entry;
        HAS_OPTION = hasOption;
        FSTYPE = fstype;
    }


    /**
     * 
     */
    private FileSystemUtil () {}


    /**
     * @param fs
     * @return the path to the filesystem root, null if unknown
     * @throws IOException
     */
    public static Path getRootPath ( FileStore fs ) throws IOException {
        if ( !UNIX_FILE_STORE.isAssignableFrom(fs.getClass()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not a UnixFileStore " + fs.name()); //$NON-NLS-1$
            }
            return null;
        }
        try {
            return (Path) GET_ROOT_PATH.invoke(fs);
        }
        catch ( IOException e ) {
            throw e;
        }
        catch ( Throwable e ) {
            throw new IOException("Error looking up filesystem root", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param fs
     * @return the filesystem type
     * @throws IOException
     */
    public static String getFsType ( FileStore fs ) throws IOException {
        if ( !UNIX_FILE_STORE.isAssignableFrom(fs.getClass()) ) {
            return null;
        }

        try {
            Object me = GET_MOUNT_ENTRY.invoke(fs);
            String fsType = (String) FSTYPE.invoke(me);

            if ( StringUtils.isBlank(fsType) ) {
                return null;
            }

            return fsType.trim();
        }
        catch ( IOException e ) {
            throw e;
        }
        catch ( Throwable e ) {
            throw new IOException("Error check getting fstype options", e); //$NON-NLS-1$
        }
    }


    /**
     * @param fs
     * @param option
     * @return whether the filesystem has the given option flag set
     * @throws IOException
     */
    public static boolean hasMountOption ( FileStore fs, String option ) throws IOException {
        if ( !UNIX_FILE_STORE.isAssignableFrom(fs.getClass()) ) {
            return false;
        }

        try {
            Object me = GET_MOUNT_ENTRY.invoke(fs);
            return (boolean) HAS_OPTION.invoke(me, option);
        }
        catch ( IOException e ) {
            throw e;
        }
        catch ( Throwable e ) {
            throw new IOException("Error check up filesystem options", e); //$NON-NLS-1$
        }
    }
}

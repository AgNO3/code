/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public final class FileTemporaryUtils {

    private static final Logger log = Logger.getLogger(FileTemporaryUtils.class);

    /**
     * 
     */
    private static final String TMP_SUFFIX = ".tmp"; //$NON-NLS-1$


    private FileTemporaryUtils () {}


    /**
     * Creates a temporary file related to a given file.
     * 
     * The target will be prefixed with the file's name and stored in the same filesystem.
     * 
     * @param file
     * @return a temporary file on the same filesystem as the given file
     * @throws IOException
     */
    public static Path createRelatedTemporaryFile ( Path file ) throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace("Creating related temporary file for " + file); //$NON-NLS-1$
        }
        return Files.createTempFile(
            FileSecurityUtils.findSecureTempPath(file),
            file.getFileName().toString(),
            FileTemporaryUtils.TMP_SUFFIX,
            new FileAttribute[] {
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions())
            });
    }


    /**
     * Creates a temporary file related to a given directory or file.
     * 
     * The target will be prefixed with the file's name and stored in the same filesystem.
     * 
     * @param path
     * @return a temporary directory on the same filesystem as the given file
     * @throws IOException
     */
    public static Path createRelatedTemporaryDirectory ( Path path ) throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace("Creating related temporary dir for " + path); //$NON-NLS-1$
        }
        return Files.createTempDirectory(FileSecurityUtils.findSecureTempPath(path.getParent()), path.getFileName().toString(), new FileAttribute[] {
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions())
        });
    }


    /**
     * Securely creates a temporary buffer in an insecure location.
     * 
     * The buffer file may not be referenced by path as an attacker might replace this
     * path with a target of his choice.
     * 
     * @param p
     * @param prefix
     * @param suffix
     * @return a file channel pointing to a newly opened temporary file, deleted on close
     * @throws IOException
     */
    public static FileChannel createSecureTempBuffer ( Path p, String prefix, String suffix ) throws IOException {
        Set<OpenOption> options = new HashSet<>();
        options.add(LinkOption.NOFOLLOW_LINKS);
        options.add(StandardOpenOption.CREATE_NEW);
        options.add(StandardOpenOption.DELETE_ON_CLOSE);
        Path tmpPath = p.resolve(prefix.concat(FileSecurityUtils.generateRandomTag()).concat(suffix));
        return FileChannel.open(tmpPath, options, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));
    }

}

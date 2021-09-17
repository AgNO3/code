/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.impl;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.AbstractFileBackupUnit;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class AbstractFileBackupUnitGenerator <T extends AbstractFileBackupUnit> implements BackupUnitGenerator<T> {

    private static final Logger log = Logger.getLogger(AbstractFileBackupUnitGenerator.class);


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param tempDir
     * @param p
     * @param root
     * @throws IOException
     */
    private void copyToTempDir ( T unit, Path tempDir, final Path root ) throws IOException {
        Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 10, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {

                if ( excluded(unit, dir) ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Excluded " + dir); //$NON-NLS-1$
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                if ( dir.equals(root) ) {
                    return FileVisitResult.CONTINUE;
                }

                Path relativize = root.relativize(dir);
                Path resolve = tempDir.resolve(relativize);
                if ( !Files.exists(resolve) ) {
                    Files.createDirectory(resolve, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
                }
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                if ( excluded(unit, file) ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Excluded " + file); //$NON-NLS-1$
                    }
                    return FileVisitResult.CONTINUE;
                }

                if ( !attrs.isRegularFile() ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Not a file " + file); //$NON-NLS-1$
                    }
                    return FileVisitResult.CONTINUE;
                }

                Path relativize = root.relativize(file);
                Path tgt = tempDir.resolve(relativize);
                if ( getLog().isTraceEnabled() ) {
                    getLog().trace(String.format("Adding file %s -> %s", file, tgt)); //$NON-NLS-1$
                }

                Files.copy(file, tgt, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                throw exc;
            }


            @Override
            public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
                return FileVisitResult.CONTINUE;
            }

        });
    }


    /**
     * {@inheritDoc}
     * 
     * @throws BackupException
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#backup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.nio.file.Path)
     */
    @Override
    public void backup ( ServiceStructuralObject service, T unit, Path tempDir ) throws BackupException {
        try {

            Path p = resolvePath(service, unit);
            if ( !Files.exists(p) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Does not exist " + p); //$NON-NLS-1$
                }
                return;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Creating backup of " + p); //$NON-NLS-1$
            }
            copyToTempDir(unit, tempDir, p);
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to create file backup", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#restore(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.util.zip.ZipFile, java.lang.String,
     *      java.util.SortedSet)
     */
    @Override
    public void restore ( ServiceStructuralObject service, T unit, ZipFile zf, String prefix, SortedSet<String> unitFiles ) throws BackupException {
        try {
            Path tgt = resolvePath(service, unit);
            if ( log.isDebugEnabled() ) {
                log.debug("Restoring into " + tgt); //$NON-NLS-1$
            }
            ensureDirectories(tgt, unit);
            for ( String file : unitFiles ) {
                restoreFile(unit, zf, tgt, prefix, file);
            }
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to restore file backup", e); //$NON-NLS-1$
        }
    }


    protected static boolean excluded ( AbstractFileBackupUnit unit, Path file ) {
        for ( String excludePattern : unit.getExcludePatterns() ) {
            if ( Pattern.matches(excludePattern, file.toString()) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param unit
     * @param zf
     * @param tgt
     * @param file
     * @param file2
     * @throws IOException
     */
    protected void restoreFile ( T unit, ZipFile zf, Path tgt, String prefix, String file ) throws IOException {
        ZipEntry entry = zf.getEntry(prefix + '/' + file);
        Path fPath = tgt.resolve(file);
        ensureDirectories(fPath, unit);
        Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(fPath);
        try {
            copyTo(zf, entry, tmpFile);
            setPermissions(tmpFile, unit, false);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Restoring %s to %s", file, fPath)); //$NON-NLS-1$
            }
            FileUtil.safeMove(tmpFile, fPath, true);
        }
        finally {
            Files.deleteIfExists(tmpFile);
        }
    }


    /**
     * @param zf
     * @param entry
     * @param tmpFile
     * @throws IOException
     */
    protected void copyTo ( ZipFile zf, ZipEntry entry, Path tmpFile ) throws IOException {
        try ( FileChannel fc = FileChannel.open(tmpFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
              OutputStream os = Channels.newOutputStream(fc) ) {
            IOUtils.copyLarge(zf.getInputStream(entry), os);
        }
    }


    /**
     * @param fPath
     * @param unit
     * @throws IOException
     */
    private void ensureDirectories ( Path fPath, T unit ) throws IOException {
        Path parent = fPath.getParent();
        if ( !Files.isDirectory(parent) ) {
            ensureDirectories(parent, unit);
            ensureDirectory(parent, unit);
        }
    }


    /**
     * @param tgt
     * @param unit
     */
    private void ensureDirectory ( Path tgt, T unit ) throws IOException {
        if ( Files.isDirectory(tgt) ) {
            setPermissions(tgt, unit, true);
            return;
        }
        if ( Files.exists(tgt) ) {
            throw new IOException("Is not a directory but exists " + tgt); //$NON-NLS-1$
        }
        setPermissions(Files.createDirectory(tgt, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions())), unit, true);
    }


    /**
     * @param tgt
     * @param unit
     * @throws IOException
     */
    protected void setPermissions ( Path tgt, T unit, boolean dir ) throws IOException {
        PosixFileAttributeView fa = Files.getFileAttributeView(tgt, PosixFileAttributeView.class);
        if ( unit.getOwner() != null ) {
            fa.setOwner(unit.getOwner());
        }

        if ( unit.getGroup() != null ) {
            fa.setGroup(unit.getGroup());
        }

        if ( dir ) {
            fa.setPermissions(unit.getDirPermissions() != null ? unit.getDirPermissions() : FileSecurityUtils.getOwnerOnlyDirPermissions());
        }
        else {
            fa.setPermissions(unit.getFilePermissions() != null ? unit.getFilePermissions() : FileSecurityUtils.getOwnerOnlyFilePermissions());
        }
    }


    /**
     * @param service
     * @param unit
     * @return
     */
    protected abstract Path resolvePath ( ServiceStructuralObject service, T unit ) throws BackupException;

}
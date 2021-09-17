/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.CleanupBackupUnit;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class CleanupBackupUnitGenerator implements BackupUnitGenerator<CleanupBackupUnit> {

    private static final Logger log = Logger.getLogger(CleanupBackupUnitGenerator.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<CleanupBackupUnit> getUnitType () {
        return CleanupBackupUnit.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#backup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.nio.file.Path)
     */
    @Override
    public void backup ( ServiceStructuralObject service, CleanupBackupUnit unit, Path tempDir ) throws BackupException {
        // do nothing
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#restore(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.util.zip.ZipFile, java.lang.String,
     *      java.util.SortedSet)
     */
    @Override
    public void restore ( ServiceStructuralObject service, CleanupBackupUnit unit, ZipFile data, String prefix, SortedSet<String> unitFiles )
            throws BackupException {
        boolean shared = unit.isShared();
        String relativePath = unit.getRelativePath();
        String alias = unit.getStorageAlias();
        Path overrideMountpoint = unit.getOverrideMountpoint();
        Path root = StorageFilesBackupUnitGenerator.resolveStoragePath(service, alias, shared, relativePath, overrideMountpoint);

        if ( !Files.exists(root) ) {
            return;
        }

        log.info("Cleaning path " + root); //$NON-NLS-1$

        try {
            Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), 5, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                    return FileVisitResult.CONTINUE;
                }


                @Override
                public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Remove file " + file); //$NON-NLS-1$
                    }
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }


                @Override
                public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
                    if ( root.equals(dir) ) {
                        return FileVisitResult.CONTINUE;
                    }

                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Remove directory " + dir); //$NON-NLS-1$
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }


                @Override
                public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                    throw exc;
                }

            });
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to clean directory " + root, e); //$NON-NLS-1$
        }
    }

}

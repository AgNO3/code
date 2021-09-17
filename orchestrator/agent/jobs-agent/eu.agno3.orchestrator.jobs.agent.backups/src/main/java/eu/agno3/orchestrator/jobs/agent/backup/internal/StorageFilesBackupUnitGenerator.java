/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.StorageFilesBackupUnit;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class StorageFilesBackupUnitGenerator extends AbstractFileBackupUnitGenerator<StorageFilesBackupUnit> {

    /**
     * 
     */
    private static final String STORAGE = "storage-"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(StorageFilesBackupUnitGenerator.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<StorageFilesBackupUnit> getUnitType () {
        return StorageFilesBackupUnit.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator#resolvePath(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.units.AbstractFileBackupUnit)
     */
    @Override
    protected Path resolvePath ( ServiceStructuralObject service, StorageFilesBackupUnit unit ) throws BackupException {

        boolean shared = unit.isShared();
        String relativePath = unit.getRelativePath();
        String alias = unit.getStorageAlias();
        Path overrideMountpoint = unit.getOverrideMountpoint();

        try {
            if ( overrideMountpoint == null ) {
                getStorageGroup(alias);
            }
        }
        catch ( IOException e ) {
            throw new BackupException("Storage not properly set up " + alias); //$NON-NLS-1$
        }

        Path root = resolveStoragePath(service, alias, shared, relativePath, overrideMountpoint);
        if ( log.isDebugEnabled() ) {
            log.debug("Resolved root path is " + root); //$NON-NLS-1$
        }

        return root;
    }


    /**
     * @param service
     * @param alias
     * @param shared
     * @param relativePath
     * @param overrideMountpoint
     * @return the resolved path
     * @throws BackupException
     */
    static Path resolveStoragePath ( ServiceStructuralObject service, String alias, boolean shared, String relativePath, Path overrideMountpoint )
            throws BackupException {
        Path mountpoint = overrideMountpoint != null ? overrideMountpoint : getMountpoint(alias);

        if ( !Files.exists(mountpoint) || !Files.isDirectory(mountpoint) ) {
            throw new BackupException("Storage is not available " + alias); //$NON-NLS-1$
        }

        Path root = mountpoint;
        if ( overrideMountpoint == null ) {
            if ( shared ) {
                root = root.resolve(service.getServiceType().toString());
            }
            else {
                root = root.resolve(service.getId().toString());
            }
        }

        if ( relativePath != null ) {
            root = root.resolve(relativePath);
        }
        return root;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator#setPermissions(java.nio.file.Path,
     *      eu.agno3.orchestrator.jobs.agent.backup.units.AbstractFileBackupUnit, boolean)
     */
    @Override
    protected void setPermissions ( Path tgt, StorageFilesBackupUnit unit, boolean dir ) throws IOException {

        if ( unit.getOverrideMountpoint() != null ) {
            super.setPermissions(tgt, unit, dir);
            return;
        }

        PosixFileAttributeView fa = Files.getFileAttributeView(tgt, PosixFileAttributeView.class);
        if ( unit.getOwner() != null ) {
            fa.setOwner(unit.getOwner());
        }

        fa.setGroup(getStorageGroup(unit.getStorageAlias()));

        if ( dir ) {
            if ( unit.isShared() ) {
                fa.setPermissions(FileSecurityUtils.getGroupWriteDirPermissions());
            }
            else {
                fa.setPermissions(FileSecurityUtils.getOwnerOnlyDirPermissions());
            }
        }
        else {
            if ( unit.isShared() ) {
                fa.setPermissions(FileSecurityUtils.getGroupWriteFilePermissions());
            }
            else {
                fa.setPermissions(FileSecurityUtils.getOwnerOnlyFilePermissions());
            }
        }
    }


    private static GroupPrincipal getStorageGroup ( String alias ) throws IOException {
        return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(STORAGE + alias);
    }


    /**
     * @param e
     * @return
     */
    private static Path getMountpoint ( String alias ) {
        return Paths.get("/storage/" + alias); //$NON-NLS-1$
    }

}

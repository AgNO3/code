/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.DerbyDatabaseBackupUnit;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.runtime.db.DatabaseException;
import eu.agno3.runtime.db.jmx.DatabaseManagementMXBean;
import eu.agno3.runtime.jmx.JMXClient;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class DerbyDatabaseBackupUnitGenerator implements BackupUnitGenerator<DerbyDatabaseBackupUnit> {

    private static final Logger log = Logger.getLogger(DerbyDatabaseBackupUnitGenerator.class);

    static final Set<String> EXCLUDE_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "dbex.lck", //$NON-NLS-1$
        "db.lck"))); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<DerbyDatabaseBackupUnit> getUnitType () {
        return DerbyDatabaseBackupUnit.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#backup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.nio.file.Path)
     */
    @Override
    public void backup ( ServiceStructuralObject service, DerbyDatabaseBackupUnit unit, Path tempDir ) throws BackupException {

        RuntimeServiceManager rsm;
        ServiceRuntimeStatus runtimeStatus;
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Backing up datasource " + unit.getDataSource()); //$NON-NLS-1$
            }
            rsm = unit.getRuntimeServiceManager();
            runtimeStatus = rsm.getRuntimeStatus(service.getId());

        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Failed to get service status", e); //$NON-NLS-1$
        }

        try {
            if ( runtimeStatus == ServiceRuntimeStatus.ACTIVE || runtimeStatus == ServiceRuntimeStatus.WARNING ) {
                backupOnline(service, rsm, unit, tempDir);
            }
            else if ( runtimeStatus == ServiceRuntimeStatus.DISABLED ) {
                backupInternal(service, rsm, unit, tempDir);
            }
            else {
                log.info(String.format("Service status is %s, disabling service for backup", runtimeStatus)); //$NON-NLS-1$
                rsm.disable(service.getId());
                backupInternal(service, rsm, unit, tempDir);
                rsm.enable(service.getId());
            }
        }
        catch (
            ServiceManagementException |
            IOException e ) {
            throw new BackupException("Failed to generate backup", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @param rsm
     * @param unit
     * @param tempDir
     * @throws ServiceManagementException
     * @throws BackupException
     */
    protected void backupOnline ( ServiceStructuralObject service, RuntimeServiceManager rsm, DerbyDatabaseBackupUnit unit, Path tempDir )
            throws BackupException {

        log.debug("Performing online backup"); //$NON-NLS-1$
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try ( JMXClient jmxConnection = rsm.getJMXConnection() ) {
            DatabaseManagementMXBean manageBean = JMX.newMBeanProxy(
                jmxConnection,
                new ObjectName("eu.agno3.runtime.db:type=DatabaseManagementBean"), //$NON-NLS-1$
                DatabaseManagementMXBean.class);

            log.debug("Locking database"); //$NON-NLS-1$
            manageBean.lockDataSource(unit.getDataSource(), 30000);
            try {
                backupInternal(service, rsm, unit, tempDir);
            }
            finally {
                log.debug("Unlocking database"); //$NON-NLS-1$
                manageBean.unlockDataSource(unit.getDataSource());
            }
        }
        catch (
            ServiceManagementException |
            UndeclaredThrowableException |
            MalformedObjectNameException |
            IOException |
            DatabaseException e ) {
            throw new BackupException("Online backup failed", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param service
     * @param rsm
     * @param unit
     * @param tempDir
     * @throws BackupException
     * @throws IOException
     */
    protected void backupInternal ( ServiceStructuralObject service, RuntimeServiceManager rsm, DerbyDatabaseBackupUnit unit, Path tempDir )
            throws BackupException, IOException {

        final Path dbPath = StorageFilesBackupUnitGenerator
                .resolveStoragePath(
                    service,
                    unit.getDatabaseStorageAlias(),
                    unit.isDatabaseStorageShared(),
                    unit.getDatabaseStoragePath(),
                    rsm.getOverrideStoragePath(unit.getDatabaseStorageAlias()))
                .resolve(unit.getDataSource());

        log.info("Backing up database from " + dbPath); //$NON-NLS-1$

        if ( !Files.exists(dbPath) ) {
            return;
        }

        Files.walkFileTree(dbPath, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                if ( dir.equals(dbPath) ) {
                    return FileVisitResult.CONTINUE;
                }

                Path relativize = dbPath.relativize(dir);
                Files.createDirectories(
                    tempDir.resolve(relativize),
                    PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                if ( EXCLUDE_FILES.contains(file.getFileName().toString()) ) {
                    return FileVisitResult.CONTINUE;
                }
                Path relativize = dbPath.relativize(file);
                Files.copy(file, tempDir.resolve(relativize), StandardCopyOption.REPLACE_EXISTING);
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#restore(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.util.zip.ZipFile, java.lang.String,
     *      java.util.SortedSet)
     */
    @Override
    public void restore ( ServiceStructuralObject service, DerbyDatabaseBackupUnit unit, ZipFile data, String prefix, SortedSet<String> unitFiles )
            throws BackupException {
        RuntimeServiceManager rsm;
        UserPrincipal serviceUser;
        GroupPrincipal serviceGroup;
        try {
            rsm = unit.getRuntimeServiceManager();
            UUID id = service.getId();
            ServiceRuntimeStatus status = rsm.getRuntimeStatus(id);
            if ( status != ServiceRuntimeStatus.DISABLED ) {
                throw new BackupException(String.format("Service %s needs to be disabled is %s", service, status)); //$NON-NLS-1$
            }

            serviceUser = rsm.getServicePrincipal();
            serviceGroup = rsm.getGroupPrincipal();
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Failed to lookup service", e); //$NON-NLS-1$
        }

        final Path dbPath = StorageFilesBackupUnitGenerator
                .resolveStoragePath(
                    service,
                    unit.getDatabaseStorageAlias(),
                    unit.isDatabaseStorageShared(),
                    unit.getDatabaseStoragePath(),
                    rsm.getOverrideStoragePath(unit.getDatabaseStorageAlias()))
                .resolve(unit.getDataSource());

        log.info("Restoring database to " + dbPath); //$NON-NLS-1$

        try {
            for ( String file : unitFiles ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Restoring " + file); //$NON-NLS-1$
                }
                Path tgt = dbPath.resolve(file);
                ensureDirectories(tgt, serviceUser, serviceGroup);
                copyTo(data, data.getEntry(prefix + '/' + file), tgt, serviceUser, serviceGroup);
            }
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to restore database data", e); //$NON-NLS-1$
        }
    }


    /**
     * @param zf
     * @param entry
     * @param tgt
     * @throws IOException
     */
    protected void copyTo ( ZipFile zf, ZipEntry entry, Path tgt, UserPrincipal u, GroupPrincipal g ) throws IOException {
        try ( FileChannel fc = FileChannel.open(
            tgt,
            EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));
              OutputStream os = Channels.newOutputStream(fc) ) {
            IOUtils.copyLarge(zf.getInputStream(entry), os);
        }
        setPermissions(tgt, u, g, false);
    }


    /**
     * @param fPath
     * @param unit
     * @throws IOException
     */
    private void ensureDirectories ( Path fPath, UserPrincipal user, GroupPrincipal group ) throws IOException {
        Path parent = fPath.getParent();
        if ( !Files.isDirectory(parent) ) {
            ensureDirectories(parent, user, group);
            ensureDirectory(parent, user, group);
        }
    }


    /**
     * @param tgt
     * @param unit
     */
    private void ensureDirectory ( Path tgt, UserPrincipal user, GroupPrincipal group ) throws IOException {
        if ( Files.isDirectory(tgt) ) {
            setPermissions(tgt, user, group, true);
            return;
        }
        if ( Files.exists(tgt) ) {
            throw new IOException("Is not a directory but exists " + tgt); //$NON-NLS-1$
        }
        setPermissions(
            Files.createDirectory(tgt, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions())),
            user,
            group,
            true);
    }


    /**
     * @param tgt
     * @param unit
     * @throws IOException
     */
    protected void setPermissions ( Path tgt, UserPrincipal user, GroupPrincipal group, boolean dir ) throws IOException {
        PosixFileAttributeView fa = Files.getFileAttributeView(tgt, PosixFileAttributeView.class);
        if ( user != null ) {
            fa.setOwner(user);
        }

        if ( group != null ) {
            fa.setGroup(group);
        }

        if ( dir ) {
            fa.setPermissions(FileSecurityUtils.getOwnerOnlyDirPermissions());
        }
        else {
            fa.setPermissions(FileSecurityUtils.getOwnerOnlyFilePermissions());
        }
    }
}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.BackupManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.orchestrator.system.backups.ServiceBackupInfo;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = CommandProvider.class )
public class BackupConsoleCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(BackupConsoleCommandProvider.class);
    static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormat.forStyle("MM"); //$NON-NLS-1$

    private ConfigRepository configRepository;
    private BackupGenerator backupGenerator;
    private BackupManager backupManager;


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository repo ) {
        this.configRepository = repo;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository repo ) {
        if ( this.configRepository == repo ) {
            this.configRepository = null;
        }
    }


    @Reference
    protected synchronized void setBackupGenerator ( BackupGenerator bg ) {
        this.backupGenerator = bg;
    }


    protected synchronized void unsetBackupGenerator ( BackupGenerator bg ) {
        if ( this.backupGenerator == bg ) {
            this.backupGenerator = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setBackupManager ( BackupManager bm ) {
        this.backupManager = bm;
    }


    protected synchronized void unsetBackupManager ( BackupManager bm ) {
        if ( this.backupManager == bm ) {
            this.backupManager = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @return the backupGenerator
     */
    synchronized BackupGenerator getBackupGenerator () {
        return this.backupGenerator;
    }


    /**
     * @return the backupManager
     */
    synchronized BackupManager getBackupManager () {
        return this.backupManager;
    }


    Set<ServiceStructuralObject> makeTargets ( String service ) throws ConfigRepositoryException {
        if ( !StringUtils.isBlank(service) ) {

            try {
                UUID id = UUID.fromString(service);
                for ( ServiceStructuralObject sos : this.configRepository.getServices() ) {
                    if ( id.equals(sos.getId()) ) {
                        return Collections.singleton(sos);
                    }
                }
            }
            catch (
                IllegalArgumentException |
                ConfigRepositoryException e ) {
                log.trace("Failed to get service by id " + service, e); //$NON-NLS-1$
            }

            ServiceStructuralObject svc = this.configRepository.getSingletonServiceByType(service);
            if ( svc == null ) {
                return Collections.EMPTY_SET;
            }
            return Collections.singleton(svc);
        }
        return new HashSet<>(this.configRepository.getServices());
    }

    /**
     * Create managed backup
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "backup", name = "backup", description = "Create managed backup" )
    public class BackupCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * {@inheritDoc}
         * 
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            try {
                BackupManager bm = getBackupManager();
                if ( bm == null ) {
                    this.session.getConsole().println("Backup manager unavailable"); //$NON-NLS-1$
                    return null;
                }
                UUID backupId = bm.makeBackup();
                this.session.getConsole().println("Created backup " + backupId); //$NON-NLS-1$
                return backupId;
            }
            catch ( BackupException e ) {
                getLog().warn("Failed to create backup", e); //$NON-NLS-1$
                this.session.getConsole().println("Failed to create backup: " + e.toString()); //$NON-NLS-1$
                return null;
            }

        }
    }

    /**
     * Restore managed backup
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "backup", name = "restore", description = "Restore managed backup" )
    public class RestoreCommand implements Action {

        @Argument ( index = 0, name = "id", required = true )
        String id = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * {@inheritDoc}
         * 
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            try {
                BackupManager bm = getBackupManager();
                if ( bm == null ) {
                    this.session.getConsole().println("Backup manager unavailable"); //$NON-NLS-1$
                    return null;
                }
                bm.restore(UUID.fromString(this.id));
                this.session.getConsole().println("Restored backup " + this.id); //$NON-NLS-1$
            }
            catch ( BackupException e ) {
                getLog().warn("Failed to restore backup", e); //$NON-NLS-1$
                this.session.getConsole().println("Failed to restore backup: " + e.toString()); //$NON-NLS-1$
            }
            return null;
        }
    }

    /**
     * List managed backups
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "backup", name = "list", description = "List managed backup" )
    public class ListCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @SuppressWarnings ( "resource" )
        @Override
        public Object execute () throws Exception {
            BackupManager bm = getBackupManager();
            PrintStream console = this.session.getConsole();
            if ( bm == null ) {
                console.println("Backup manager unavailable"); //$NON-NLS-1$
                return null;
            }

            Ansi out = Ansi.ansi();
            for ( BackupInfo backupInfo : bm.list() ) {
                String sizeInfo = StringUtils.EMPTY;

                out.bold().a(backupInfo.getTimestamp().toString(DISPLAY_FORMAT)).boldOff().a(' ');
                out.a(backupInfo.getId());

                if ( backupInfo.getTotalSize() != null ) {
                    out.a(' ').a(String.format("(%.2f MB)", backupInfo.getTotalSize() / 1024f / 1024)); //$NON-NLS-1$
                }
                out.newline();

                for ( ServiceBackupInfo serviceBackupInfo : backupInfo.getServices() ) {
                    if ( serviceBackupInfo.getSize() != null ) {
                        sizeInfo = String.format("(%.2f MB)", serviceBackupInfo.getSize() / 1024f / 1024); //$NON-NLS-1$
                    }
                    out.a(String.format(" * %s: %s %s", serviceBackupInfo.getServiceId(), serviceBackupInfo.getServiceType(), sizeInfo)); //$NON-NLS-1$
                    out.newline();
                }
            }
            console.print(out.toString());
            return null;
        }

    }

    /**
     * Create backup to file target
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "backup", name = "backupRaw", description = "Create backup" )
    public class BackupRawCommand implements Action {

        @Argument ( index = 0, name = "target", required = true )
        String target = null;

        @Argument ( index = 1, name = "service", required = false )
        String service = null;

        @Option ( aliases = "-f", name = "-force" )
        boolean force = false;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {

            EnumSet<StandardOpenOption> opts = EnumSet
                    .of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            if ( this.force ) {
                opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            long start = System.currentTimeMillis();
            Set<ServiceStructuralObject> targets;
            try {
                targets = makeTargets(this.service);
            }
            catch ( ConfigRepositoryException e ) {
                this.session.getConsole().println(String.format("Failed to find service: %s: %s", e.getClass().getName(), e.getMessage())); //$NON-NLS-1$
                return null;
            }

            Path tPath = Paths.get(this.target);

            if ( targets.isEmpty() ) {
                this.session.getConsole().println("Target not found " + this.target); //$NON-NLS-1$
                return null;
            }
            else if ( targets.size() > 1 && Files.exists(tPath) && !Files.isDirectory(tPath) ) {
                this.session.getConsole().println("For multiple backups, the target needs to be a directory"); //$NON-NLS-1$
                return null;
            }
            else if ( targets.size() > 1 && !Files.isDirectory(tPath) ) {
                try {
                    Files.createDirectories(tPath, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
                }
                catch ( IOException e ) {
                    this.session.getConsole().println("Failed to create target directory " + tPath); //$NON-NLS-1$
                    return null;
                }
            }

            for ( ServiceStructuralObject svc : targets ) {
                Path sPath;
                if ( targets.size() > 1 || Files.isDirectory(tPath) ) {
                    sPath = tPath.resolve(String.format("%s-%s.zip", svc.getServiceType(), svc.getId())); //$NON-NLS-1$
                }
                else {
                    sPath = tPath;
                }

                try ( FileChannel fc = FileChannel
                        .open(sPath, opts, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));
                      OutputStream os = Channels.newOutputStream(fc) ) {

                    getBackupGenerator().backup(svc, os);
                    this.session.getConsole().println(String.format("Created backup for %s (%s)", svc.getServiceType(), svc.getId())); //$NON-NLS-1$
                }
                catch (
                    IOException |
                    BackupException e ) {
                    getLog().error("Failed to create backup for " + svc, e); //$NON-NLS-1$
                    this.session.getConsole()
                            .println(String.format("Failed to create backup for %s: %s: %s", svc, e.getClass().getName(), e.getMessage())); //$NON-NLS-1$
                }

                try ( ZipFile zf = new ZipFile(sPath.toFile()) ) {}
                catch ( IOException e ) {
                    this.session.getConsole().println("Produced an invalid file " + e.getMessage()); //$NON-NLS-1$
                }
            }

            this.session.getConsole().println(String.format("Backup took %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Restore backup from file
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "backup", name = "restoreRaw", description = "Restore backup" )
    public class RestoreRawCommand implements Action {

        @Argument ( index = 0, name = "source", required = true )
        String source = null;

        @Argument ( index = 1, name = "service", required = false )
        String service = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            try {
                Path sPath = Paths.get(this.source);
                if ( !Files.exists(sPath) ) {
                    throw new IOException("Restore source does not exist " + sPath); //$NON-NLS-1$
                }

                Set<ServiceStructuralObject> makeSources = makeTargets(this.service);
                if ( makeSources.isEmpty() ) {
                    this.session.getConsole().println("Target service not found " + this.service); //$NON-NLS-1$
                    return null;
                }

                if ( Files.isRegularFile(sPath) && makeSources.size() > 1 ) {
                    this.session.getConsole().println("For multiple backups, the target needs to be a directory"); //$NON-NLS-1$
                    return null;
                }

                for ( ServiceStructuralObject svc : makeSources ) {
                    Path ssPath;
                    if ( makeSources.size() > 1 || Files.isDirectory(sPath) ) {
                        ssPath = sPath.resolve(String.format("%s-%s.zip", svc.getServiceType(), svc.getId())); //$NON-NLS-1$
                    }
                    else {
                        ssPath = sPath;
                    }
                    getBackupGenerator().restore(svc, ssPath);
                    this.session.getConsole().println(String.format("Restored backup for %s (%s)", svc.getServiceType(), svc.getId())); //$NON-NLS-1$
                }
            }
            catch (
                IOException |
                BackupException |
                ConfigRepositoryException e ) {
                getLog().error("Failed to restore backup", e); //$NON-NLS-1$
                this.session.getConsole().println("Failed to restore backup: " + e.getMessage()); //$NON-NLS-1$
            }
            return null;
        }
    }
}

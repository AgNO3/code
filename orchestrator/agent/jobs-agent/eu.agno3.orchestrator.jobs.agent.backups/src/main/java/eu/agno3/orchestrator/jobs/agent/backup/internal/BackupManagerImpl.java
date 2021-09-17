/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.BackupManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.orchestrator.system.backups.ServiceBackupInfo;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    BackupManager.class, SystemService.class
}, configurationPid = "backup", configurationPolicy = ConfigurationPolicy.REQUIRE )
@SystemServiceType ( BackupManager.class )
public class BackupManagerImpl implements BackupManager {

    private static final Logger log = Logger.getLogger(BackupManagerImpl.class);

    private static final String BACKUP_PROPERTIES = "backup.properties"; //$NON-NLS-1$

    private static final String SERVICE_TYPE_PREFIX = "service.type."; //$NON-NLS-1$
    private static final String SERVICE_ID_PREFIX = "service.id."; //$NON-NLS-1$
    private static final String TIMESTAMP_FIELD = "timestamp"; //$NON-NLS-1$
    private static final String ID_FIELD = "id"; //$NON-NLS-1$
    private static final String SERVICE_COUNT_FIELD = "serviceCount"; //$NON-NLS-1$
    private static final String DEFAULT_BACKUP_PATH = "/srv/backups"; //$NON-NLS-1$

    private static final String APP_VERSION_FIELD = "applicanceVersion"; //$NON-NLS-1$
    private static final String APP_BUILD_FIELD = "applicanceBuild"; //$NON-NLS-1$

    private BackupGenerator backupGenerator;
    private ConfigRepository configRepository;

    private Path backupPath;


    @Reference
    protected synchronized void setBackupGenerator ( BackupGenerator bg ) {
        this.backupGenerator = bg;
    }


    protected synchronized void unsetBackupGenerator ( BackupGenerator bg ) {
        if ( this.backupGenerator == bg ) {
            this.backupGenerator = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepository = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepository == cr ) {
            this.configRepository = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.backupPath = Paths.get(ConfigUtil.parseString(ctx.getProperties(), "backupPath", DEFAULT_BACKUP_PATH)); //$NON-NLS-1$

        if ( !Files.exists(this.backupPath) ) {
            try {
                Files.createDirectories(this.backupPath, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }
            catch ( IOException e ) {
                log.error("Failed to create backup directory", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public UUID makeBackup () throws BackupException {
        UUID backupId = UUID.randomUUID();
        Path base = this.backupPath.resolve(backupId.toString());
        try {
            base = Files.createDirectory(base, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            Collection<ServiceStructuralObject> services = this.configRepository.getServices();
            writeBackupProperties(base, backupId, services);
            doMakeServiceBackups(base, services);
            return backupId;
        }
        catch (
            IOException |
            ConfigRepositoryException e ) {
            throw new BackupException("Failed to create backup", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupManager#restore(java.util.UUID)
     */
    @Override
    public void restore ( UUID backupId ) throws BackupException {
        restore(backupId, Collections.EMPTY_MAP);
    }


    @Override
    public void restore ( UUID backupId, Map<UUID, UUID> remapService ) throws BackupException {
        Path base = this.backupPath.resolve(backupId.toString());
        Path propsFile = base.resolve(BACKUP_PROPERTIES);

        if ( !Files.exists(base) || !Files.exists(propsFile) ) {
            throw new BackupException("Backup does not exist " + backupId); //$NON-NLS-1$
        }

        try {
            Properties props = readBackupProperties(propsFile);
            UUID id = UUID.fromString(props.getProperty(ID_FIELD)); // $NON-NLS-1$
            if ( !id.equals(backupId) ) {
                throw new BackupException("ID mismatch, backup corrupted"); //$NON-NLS-1$
            }
            DateTime ts = DateTime.parse(props.getProperty(TIMESTAMP_FIELD));
            log.info("Restoring backup from " + ts); //$NON-NLS-1$

            for ( Entry<ServiceStructuralObject, Path> e : getServices(remapService, props, base).entrySet() ) {
                this.backupGenerator.restore(e.getKey(), e.getValue());
            }
        }
        catch (
            IOException |
            ConfigRepositoryException e ) {
            throw new BackupException("Failed to restore backup", e); //$NON-NLS-1$
        }
    }


    @Override
    public List<BackupInfo> list () throws BackupException {
        List<BackupInfo> res = new ArrayList<>();
        try {
            Files.list(this.backupPath).forEach( ( Path p ) -> {

                if ( !Files.isDirectory(p) ) {
                    return;
                }

                try {
                    BackupInfo bi = new BackupInfo();
                    Properties props = readBackupProperties(p.resolve(BACKUP_PROPERTIES));

                    bi.setId(UUID.fromString(props.getProperty(ID_FIELD)));
                    bi.setTimestamp(DateTime.parse(props.getProperty(TIMESTAMP_FIELD)));
                    bi.setApplianceVersion(props.getProperty(APP_VERSION_FIELD));
                    bi.setApplianceBuild(props.getProperty(APP_VERSION_FIELD));
                    long totalSize = 0;

                    for ( int i = 0; i < Integer.parseInt(props.getProperty(SERVICE_COUNT_FIELD)); i++ ) {
                        UUID sId = UUID.fromString(props.getProperty(SERVICE_ID_PREFIX + i));
                        String sType = props.getProperty(SERVICE_TYPE_PREFIX + i);
                        Path sp = p.resolve(String.format("%s-%s.zip", sType, sId)); //$NON-NLS-1$
                        ServiceBackupInfo sbi = new ServiceBackupInfo();
                        sbi.setServiceId(sId);
                        sbi.setServiceType(sType);
                        if ( Files.exists(sp) ) {
                            long size = Files.size(sp);
                            totalSize += size;
                            sbi.setSize(size);
                        }
                        bi.getServices().add(sbi);
                    }

                    bi.setTotalSize(totalSize);
                    res.add(bi);
                }
                catch ( Exception e ) {
                    getLog().warn("Failed to read backup at " + p, e); //$NON-NLS-1$
                }

            });
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to enumerate backups", e); //$NON-NLS-1$
        }

        // sort
        Collections.sort(res, new Comparator<BackupInfo>() {

            @Override
            public int compare ( BackupInfo o1, BackupInfo o2 ) {
                if ( o1.getTimestamp() == null && o2.getTimestamp() == null ) {
                    return 0;
                }
                else if ( o1.getTimestamp() == null ) {
                    return 1;
                }
                else if ( o2.getTimestamp() == null ) {
                    return -1;
                }
                return -1 * o1.getTimestamp().compareTo(o2.getTimestamp());
            }

        });
        return res;
    }


    @Override
    public void remove ( UUID backupId ) throws BackupException {
        Path base = this.backupPath.resolve(backupId.toString());
        Path propsFile = base.resolve(BACKUP_PROPERTIES);
        if ( !Files.exists(base) || !Files.exists(propsFile) ) {
            throw new BackupException("Backup does not exist " + backupId); //$NON-NLS-1$
        }

        try {
            FileUtil.deleteRecursive(base);
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to remove backup files", e); //$NON-NLS-1$
        }
    }


    /**
     * @param remapService
     * @param props
     * @param basePath
     * @return
     * @throws ConfigRepositoryException
     */
    Map<ServiceStructuralObject, Path> getServices ( Map<UUID, UUID> remapService, Properties props, Path basePath )
            throws ConfigRepositoryException {
        int nServices = Integer.parseInt(props.getProperty(SERVICE_COUNT_FIELD));
        Map<ServiceStructuralObject, Path> services = new LinkedHashMap<>();
        for ( int i = 0; i < nServices; i++ ) {
            UUID sId = UUID.fromString(props.getProperty(SERVICE_ID_PREFIX + i));
            String sType = props.getProperty(SERVICE_TYPE_PREFIX + i);
            Path p = basePath.resolve(String.format("%s-%s.zip", sType, sId)); //$NON-NLS-1$
            if ( remapService.containsKey(sId) ) {
                sId = remapService.get(sId);
            }

            services.put(this.configRepository.getService(new StructuralObjectReferenceImpl(sId, StructuralObjectType.SERVICE, sType)), p);
        }
        return services;
    }


    /**
     * @param propsFile
     * @return
     * @throws IOException
     */
    private static Properties readBackupProperties ( Path propsFile ) throws IOException {
        try ( FileInputStream fis = new FileInputStream(propsFile.toFile()) ) {
            Properties p = new Properties();
            p.load(fis);
            return p;
        }
    }


    /**
     * @param base
     * @param backupId
     * @param services
     * @throws IOException
     */
    private static void writeBackupProperties ( Path base, UUID backupId, Collection<ServiceStructuralObject> services ) throws IOException {
        Properties props = new Properties();
        props.setProperty(ID_FIELD, backupId.toString()); // $NON-NLS-1$
        props.setProperty(TIMESTAMP_FIELD, DateTime.now().toString()); // $NON-NLS-1$
        props.setProperty(SERVICE_COUNT_FIELD, String.valueOf(services.size())); // $NON-NLS-1$

        String ver = SystemImageUtil.getApplianceVersion();
        if ( !StringUtils.isBlank(ver) ) {
            props.setProperty(APP_VERSION_FIELD, ver);
        }

        String build = SystemImageUtil.getApplianceBuild();
        if ( !StringUtils.isBlank(build) ) {
            props.setProperty(APP_BUILD_FIELD, build);
        }

        int pos = 0;
        for ( ServiceStructuralObject sos : services ) {
            props.setProperty(SERVICE_ID_PREFIX + pos, sos.getId().toString()); // $NON-NLS-1$
            props.setProperty(SERVICE_TYPE_PREFIX + pos, sos.getServiceType()); // $NON-NLS-1$
            props.setProperty("service.state." + pos, sos.getState().toString()); //$NON-NLS-1$
            pos++;
        }

        try ( FileOutputStream fos = new FileOutputStream(base.resolve(BACKUP_PROPERTIES).toFile()) ) {
            props.store(fos, "Generated backup"); //$NON-NLS-1$
        }
    }


    /**
     * @param base
     * @param services
     * @throws BackupException
     * @throws IOException
     */
    void doMakeServiceBackups ( Path base, Collection<ServiceStructuralObject> services ) throws BackupException, IOException {
        for ( ServiceStructuralObject svc : services ) {
            Path sPath = base.resolve(String.format("%s-%s.zip", svc.getServiceType(), svc.getId())); //$NON-NLS-1$
            try ( FileChannel fc = FileChannel.open(
                sPath,
                EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));
                  OutputStream os = Channels.newOutputStream(fc) ) {

                this.backupGenerator.backup(svc, os);
            }
            try ( ZipFile zf = new ZipFile(sPath.toFile()) ) {}
            catch ( IOException e ) {
                throw new BackupException("Invalid ZIP file created", e); //$NON-NLS-1$
            }
        }
    }

}

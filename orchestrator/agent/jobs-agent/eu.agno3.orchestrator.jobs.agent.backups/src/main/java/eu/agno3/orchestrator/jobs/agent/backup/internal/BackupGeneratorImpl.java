/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    BackupGenerator.class, SystemService.class
} )
@SystemServiceType ( BackupGenerator.class )
public class BackupGeneratorImpl implements BackupGenerator {

    private static final Logger log = Logger.getLogger(BackupGeneratorImpl.class);

    private ServiceManager serviceManager;
    private ServiceTypeRegistry serviceTypeRegistry;
    private ConfigRepository configRepository;

    private Map<String, BackupUnitGenerator<?>> generators = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindBackupGenerator ( BackupUnitGenerator<?> bug ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding backup unit generator for " + bug.getUnitType().getName()); //$NON-NLS-1$
        }
        if ( this.generators.put(bug.getUnitType().getName(), bug) != null ) {
            log.warn("Duplicate registration for backup t ype " + bug.getUnitType().getName()); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindBackupGenerator ( BackupUnitGenerator<?> bug ) {
        this.generators.remove(bug.getUnitType().getName());
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setServiceTypeRegistry ( ServiceTypeRegistry str ) {
        this.serviceTypeRegistry = str;
    }


    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry str ) {
        if ( this.serviceTypeRegistry == str ) {
            this.serviceTypeRegistry = null;
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


    /**
     * @param u
     * @return
     * @throws BackupException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends BackupUnit> BackupUnitGenerator<T> getGeneratorFor ( T u ) throws BackupException {
        BackupUnitGenerator<T> bu = (BackupUnitGenerator<T>) this.generators.get(u.getClass().getName());
        if ( bu == null ) {
            throw new BackupException("Failed to locate generator for backup unit type " + u.getClass().getName()); //$NON-NLS-1$
        }
        return bu;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupGenerator#backup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.io.OutputStream)
     */
    @Override
    public void backup ( ServiceStructuralObject service, OutputStream os ) throws BackupException {
        try ( BufferedOutputStream bos = new BufferedOutputStream(os);
              ZipOutputStream zos = new ZipOutputStream(bos) ) {
            Map<BackupUnit, BackupUnitGenerator<BackupUnit>> gens = makeBackupUnits(service);
            SortedSet<String> fileIdx = new TreeSet<>();
            writeMetadata(service, zos);
            for ( Entry<BackupUnit, BackupUnitGenerator<BackupUnit>> entry : gens.entrySet() ) {
                String name = makeUnitName(entry.getKey());
                ZipEntry e = new ZipEntry(name);
                Path tempDir = Files.createTempDirectory("backup"); //$NON-NLS-1$
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Creating backup for %s in %s", entry.getKey(), tempDir)); //$NON-NLS-1$
                    }
                    entry.getValue().backup(service, entry.getKey(), tempDir);
                    recursiveAddFiles(zos, e, tempDir, fileIdx);
                }
                finally {
                    FileUtils.deleteDirectory(tempDir.toFile());
                }
            }
            writeFileIndex(zos, fileIdx);
            zos.finish();
            zos.close();
            os.flush();
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to create service backup", e); //$NON-NLS-1$
        }

    }


    /**
     * @param zos
     * @param fileIdx
     * @throws IOException
     */
    private static void writeFileIndex ( ZipOutputStream zos, SortedSet<String> fileIdx ) throws IOException {
        zos.putNextEntry(new ZipEntry(".idx")); //$NON-NLS-1$
        for ( String fname : fileIdx ) {
            zos.write(fname.getBytes(StandardCharsets.UTF_8));
            zos.write('\n');
        }
        zos.closeEntry();
    }


    /**
     * @param zos
     * @param e
     * @param tempDir
     * @throws IOException
     */
    private static void recursiveAddFiles ( ZipOutputStream zos, ZipEntry e, Path tempDir, Set<String> fileIndex ) throws IOException {
        Files.walkFileTree(tempDir, EnumSet.noneOf(FileVisitOption.class), 5, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                String fName = tempDir.relativize(file).toString();
                String fullname = e.getName() + "/" + fName; //$NON-NLS-1$

                if ( getLog().isTraceEnabled() ) {
                    getLog().trace("Adding file " + fullname); //$NON-NLS-1$
                }
                fileIndex.add(fullname);
                zos.putNextEntry(new ZipEntry(fullname));
                try ( FileChannel fc = FileChannel.open(file, StandardOpenOption.READ);
                      InputStream is = Channels.newInputStream(fc) ) {
                    IOUtils.copyLarge(is, zos);
                }
                zos.closeEntry();
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
     * @param service
     * @param zos
     * @throws IOException
     */
    private static void writeMetadata ( ServiceStructuralObject service, ZipOutputStream zos ) throws IOException {
        ZipEntry e = new ZipEntry("meta.properties"); //$NON-NLS-1$
        Properties metaProperties = new Properties();
        metaProperties.setProperty("serviceId", service.getId().toString()); //$NON-NLS-1$
        metaProperties.setProperty("serviceType", service.getServiceType()); //$NON-NLS-1$
        metaProperties.setProperty("appVersion", SystemImageUtil.getApplianceVersion()); //$NON-NLS-1$
        metaProperties.setProperty("appBuild", SystemImageUtil.getApplianceBuild()); //$NON-NLS-1$
        e.setComment("Backup metadata"); //$NON-NLS-1$
        zos.putNextEntry(e);
        metaProperties.store(zos, "Backup metadata"); //$NON-NLS-1$
        zos.closeEntry();
    }


    /**
     * @param key
     * @return
     */
    private static String makeUnitName ( BackupUnit key ) {
        return String.format("%s-%s", key.getClass().getSimpleName(), key.getId()); //$NON-NLS-1$
    }


    /**
     * @param service
     * @return
     * @throws BackupException
     */
    private Map<BackupUnit, BackupUnitGenerator<BackupUnit>> makeBackupUnits ( ServiceStructuralObject service ) throws BackupException {
        Map<BackupUnit, BackupUnitGenerator<BackupUnit>> gens = new LinkedHashMap<>();
        try {
            BaseServiceManager bsm = this.serviceManager
                    .getServiceManager(StructuralObjectReferenceImpl.fromObject(service), BaseServiceManager.class);
            Optional<@NonNull ConfigurationInstance> activeConfig = this.configRepository.getActiveConfiguration(service);

            if ( !activeConfig.isPresent() ) {
                log.debug("Service is not configured " + service); //$NON-NLS-1$
                return Collections.EMPTY_MAP;
            }

            List<BackupUnit> backupUnits = bsm.getBackupUnits(activeConfig.get());
            for ( BackupUnit u : backupUnits ) {
                gens.put(u, getGeneratorFor(u));
            }
        }
        catch (
            ServiceManagementException |
            ConfigRepositoryException e ) {
            throw new BackupException("Failed to get backup details", e); //$NON-NLS-1$
        }
        return gens;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupGenerator#restore(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.nio.file.Path)
     */
    @Override
    public void restore ( ServiceStructuralObject service, Path source ) throws BackupException {
        Map<BackupUnit, BackupUnitGenerator<BackupUnit>> gens = makeBackupUnits(service);
        if ( gens.isEmpty() ) {
            return;
        }
        BaseServiceManager bsm = null;
        ServiceRuntimeStatus originalStatus = null;
        try {
            StructuralObjectReference ref = StructuralObjectReferenceImpl.fromObject(service);
            if ( log.isDebugEnabled() ) {
                log.debug("Start restoring service, disabling service " + service); //$NON-NLS-1$
            }
            if ( !"urn:agno3:1.0:hostconfig".equals(ref.getLocalType()) ) { //$NON-NLS-1$
                bsm = this.serviceManager.getServiceManager(ref, BaseServiceManager.class);
                originalStatus = bsm.getRuntimeStatus(ref.getId());
                this.serviceManager.disableService(ref);
            }
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Failed to get service status", e); //$NON-NLS-1$
        }

        try ( ZipFile zf = new ZipFile(source.toFile()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Start restoring service %s, from %s ", service, source)); //$NON-NLS-1$
            }
            Properties metadata = loadMetadata(zf);
            checkMetadata(service, metadata);
            Map<String, SortedSet<String>> fileIndex = loadFileIndex(zf);

            log.debug("Done loading metadata"); //$NON-NLS-1$

            for ( Entry<BackupUnit, BackupUnitGenerator<BackupUnit>> entry : gens.entrySet() ) {
                String name = makeUnitName(entry.getKey());
                SortedSet<String> unitFiles = fileIndex.get(name);
                if ( unitFiles == null ) {
                    unitFiles = new TreeSet<>();
                }
                if ( log.isDebugEnabled() ) {
                    log.debug("Restore " + entry.getKey()); //$NON-NLS-1$
                }
                entry.getValue().restore(service, entry.getKey(), zf, name, unitFiles);
                if ( log.isDebugEnabled() ) {
                    log.debug("Restored " + entry.getKey()); //$NON-NLS-1$
                }
            }

            log.debug("Leave normally"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.warn("Failed to restore backup", e); //$NON-NLS-1$
            throw new BackupException("Failed to restore backup", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Failed to restore backup", e); //$NON-NLS-1$
            throw e;
        }
        finally {
            log.debug("Entering finally"); //$NON-NLS-1$
            if ( bsm != null && ( originalStatus == ServiceRuntimeStatus.ACTIVE || originalStatus == ServiceRuntimeStatus.ERROR ) ) {
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Completed restoring service, reenabling service " + service); //$NON-NLS-1$
                    }
                    bsm.enable(service.getId());
                }
                catch ( ServiceManagementException e ) {
                    throw new BackupException("Failed to reenable service", e); //$NON-NLS-1$
                }
            }
            log.debug("Leaving finally"); //$NON-NLS-1$
        }

    }


    /**
     * @param service
     * @throws BackupException
     */
    private static void checkMetadata ( ServiceStructuralObject service, Properties metadata ) throws BackupException {
        String serviceId = metadata.getProperty("serviceId"); //$NON-NLS-1$
        if ( StringUtils.isBlank(serviceId) ) {
            throw new BackupException("Missing serviceId"); //$NON-NLS-1$
        }
        String serviceType = metadata.getProperty("serviceType"); //$NON-NLS-1$
        if ( StringUtils.isBlank(serviceType) ) {
            throw new BackupException("Missing serviceType"); //$NON-NLS-1$
        }

        if ( !service.getServiceType().equals(serviceType) ) {
            throw new BackupException("Restoring to wrong service type"); //$NON-NLS-1$
        }

        if ( !UUID.fromString(serviceId).equals(service.getId()) ) {
            log.warn("Restore from other service"); //$NON-NLS-1$
        }
    }


    /**
     * @param zf
     * @return
     * @throws BackupException
     * @throws IOException
     */
    private static Map<String, SortedSet<String>> loadFileIndex ( ZipFile zf ) throws BackupException, IOException {
        ZipEntry idxEntry = zf.getEntry(".idx"); //$NON-NLS-1$
        if ( idxEntry == null ) {
            throw new BackupException("Not a valid back file, index missing"); //$NON-NLS-1$
        }

        Map<String, SortedSet<String>> fileIndex = new HashMap<>();
        try ( InputStream is = zf.getInputStream(idxEntry);
              InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
              BufferedReader br = new BufferedReader(r) ) {
            String line;
            while ( ( line = br.readLine() ) != null ) {
                int sep = line.indexOf('/');
                if ( sep <= 0 ) {
                    continue;
                }
                String unitName = line.substring(0, sep);

                SortedSet<String> perUnit = fileIndex.get(unitName);
                if ( perUnit == null ) {
                    perUnit = new TreeSet<>();
                    fileIndex.put(unitName, perUnit);
                }
                perUnit.add(line.substring(sep + 1));
            }
        }
        return fileIndex;
    }


    /**
     * @param zf
     * @return
     * @throws BackupException
     * @throws IOException
     */
    private static Properties loadMetadata ( ZipFile zf ) throws BackupException, IOException {
        ZipEntry metaEntry = zf.getEntry("meta.properties"); //$NON-NLS-1$

        if ( metaEntry == null ) {
            throw new BackupException("Not a valid backup file, metadata missing"); //$NON-NLS-1$
        }

        Properties metaProperties = new Properties();
        try ( InputStream is = zf.getInputStream(metaEntry) ) {
            metaProperties.load(is);
        }

        return metaProperties;
    }
}

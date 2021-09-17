/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.units.EnsureKeytabAccess;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.agent.api.RealmConfigUtil;
import eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil;
import eu.agno3.orchestrator.config.hostconfig.agent.api.StorageContext;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.orchestrator.config.hostconfig.storage.CIFSMountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.NFSMountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.mkdir.MkDir;
import eu.agno3.orchestrator.system.base.units.file.symlink.Symlink;
import eu.agno3.orchestrator.system.base.units.file.touch.Touch;
import eu.agno3.orchestrator.system.base.units.user.EnsureGroupExists;
import eu.agno3.orchestrator.system.base.units.user.EnsureMembership;
import eu.agno3.orchestrator.system.base.units.user.EnsureUserExists;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ServiceStorageUtil.class, StorageConfigJobBuilder.class
} )
public class StorageConfigJobBuilder implements ServiceStorageUtil {

    private static final String MOUNT = "/bin/mount"; //$NON-NLS-1$
    private static final String UMOUNT = "/bin/umount"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(StorageConfigJobBuilder.class);

    private static final String ETC_FSTAB = "/etc/fstab"; //$NON-NLS-1$

    private static final int CHANGE_ADD = 1;
    private static final int CHANGE_REMOVE = 2;
    private static final int CHANGED_MATCHER = 4;
    private static final int CHANGED_SETTINGS = 8;
    private static final int CHANGED_NET_SETTINGS = 16;

    private RealmConfigUtil realmConfigUtil;


    /**
     * 
     */
    public StorageConfigJobBuilder () {}


    @Reference
    protected synchronized void setRealmConfigJobBuilder ( RealmConfigUtil rcjb ) {
        this.realmConfigUtil = rcjb;
    }


    protected synchronized void unsetRealmConfigJobBuilder ( RealmConfigUtil rcjb ) {
        if ( this.realmConfigUtil == rcjb ) {
            this.realmConfigUtil = null;
        }
    }


    /**
     * @param b
     * @param ctx
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    public void configureStorage ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, IOException, JobBuilderException {

        log.debug("Configure storage...."); //$NON-NLS-1$
        // TODO: mounting and unmounting should be better checked
        // e.g. checking whether it's a valid mountpoint/already mounted and
        // whether there are processes still accessing the store (fuser)
        // this will be best implemented as a extra unit
        // fuser -m -s -M <mountpoint>
        Map<String, MountEntry> newEntryMap = makeEntryMap(ctx.cfg().getStorageConfiguration());
        Map<String, Integer> changes = getChanges(ctx, newEntryMap);
        boolean changedStorageSettings = !changes.isEmpty();

        for ( Entry<String, Integer> e : changes.entrySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Storage %s change %d", e.getKey(), e.getValue())); //$NON-NLS-1$
            }

            if ( ( e.getValue() & CHANGE_REMOVE ) != 0 ) {
                b.add(Exec.class).cmd(UMOUNT).args(getMountpoint(e.getKey()));

            }
            else if ( ( e.getValue() & CHANGED_SETTINGS ) != 0 ) {
                throw new JobBuilderException("Cannot change datastore alias, unimplemented"); //$NON-NLS-1$
            }
            else if ( ctx.job().getApplyInfo().isForce() || ( e.getValue() & ( CHANGED_NET_SETTINGS | CHANGE_ADD ) ) != 0 ) {
                specificConfig(b, ctx, e.getValue(), newEntryMap.get(e.getKey()));
            }

            if ( ( e.getValue() & ( CHANGE_ADD | CHANGE_REMOVE ) ) == 0
                    && ( ctx.job().getApplyInfo().isForce() || ( e.getValue() & CHANGED_NET_SETTINGS ) != 0 ) ) {
                b.add(Exec.class).cmd(MOUNT).args(
                    "-o", //$NON-NLS-1$
                    "remount", //$NON-NLS-1$
                    getMountpoint(e.getKey()));
            }
        }

        b.add(Contents.class).file(ETC_FSTAB).content(ctx.tpl(ETC_FSTAB)).perms(HostConfigJobBuilder.WORLD_READABLE_CONFIG)
                .runIf(changedStorageSettings || ctx.job().getApplyInfo().isForce());

        for ( Entry<String, Integer> e : changes.entrySet() ) {
            if ( ( e.getValue() & CHANGE_ADD ) != 0 ) {
                String mountpoint = getMountpoint(e.getKey());
                b.add(MkDir.class).file(mountpoint);
                b.add(Exec.class).cmd(MOUNT).args(mountpoint);
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil#ensureStorageAccess(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext,
     *      eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration, java.lang.String, java.nio.file.Path,
     *      java.lang.String)
     */
    @Override
    public StorageContext ensureStorageAccess ( JobBuilder b, ConfigurationJobContext<?, ?> ctx, StorageConfiguration sc, String alias,
            Path overridePath, String serviceUser ) throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException {

        if ( overridePath != null ) {
            return new StorageContext(null, overridePath, overridePath, overridePath);
        }

        MountEntry matched = null;
        for ( MountEntry mountEntry : sc.getMountEntries() ) {
            if ( !StringUtils.isBlank(mountEntry.getAlias()) && mountEntry.getAlias().equals(alias) ) {
                matched = mountEntry;
                break;
            }
        }

        if ( matched == null ) {
            throw new JobBuilderException("Failed to locate mount " + alias); //$NON-NLS-1$
        }

        String storageGroup = String.format("storage-%s", matched.getAlias()); //$NON-NLS-1$
        if ( !StringUtils.isBlank(serviceUser) ) {
            b.add(EnsureMembership.class).user(serviceUser).group(storageGroup);
        }

        if ( matched instanceof CIFSMountEntry ) {
            ensureCIFSAccess(b, ctx, (CIFSMountEntry) matched, serviceUser);
        }

        ServiceStructuralObject service = ctx.job().getService();
        String serviceType = service.getServiceType();
        UUID serviceId = service.getId();

        Path storageBase = Paths.get(getMountpoint(matched.getAlias()));
        Path serviceLocalStorage = storageBase.resolve(serviceId.toString());
        Path serviceSharedStorage = storageBase.resolve(serviceType.toString());

        setupServiceStorage(b, serviceUser, matched, storageGroup, storageBase, serviceLocalStorage, serviceSharedStorage);
        return new StorageContext(FileSecurityUtils.isRunningAsRoot() ? storageGroup : null, storageBase, serviceLocalStorage, serviceSharedStorage);
    }


    /**
     * @param b
     * @param serviceUser
     * @param matched
     * @param princ
     * @param storageBase
     * @param serviceLocalStorage
     * @param serviceSharedStorage
     * @throws UnitInitializationFailedException
     */
    private static void setupServiceStorage ( JobBuilder b, String serviceUser, MountEntry matched, String princ, Path storageBase,
            Path serviceLocalStorage, Path serviceSharedStorage ) throws UnitInitializationFailedException {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Service user is %s storage group %s for %s", //$NON-NLS-1$
                serviceUser,
                princ != null ? princ : "<null>", //$NON-NLS-1$
                storageBase));
        }

        // make sure that it is actually mounted
        b.add(Exec.class).cmd("/bin/mountpoint") //$NON-NLS-1$
                .args("-q", storageBase.toString()).expectExitCode(0); //$NON-NLS-1$

        b.add(MkDir.class).file(storageBase).groupLazy(princ).ownerLazy(princ).perms(FileSecurityUtils.getGroupWriteDirPermissions());

        if ( matched instanceof LocalMountEntry ) {
            b.add(MkDir.class).file(serviceLocalStorage).groupLazy(princ).ownerLazy(serviceUser != null ? serviceUser : "root") //$NON-NLS-1$
                    .perms(FileSecurityUtils.getOwnerOnlyDirPermissions());
            b.add(MkDir.class).file(serviceSharedStorage).groupLazy(princ).ownerLazy(princ).perms(FileSecurityUtils.getGroupWriteDirPermissions());
        }
        else if ( matched instanceof CIFSMountEntry ) {
            // create directories
            b.add(MkDir.class).file(serviceLocalStorage);
            b.add(MkDir.class).file(serviceSharedStorage);
        }
        else if ( matched instanceof NFSMountEntry ) {
            // check whether permissions are okay? can we?
            b.add(MkDir.class).file(serviceLocalStorage).ownerLazy(princ).groupLazy(princ).perms(FileSecurityUtils.getGroupWriteDirPermissions());
            b.add(MkDir.class).file(serviceSharedStorage).ownerLazy(princ).groupLazy(princ).perms(FileSecurityUtils.getGroupWriteDirPermissions());
        }

        b.add(Touch.class).ownerLazy(princ).groupLazy(princ).file(serviceLocalStorage.resolve(".keep")); //$NON-NLS-1$
        b.add(Touch.class).groupLazy(princ).groupLazy(princ).file(serviceSharedStorage.resolve(".keep")); //$NON-NLS-1$
    }


    /**
     * @param b
     * @param ctx
     * @param matched
     * @param serviceUser
     * @throws UnitInitializationFailedException
     */
    private static void ensureCIFSAccess ( JobBuilder b, ConfigurationJobContext<?, ?> ctx, CIFSMountEntry matched, String serviceUser )
            throws UnitInitializationFailedException {
        if ( matched.getAuthType() == CIFSAuthType.KERBEROS ) {
            b.add(EnsureKeytabAccess.class).realm(matched.getAuthRealm()).keytab(matched.getAuthKeytab()).user(serviceUser);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param key
     * @param change
     * @param mountEntry
     * @param realmConfigurator
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws IOException
     */
    private void specificConfig ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, int change,
            MountEntry mountEntry ) throws JobBuilderException, UnitInitializationFailedException, IOException {

        File configPath = new File(
            String.format("/etc/storage/%s/%s/", mountEntry.getMountType().name().toLowerCase(Locale.ROOT), mountEntry.getAlias())); //$NON-NLS-1$
        String princ = String.format("storage-%s", mountEntry.getAlias()); //$NON-NLS-1$

        b.add(EnsureGroupExists.class).system().group(princ);
        b.add(EnsureUserExists.class).system().user(princ).home(configPath).primaryGroup(princ);

        if ( mountEntry instanceof LocalMountEntry ) {
            configLocal(b, ctx, change, (LocalMountEntry) mountEntry, configPath, princ);
        }
        else if ( mountEntry instanceof NFSMountEntry ) {
            configNFS(b, ctx, change, (NFSMountEntry) mountEntry, configPath, princ);
        }
        else if ( mountEntry instanceof CIFSMountEntry ) {
            configCIFS(b, ctx, change, (CIFSMountEntry) mountEntry, configPath, princ);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param change
     * @param mountEntry
     * @param realmConfigurator
     * @throws IOException
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws KerberosException
     * @throws ExecutionException
     */
    private void configCIFS ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, int change,
            CIFSMountEntry mountEntry, File configPath, String princ ) throws IOException, JobBuilderException, UnitInitializationFailedException {

        // ensure storage group exists
        if ( mountEntry.getAuthType() == CIFSAuthType.GUEST ) {
            // nothing to do
            return;
        }
        else if ( mountEntry.getAuthType() == CIFSAuthType.KERBEROS ) {
            try {
                configureCIFSKerberos(b, ctx, mountEntry, princ, configPath);
            }
            catch (
                KerberosException |
                ADException e ) {
                throw new JobBuilderException("Failed to configure kerberos", e); //$NON-NLS-1$
            }
        }
        else {
            Map<String, Serializable> cifsCreds = new HashMap<>();
            cifsCreds.put("entry", mountEntry); //$NON-NLS-1$

            b.add(Contents.class).file(new File(configPath, "credentials")) //$NON-NLS-1$
                    .content(ctx.tpl("/storage/cifs-credentials", cifsCreds))//$NON-NLS-1$
                    .ownerLazy(princ).groupLazy(princ).perms(FileSecurityUtils.getGroupReadFilePermissions())
                    .createTargetDir(FileSecurityUtils.getGroupReadDirPermissions(), princ, princ);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param mountEntry
     * @param rc
     * @param princ
     * @param configPath
     * @throws JobBuilderException
     * @throws KerberosException
     * @throws ADException
     * @throws UnitInitializationFailedException
     */
    private void configureCIFSKerberos ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            CIFSMountEntry mountEntry, String princ, File configPath )
                    throws JobBuilderException, KerberosException, ADException, UnitInitializationFailedException {
        // symlink and ensure access
        String realmName = mountEntry.getAuthRealm();
        if ( StringUtils.isBlank(realmName) ) {
            throw new JobBuilderException("KERBEROS configured but no auth realm set"); //$NON-NLS-1$
        }

        Path keytabFile = this.realmConfigUtil.ensureKeytab(b, ctx, ctx.cfg().getRealmsConfiguration(), princ, realmName, mountEntry.getAuthKeytab());
        b.add(Symlink.class).file(new File(configPath, "keytab")).source(keytabFile); //$NON-NLS-1$
    }


    /**
     * @param b
     * @param ctx
     * @param change
     * @param mountEntry
     * @param realmConfigurator
     */
    private void configNFS ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, int change,
            NFSMountEntry mountEntry, File configPath, String princ ) {
        // unused as long as no proper auth is implemented
    }


    /**
     * @param b
     * @param ctx
     * @param change
     * @param mountEntry
     * @param princ
     * @param configPath
     * @throws JobBuilderException
     */
    private static void configLocal ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, int change,
            LocalMountEntry mountEntry, File configPath, String princ ) throws JobBuilderException {
        // should we check/intialize filesystem here?

        boolean found = false;
        try {
            for ( Drive d : ctx.storageInfo().getDrives() ) {
                for ( Volume volume : d.getVolumes() ) {
                    FileSystem fs = volume.getFileSystem();

                    if ( fs == null ) {
                        continue;
                    }

                    if ( mountEntry.getMatchLabel() != null && mountEntry.getMatchLabel().equals(fs.getLabel()) ) {
                        found = true;
                        break;
                    }

                    if ( mountEntry.getMatchUuid() != null && mountEntry.getMatchUuid().equals(fs.getUuid()) ) {
                        found = true;
                        break;
                    }
                }
            }
        }
        catch ( SystemInformationException e ) {
            throw new JobBuilderException("Failed to get drive information", e); //$NON-NLS-1$
        }

        if ( !found ) {
            throw new JobBuilderException("Failed to find actual device for " + mountEntry.getAlias()); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return
     */
    private static String getMountpoint ( String alias ) {
        return "/storage/" + alias; //$NON-NLS-1$
    }


    /**
     * @param ctx
     * @param newEntryMap
     * @throws JobBuilderException
     */
    private static Map<String, Integer> getChanges ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx,
            Map<String, MountEntry> newEntryMap ) throws JobBuilderException {

        Map<String, Integer> changes = new HashMap<>();

        Map<String, MountEntry> oldEntryMap;

        if ( ctx.cur().isPresent() ) {
            oldEntryMap = makeEntryMap(ctx.cur().get().getStorageConfiguration());
        }
        else {
            oldEntryMap = Collections.EMPTY_MAP;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("New entries " + newEntryMap); //$NON-NLS-1$
            log.debug("Old entries " + oldEntryMap); //$NON-NLS-1$
        }

        addNew(changes, newEntryMap, oldEntryMap);
        addRemoved(changes, newEntryMap, oldEntryMap);
        addChanged(ctx, changes, newEntryMap, oldEntryMap);
        return changes;
    }


    /**
     * @param ctx
     * @param changes
     * @param newEntryMap
     * @param oldEntryMap
     */
    private static void addChanged ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, Map<String, Integer> changes,
            Map<String, MountEntry> newEntryMap, Map<String, MountEntry> oldEntryMap ) {
        Set<String> commonEntries = new HashSet<>(newEntryMap.keySet());
        commonEntries.retainAll(oldEntryMap.keySet());

        for ( String alias : commonEntries ) {
            int changed = getEntryChanged(ctx, oldEntryMap.get(alias), newEntryMap.get(alias));
            if ( changed != 0 || ctx.job().getApplyInfo().isForce() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Changed storage " + alias); //$NON-NLS-1$
                }
                changes.put(alias, changed);
            }
        }
    }


    /**
     * @param changes
     * @param newEntryMap
     * @param oldEntryMap
     */
    private static void addRemoved ( Map<String, Integer> changes, Map<String, MountEntry> newEntryMap, Map<String, MountEntry> oldEntryMap ) {
        Set<String> removedEntries = new HashSet<>(oldEntryMap.keySet());
        removedEntries.removeAll(newEntryMap.keySet());
        for ( String alias : removedEntries ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Remove storage " + alias); //$NON-NLS-1$
            }
            changes.put(alias, CHANGE_REMOVE);
        }
    }


    /**
     * @param changes
     * @param newEntryMap
     * @param oldEntryMap
     */
    private static void addNew ( Map<String, Integer> changes, Map<String, MountEntry> newEntryMap, Map<String, MountEntry> oldEntryMap ) {
        Set<String> addedEntries = new HashSet<>(newEntryMap.keySet());
        addedEntries.removeAll(oldEntryMap.keySet());
        for ( String alias : addedEntries ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding storage " + alias); //$NON-NLS-1$
            }
            changes.put(alias, CHANGE_ADD);
        }
    }


    /**
     * @param ctx
     * @param alias
     * @param oldEntry
     * @param newEntry
     * @return
     */
    private static int getEntryChanged ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, MountEntry oldEntry,
            MountEntry newEntry ) {

        int changes = 0;
        if ( oldEntry instanceof LocalMountEntry && newEntry instanceof LocalMountEntry ) {
            LocalMountEntry lo = (LocalMountEntry) oldEntry;
            LocalMountEntry ln = (LocalMountEntry) newEntry;
            if ( !Objects.equals(lo.getMatchLabel(), ln.getMatchLabel()) || !Objects.equals(lo.getMatchUuid(), ln.getMatchUuid()) ) {
                changes |= CHANGED_MATCHER;
            }
        }
        else if ( oldEntry instanceof NFSMountEntry && newEntry instanceof NFSMountEntry ) {
            changes |= compareNFSMountEntry((NFSMountEntry) oldEntry, (NFSMountEntry) newEntry);
        }
        else if ( oldEntry instanceof CIFSMountEntry && newEntry instanceof CIFSMountEntry ) {
            changes |= compareCIFSMountEntry((CIFSMountEntry) oldEntry, (CIFSMountEntry) newEntry);
        }

        if ( !Objects.equals(oldEntry.getMountType(), newEntry.getMountType()) ) {
            changes |= CHANGED_SETTINGS;
        }

        return changes;
    }


    /**
     * @param oldEntry
     * @param newEntry
     * @return
     */
    private static int compareCIFSMountEntry ( CIFSMountEntry oldEntry, CIFSMountEntry newEntry ) {
        int changes = 0;
        if ( !Objects.equals(oldEntry.getUncPath(), newEntry.getUncPath()) ) {
            changes |= CHANGED_SETTINGS;
        }

        if ( !Objects.equals(oldEntry.getEnableSigning(), newEntry.getEnableSigning())
                || !Objects.equals(oldEntry.getAuthType(), newEntry.getAuthType())
                || !Objects.equals(oldEntry.getAllowSMB1(), newEntry.getAllowSMB1())
                || !Objects.equals(oldEntry.getDisableSMB2(), newEntry.getAllowSMB1()) ) {
            changes |= CHANGED_NET_SETTINGS;
        }

        if ( !Objects.equals(oldEntry.getAuthKeytab(), newEntry.getAuthKeytab())
                || !Objects.equals(oldEntry.getAuthRealm(), oldEntry.getAuthRealm()) ) {
            changes |= CHANGED_NET_SETTINGS;
        }

        if ( !Objects.equals(oldEntry.getDomain(), newEntry.getDomain()) || !Objects.equals(oldEntry.getUsername(), oldEntry.getUsername())
                || !Objects.equals(oldEntry.getPassword(), oldEntry.getPassword()) ) {
            changes |= CHANGED_NET_SETTINGS;
        }

        return changes;
    }


    /**
     * @param oldEntry
     * @param newEntry
     * @return
     */
    private static int compareNFSMountEntry ( NFSMountEntry oldEntry, NFSMountEntry newEntry ) {
        int changes = 0;
        if ( !Objects.equals(oldEntry.getTarget(), newEntry.getTarget()) ) {
            changes |= CHANGED_SETTINGS;
        }

        if ( !Objects.equals(oldEntry.getNfsVersion(), newEntry.getNfsVersion()) ) {
            changes |= CHANGED_SETTINGS;
        }

        if ( oldEntry.getSecurityType() != newEntry.getSecurityType() ) {
            changes |= CHANGED_SETTINGS;
        }

        if ( !Objects.equals(oldEntry.getAuthKeytab(), newEntry.getAuthKeytab())
                || !Objects.equals(oldEntry.getAuthRealm(), oldEntry.getAuthRealm()) ) {
            changes |= CHANGED_NET_SETTINGS;
        }

        return changes;
    }


    /**
     * @param storageConfiguration
     * @return
     * @throws JobBuilderException
     */
    private static Map<String, MountEntry> makeEntryMap ( StorageConfiguration storageConfiguration ) throws JobBuilderException {
        Map<String, MountEntry> entries = new HashMap<>();
        for ( MountEntry e : storageConfiguration.getMountEntries() ) {
            if ( entries.put(e.getAlias(), e) != null ) {
                throw new JobBuilderException("Duplicate datastore entry " + e.getAlias()); //$NON-NLS-1$
            }
        }
        return entries;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil#checkMigrationNeeded(java.lang.String,
     *      java.lang.String, java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public boolean checkMigrationNeeded ( String storageAlias, String oldAlias, Path overridePath, Path oldOverridePath ) {
        if ( !StringUtils.isBlank(oldAlias) ) {
            return !oldAlias.equals(storageAlias);
        }
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws InvalidUnitConfigurationException
     * @throws UnitInitializationFailedException
     *
     * @see eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil#migrateStorage(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext,
     *      eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration, java.lang.String, java.lang.String,
     *      java.nio.file.Path, java.nio.file.Path, java.lang.String)
     */
    @Override
    public StorageContext migrateStorage ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<?, ?> octx,
            StorageConfiguration storageConfiguration, String storageAlias, String oldAlias, Path overridePath, Path oldOverridePath,
            String userName ) throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException {

        log.warn(String.format("Configuration changed storage reference %s -> %s, this is currently unsupported", oldAlias, storageAlias)); //$NON-NLS-1$
        if ( !octx.job().getApplyInfo().isForce() ) {
            throw new JobBuilderException("Storage migration not yet supported, forced application will make the change but not migrate your data."); //$NON-NLS-1$
        }
        return ensureStorageAccess(b, octx, storageConfiguration, storageAlias, overridePath, userName);
    }
}

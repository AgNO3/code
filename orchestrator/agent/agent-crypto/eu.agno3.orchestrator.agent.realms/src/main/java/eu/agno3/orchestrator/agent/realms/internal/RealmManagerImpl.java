/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.acl.util.ACLGroupSyncUtil;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public class RealmManagerImpl implements RealmManager {

    /**
     * 
     */
    private static final String KEYTAB_SUFFIX = ".keytab"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(RealmManagerImpl.class);
    private static final String REALM_PROPERTY_FILE = "realm.properties"; //$NON-NLS-1$
    private static final String REALM_IDX_FILE = "realm.index"; //$NON-NLS-1$
    // private static final String KRB5_CONFIG_FILE = "krb5.conf"; //$NON-NLS-1$
    private static final String KEYTAB_DIR = "keytabs"; //$NON-NLS-1$

    private Path realmPath;
    private RealmType type;
    private RealmsManagerImpl realmsManagerImpl;
    private String realm;
    private ServiceManager serviceManager;


    /**
     * @param realm
     * @param type
     * @param realmPath
     * @param realmsManagerImpl
     * @param serviceManager
     */
    public RealmManagerImpl ( String realm, RealmType type, Path realmPath, RealmsManagerImpl realmsManagerImpl, ServiceManager serviceManager ) {
        this.realm = realm;
        this.type = type;
        this.realmPath = realmPath;
        this.realmsManagerImpl = realmsManagerImpl;
        this.serviceManager = serviceManager;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#getRealmName()
     */
    @Override
    public String getRealmName () {
        return this.realm;
    }


    /**
     * @return the realmsManagerImpl
     */
    protected RealmsManagerImpl getRealmsManager () {
        return this.realmsManagerImpl;
    }


    @Override
    public boolean exists () {
        return this.realmsManagerImpl.exists(this.realm);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#create(java.util.Properties, java.util.Set)
     */
    @Override
    public void create ( Properties properties, Set<UserPrincipal> defaultAllowedUsers ) throws KerberosException {
        if ( this.realmsManagerImpl.exists(this.realm) ) {
            throw new KerberosException("Realm does already exist " + this.realm); //$NON-NLS-1$
        }

        try {
            Files.createDirectories(this.realmPath, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));

            int idx = this.realmsManagerImpl.getNewIndex(this.type);
            Path realmIndex = this.realmPath.resolve(REALM_IDX_FILE);
            Files.write(realmIndex, String.valueOf(idx).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

            GroupPrincipal realmGroup = createRealmGroup();

            for ( UserPrincipal up : defaultAllowedUsers ) {
                try {
                    allowUser(up);
                }
                catch ( Exception e ) {
                    log.warn("Failed to allow user access for " + up, e); //$NON-NLS-1$
                }
            }

            setAccess(this.realmPath, realmGroup, FileSecurityUtils.getGroupReadDirPermissions());
            setAccess(realmIndex, realmGroup, FileSecurityUtils.getGroupReadFilePermissions());

            Path realmProperties = this.realmPath.resolve(REALM_PROPERTY_FILE);

            try ( FileChannel ch = FileChannel.open(
                realmProperties,
                EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getGroupReadFilePermissions()));
                  OutputStream fos = Channels.newOutputStream(ch) ) {
                properties.store(fos, StringUtils.EMPTY);
            }
            setAccess(realmProperties, realmGroup, FileSecurityUtils.getGroupReadFilePermissions());

            writeKRB5Config(properties, realmGroup);

            Path keytabsDir = this.realmPath.resolve(KEYTAB_DIR);
            if ( !Files.exists(keytabsDir) ) {
                Files.createDirectories(this.realmPath, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getGroupReadDirPermissions()));

                if ( FileSecurityUtils.isRunningAsRoot() ) {
                    PosixFileAttributeView attrs = Files
                            .getFileAttributeView(realmProperties, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
                    attrs.setGroup(realmGroup);
                    attrs.setPermissions(FileSecurityUtils.getGroupReadDirPermissions());
                }

            }
            syncACL();
        }
        catch ( IOException e ) {
            try {
                if ( Files.exists(this.realmPath) ) {
                    FileUtil.deleteRecursive(this.realmPath);
                }
            }
            catch ( IOException e1 ) {
                log.error("Failed to remove realm after failure", e1); //$NON-NLS-1$
            }
            throw new KerberosException("Failed to create realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#runMaintenance()
     */
    @Override
    public void runMaintenance () throws KerberosException {

    }


    /**
     * @param realmProperties
     * @param groupPrincipal
     * @throws IOException
     */
    private void writeKRB5Config ( Properties properties, GroupPrincipal realmGroup ) throws IOException {
        // File krb5Config = new File(this.realmPath, KRB5_CONFIG_FILE);
        // TODO: should we provide a kerberos config
        // setAccess(krb5Config, realmGroup);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#updateConfig(java.util.Properties, java.util.Set)
     */
    @Override
    public void updateConfig ( Properties properties, Set<UserPrincipal> defaultAllowUsers ) throws KerberosException {
        try {
            Path p = this.realmPath.resolve(REALM_PROPERTY_FILE);
            Path tempFile = FileTemporaryUtils.createRelatedTemporaryFile(p);

            try ( FileChannel ch = FileChannel.open(
                tempFile,
                EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getGroupReadFilePermissions()));
                  OutputStream fos = Channels.newOutputStream(ch) ) {
                properties.store(fos, StringUtils.EMPTY);
            }
            setAccess(tempFile, getRealmGroup(), FileSecurityUtils.getGroupReadFilePermissions());
            FileUtil.safeMove(tempFile, p, true);

            writeKRB5Config(properties, getRealmGroup());

            syncAllowedUsers(defaultAllowUsers);

            syncACL();
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to update config", e); //$NON-NLS-1$
        }
    }


    /**
     * @param defaultAllowUsers
     */
    protected void syncAllowedUsers ( Set<UserPrincipal> defaultAllowUsers ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Allow users " + defaultAllowUsers); //$NON-NLS-1$
        }

        Collection<String> listKeytabs = this.listKeytabs();
        for ( String keyTab : listKeytabs ) {
            try {
                KeyTabManager ktm = this.getKeytabManager(keyTab);
                Set<UserPrincipal> curAllowedUsers = ktm.getAllowedUsers();
                for ( UserPrincipal up : defaultAllowUsers ) {
                    if ( !curAllowedUsers.contains(up) ) {
                        log.debug(String.format("Adding principal %s to allowed users of keytab %s", up, keyTab)); //$NON-NLS-1$
                        ktm.allowUser(up);
                    }
                }
            }
            catch (
                KerberosException |
                UnixAccountException e ) {
                log.warn("Failed to sync default allowed users for keytab " + keyTab, e); //$NON-NLS-1$
            }
        }

        try {
            Set<UserPrincipal> curAllowedUsers = this.getAllowedUsers();
            for ( UserPrincipal up : defaultAllowUsers ) {
                if ( !curAllowedUsers.contains(up) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Adding principal to realm allowed users " + up); //$NON-NLS-1$
                    }
                    allowUser(up);
                }
            }
        }
        catch (
            UnixAccountException |
            KerberosException e ) {
            log.warn("Failed to sync realm allowed users", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#getConfig()
     */
    @Override
    public Properties getConfig () throws KerberosException {
        try {
            Path realmProperties = this.realmPath.resolve(REALM_PROPERTY_FILE);
            Properties props = new Properties();
            try ( FileChannel ch = FileChannel.open(realmProperties, EnumSet.of(StandardOpenOption.READ));
                  InputStream fis = Channels.newInputStream(ch) ) {
                props.load(fis);
            }
            return props;
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to update config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#delete()
     */
    @Override
    public void delete () throws KerberosException {
        try {
            FileUtil.deleteRecursive(this.realmPath);
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to remove realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#listKeytabs()
     */
    @Override
    public Collection<String> listKeytabs () {
        Path keytabsDir = this.realmPath.resolve(KEYTAB_DIR);
        try {
            if ( !Files.exists(keytabsDir) || !Files.isReadable(keytabsDir) || !Files.isDirectory(keytabsDir) ) {
                return Collections.EMPTY_LIST;
            }
            return Files.list(keytabsDir).filter(x -> {
                return Files.isRegularFile(x) && Files.isReadable(x) && x.getFileName().toString().endsWith(KEYTAB_SUFFIX);
            }).map(x -> {
                String name = x.getFileName().toString();
                return name.substring(0, name.length() - KEYTAB_SUFFIX.length());
            }).collect(Collectors.toList());
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate keytabs", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#createKeytab(java.lang.String)
     */
    @Override
    public KeyTabManager createKeytab ( String keyTabId ) throws KerberosException {
        GroupPrincipal group = createRealmInstanceGroup(keyTabId);
        try {
            KeyTabManagerImpl ktm = new KeyTabManagerImpl(
                keyTabId,
                getKeyTabPath(keyTabId),
                group,
                this,
                this.realmsManagerImpl.getKrbRealmManager(this.type));
            ktm.save();
            return ktm;
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to create keytab", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#getKeytabManager(java.lang.String)
     */
    @Override
    public KeyTabManager getKeytabManager ( String keyTabId ) throws KerberosException {
        try {
            return new KeyTabManagerImpl(
                keyTabId,
                getKeyTabPath(keyTabId),
                getRealmInstanceGroup(keyTabId),
                this,
                this.realmsManagerImpl.getKrbRealmManager(this.type));
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to read keytab", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws KerberosException
     * 
     */
    @Override
    public void notifyServices () throws KerberosException {
        this.serviceManager.forceReloadAll("krb@" + this.getRealm()); //$NON-NLS-1$
    }


    /**
     * @param krbRealm
     * @param keyTabId
     * @return
     * @throws KerberosException
     */
    private Path getKeyTabPath ( String keyTabId ) throws KerberosException {
        return this.realmPath.resolve(String.format("%s/%s.keytab", KEYTAB_DIR, keyTabId)); //$NON-NLS-1$
    }


    /**
     * @param f
     * @param perms
     * @throws IOException
     */
    private static void setAccess ( Path f, GroupPrincipal group, Set<PosixFilePermission> perms ) throws IOException {
        if ( !FileSecurityUtils.isRunningAsRoot() ) {
            return;
        }
        PosixFileAttributeView attrs = Files.getFileAttributeView(f, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        attrs.setGroup(group);
        attrs.setPermissions(perms);
    }


    /**
     * @return the users that are allowed to access this keystore
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KerberosException {
        GroupPrincipal realmGroup = getRealmGroup();
        if ( realmGroup != null ) {
            return UnixAccountUtil.getMembers(realmGroup);
        }

        return Collections.EMPTY_SET;
    }


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public void allowUser ( UserPrincipal user ) throws UnixAccountException, KerberosException {
        GroupPrincipal realmGroup = getRealmGroup();
        if ( realmGroup != null ) {
            UnixAccountUtil.addToGroup(realmGroup, user);
        }
        syncACL();
    }


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public void revokeUser ( UserPrincipal user ) throws UnixAccountException, KerberosException {
        GroupPrincipal realmGroup = getRealmGroup();
        if ( realmGroup != null ) {
            UnixAccountUtil.removeFromGroup(realmGroup, user);
        }
        syncACL();
    }


    protected void syncACL () {
        try {
            log.debug("Syncing ACL of realm files"); //$NON-NLS-1$
            ACLGroupSyncUtil.syncACLRecursive(this.realmPath);
        }
        catch ( IOException e ) {
            log.warn("Failed to sync ACL entries", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#getType()
     */
    @Override
    public RealmType getType () {
        return this.type;
    }


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmManager#getRealmPath()
     */
    @Override
    public Path getRealmPath () {
        return this.realmPath;
    }


    protected String getRealmInstanceGroupName ( String instance ) throws KerberosException {
        String baseName = getRealmGroupName();
        return String.format("%s-%s", baseName, StringUtils.abbreviate(instance, 31 - baseName.length())); //$NON-NLS-1$
    }


    protected GroupPrincipal getRealmInstanceGroup ( String instance ) throws KerberosException {
        if ( !FileSecurityUtils.isRunningAsRoot() ) {
            return null;
        }
        String realmInstanceGroupName = getRealmInstanceGroupName(instance);
        try {
            return UnixAccountUtil.resolveGroup(realmInstanceGroupName);
        }
        catch ( UnixAccountException e ) {
            throw new KerberosException("Failed to lookup realm instance group " + realmInstanceGroupName, e); //$NON-NLS-1$
        }
    }


    protected GroupPrincipal createRealmInstanceGroup ( String instance ) throws KerberosException {
        if ( !FileSecurityUtils.isRunningAsRoot() ) {
            return null;
        }
        String realmInstanceGroupName = getRealmInstanceGroupName(instance);
        try {
            return UnixAccountUtil.createGroup(realmInstanceGroupName);
        }
        catch ( UnixAccountException e ) {
            throw new KerberosException("Failed to create realm instance group " + realmInstanceGroupName, e); //$NON-NLS-1$
        }
    }


    protected String getRealmGroupName () throws KerberosException {
        Path idxFile = this.realmPath.resolve(REALM_IDX_FILE);

        try {
            long realmIndex = Long.parseLong(new String(Files.readAllBytes(idxFile), StandardCharsets.UTF_8));
            return String.format("%s-%d", getRealmGroupPrefix(), realmIndex); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to get realm index", e); //$NON-NLS-1$
        }
    }


    /**
     * Use different namespaces for ad and krb
     * 
     * @return
     */
    protected String getRealmGroupPrefix () {
        return "krb-rlm"; //$NON-NLS-1$
    }


    protected GroupPrincipal getRealmGroup () throws KerberosException {
        if ( !FileSecurityUtils.isRunningAsRoot() ) {
            return null;
        }
        String realmGroupName = getRealmGroupName();
        try {
            return UnixAccountUtil.resolveGroup(realmGroupName);
        }
        catch ( UnixAccountException e ) {
            throw new KerberosException("Failed to lookup realm instance group " + realmGroupName, e); //$NON-NLS-1$
        }
    }


    protected GroupPrincipal createRealmGroup () throws KerberosException {
        if ( !FileSecurityUtils.isRunningAsRoot() ) {
            return null;
        }
        String realmGroupName = getRealmGroupName();
        try {
            return UnixAccountUtil.createGroup(realmGroupName);
        }
        catch ( UnixAccountException e ) {
            throw new KerberosException("Failed to create realm group " + realmGroupName, e); //$NON-NLS-1$
        }
    }


    /**
     * @param pathname
     * @return whether this is a valid realm directory
     */
    public static boolean isRealm ( Path pathname ) {
        Path realmProps = pathname.resolve(REALM_PROPERTY_FILE);
        return Files.isDirectory(pathname) && Files.isReadable(pathname) && Files.exists(realmProps) && Files.isReadable(realmProps);
    }
}

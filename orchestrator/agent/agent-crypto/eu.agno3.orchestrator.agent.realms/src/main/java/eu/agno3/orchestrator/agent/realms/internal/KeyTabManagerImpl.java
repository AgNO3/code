/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.acl.util.ACLGroupSyncUtil;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KeyTab;
import eu.agno3.runtime.net.krb5.KeyTabEntry;
import eu.agno3.runtime.net.krb5.KrbRealmManager;


/**
 * @author mbechler
 *
 */
public class KeyTabManagerImpl implements KeyTabManager {

    private static final Logger log = Logger.getLogger(KeyTabManagerImpl.class);

    private Path keyTabFile;
    private KeyTab keyTab;
    private GroupPrincipal ownerGroup;
    private KrbRealmManager krm;
    private boolean exists;
    private RealmManager realmManager;

    private String alias;


    /**
     * @param alias
     * @param keyTabFile
     * @param ownerGroup
     * @param realmManager
     * @param krm
     * @throws IOException
     * 
     */
    public KeyTabManagerImpl ( String alias, Path keyTabFile, GroupPrincipal ownerGroup, RealmManager realmManager, KrbRealmManager krm )
            throws IOException {
        this.alias = alias;
        this.keyTabFile = keyTabFile;
        this.ownerGroup = ownerGroup;
        this.realmManager = realmManager;
        this.krm = krm;

        if ( Files.exists(this.keyTabFile) ) {
            try ( FileChannel fc = FileChannel.open(this.keyTabFile, StandardOpenOption.READ);
                  InputStream is = Channels.newInputStream(fc) ) {
                this.keyTab = KeyTab.parse(is);
                this.exists = true;
            }
        }
        else {
            this.keyTab = new KeyTab();
            this.exists = false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#getAlias()
     */
    @Override
    public String getAlias () {
        return this.alias;
    }


    /**
     * @return the exists
     */
    @Override
    public boolean exists () {
        return this.exists;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#getPath()
     */
    @Override
    public Path getPath () {
        return this.keyTabFile;
    }


    /**
     * @return the users that are allowed to access this keystore
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KerberosException {
        if ( this.ownerGroup == null ) {
            return Collections.EMPTY_SET;
        }
        return UnixAccountUtil.getMembers(this.ownerGroup);

    }


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public void allowUser ( UserPrincipal user ) throws UnixAccountException, KerberosException {
        if ( this.ownerGroup != null ) {
            UnixAccountUtil.addToGroup(this.ownerGroup, user);
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
        if ( this.ownerGroup != null ) {
            UnixAccountUtil.removeFromGroup(this.ownerGroup, user);
        }
        syncACL();
    }


    private void syncACL () {
        try {
            ACLGroupSyncUtil.syncACLRecursive(this.keyTabFile);
        }
        catch ( IOException e ) {
            log.warn("Failed to sync ACL entries", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#addKey(javax.security.auth.kerberos.KerberosKey)
     */
    @Override
    public void addKey ( KerberosKey key ) {
        KeyTabEntry e = new KeyTabEntry(key);
        if ( !this.keyTab.getEntries().contains(e) ) {
            this.keyTab.getEntries().add(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#addKeys(java.util.Collection)
     */
    @Override
    public void addKeys ( Collection<KerberosKey> keys ) {
        for ( KerberosKey key : keys ) {
            this.addKey(key);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#addKeyTab(eu.agno3.runtime.net.krb5.KeyTab)
     */
    @Override
    public void addKeyTab ( KeyTab kt ) {
        this.keyTab.getEntries().addAll(kt.getEntries());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#addPasswordKey(javax.security.auth.kerberos.KerberosPrincipal,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void addPasswordKey ( KerberosPrincipal princ, String password, String algo ) {
        this.addKey(new KerberosKey(princ, password.toCharArray(), algo));
    }


    /**
     * @param princ
     * @param password
     */
    public void addPasswordKey ( KerberosPrincipal princ, String password ) {
        Collection<Integer> etypes = this.krm.getPermittedETypeAlgos();
        Set<String> algos = new HashSet<>();
        for ( int etype : etypes ) {
            algos.add(ETypesUtil.getAlgoFromEtype(etype));
        }
        for ( String algo : algos ) {
            addPasswordKey(princ, password, algo);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#removeKey(javax.security.auth.kerberos.KerberosPrincipal,
     *      long, int)
     */
    @Override
    public void removeKey ( KerberosPrincipal princ, long kvno, int keyType ) {
        Iterator<KeyTabEntry> iterator = this.keyTab.getEntries().iterator();
        while ( iterator.hasNext() ) {
            KeyTabEntry e = iterator.next();
            if ( e.getPrincipal().equals(princ) && e.getKvno() == kvno && e.getKeyblockType() == keyType ) {
                iterator.remove();
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#removeKeys(javax.security.auth.kerberos.KerberosPrincipal,
     *      long)
     */
    @Override
    public void removeKeys ( KerberosPrincipal princ, long kvno ) {
        Iterator<KeyTabEntry> iterator = this.keyTab.getEntries().iterator();
        while ( iterator.hasNext() ) {
            KeyTabEntry e = iterator.next();
            if ( e.getPrincipal().equals(princ) && e.getKvno() == kvno ) {
                iterator.remove();
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#removeKeys(javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public void removeKeys ( KerberosPrincipal princ ) {
        Iterator<KeyTabEntry> iterator = this.keyTab.getEntries().iterator();
        while ( iterator.hasNext() ) {
            KeyTabEntry e = iterator.next();
            if ( e.getPrincipal().equals(princ) ) {
                iterator.remove();
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#listKeys()
     */
    @Override
    public Collection<KerberosKey> listKeys () {
        List<KerberosKey> keys = new LinkedList<>();
        if ( this.keyTab == null || this.keyTab.getEntries() == null ) {
            return keys;
        }
        for ( KeyTabEntry e : this.keyTab.getEntries() ) {
            keys.add(e.getKey());
        }
        return keys;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#delete()
     */
    @Override
    public void delete () throws IOException {
        this.keyTab.getEntries().clear();
        Files.deleteIfExists(this.keyTabFile);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyTabManager#save()
     */
    @Override
    public void save () throws IOException {
        Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(this.keyTabFile);

        try {

            Path ktDir = this.keyTabFile.getParent();
            if ( !Files.exists(ktDir) ) {
                ktDir = Files.createDirectories(ktDir, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }

            if ( this.ownerGroup != null ) {
                PosixFileAttributeView attrs = Files.getFileAttributeView(tmpFile, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
                attrs.setGroup(this.ownerGroup);
                attrs.setPermissions(FileSecurityUtils.getGroupReadFilePermissions());

                attrs = Files.getFileAttributeView(ktDir, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
                attrs.setGroup(this.ownerGroup);
                attrs.setPermissions(FileSecurityUtils.getGroupReadDirPermissions());
            }

            try ( FileChannel ch = FileChannel.open(tmpFile, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
                  OutputStream fos = Channels.newOutputStream(ch) ) {
                this.keyTab.write(fos);
            }

            FileUtil.safeMove(tmpFile, this.keyTabFile, true);
            this.exists = true;

            try {
                ACLGroupSyncUtil.syncACL(this.keyTabFile);
                this.realmManager.notifyServices();
            }
            catch ( KerberosException e ) {
                log.error("Failed to notify services of keytab changes", e); //$NON-NLS-1$
            }
        }
        finally {
            Files.deleteIfExists(tmpFile);
        }
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

import eu.agno3.orchestrator.system.cfgfiles.ConfigManager;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 *
 */
public class ConfigManagerImpl implements ConfigManager {

    private String instanceId;
    private Path instanceFile;
    private String factoryId;
    private UserPrincipal owner;
    private GroupPrincipal group;
    private Set<PosixFilePermission> filePerms;
    private Set<PosixFilePermission> dirPerms;


    /**
     * @param factoryId
     * @param instanceId
     * @param instanceFile
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     */
    public ConfigManagerImpl ( String factoryId, String instanceId, File instanceFile, UserPrincipal owner, GroupPrincipal group,
            Set<PosixFilePermission> filePerms, Set<PosixFilePermission> dirPerms ) {
        this.factoryId = factoryId;
        this.instanceId = instanceId;
        this.owner = owner;
        this.group = group;
        this.filePerms = filePerms;
        this.dirPerms = dirPerms;
        this.instanceFile = instanceFile.toPath();
    }


    /**
     * @param pid
     * @param instanceFile
     * @param dirPerms
     * @param filePerms
     * @param group
     * @param owner
     */
    public ConfigManagerImpl ( String pid, File instanceFile, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        this.instanceId = pid;
        this.owner = owner;
        this.group = group;
        this.filePerms = filePerms;
        this.dirPerms = dirPerms;
        this.instanceFile = instanceFile.toPath();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#getPath()
     */
    @Override
    public Path getPath () {
        return this.instanceFile;
    }


    /**
     * 
     * @return whether this is a factory instance
     */
    @Override
    public boolean isFactoryInstance () {
        return this.factoryId != null;
    }


    /**
     * @return the factoryId
     */
    @Override
    public String getFactoryId () {
        return this.factoryId;
    }


    /**
     * @return the instanceId
     */
    @Override
    public String getInstanceId () {
        return this.instanceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#exists()
     */
    @Override
    public boolean exists () {
        return Files.exists(this.instanceFile);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#remove()
     */
    @Override
    public void remove () throws IOException {
        Files.deleteIfExists(this.instanceFile);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#read()
     */
    @Override
    public Properties read () throws IOException {

        if ( !exists() ) {
            return new Properties();
        }

        Properties props = new Properties();
        try ( FileChannel channel = FileChannel.open(this.instanceFile);
              InputStream is = Channels.newInputStream(channel) ) {
            props.load(is);
        }
        return props;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#write(java.util.Properties)
     */
    @Override
    public void write ( Properties data ) throws IOException {
        Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(this.instanceFile);
        try {
            try ( FileChannel ch = FileChannel.open(tmpFile, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                  OutputStream os = Channels.newOutputStream(ch) ) {
                data.store(os, "written by ConfigManager"); //$NON-NLS-1$
            }

            this.replace(tmpFile);
        }
        finally {
            Files.deleteIfExists(tmpFile);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigManager#replace(java.nio.file.Path)
     */
    @Override
    public void replace ( Path f ) throws IOException {
        PosixFileAttributeView attrView = Files.getFileAttributeView(f, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if ( this.owner != null ) {
            attrView.setOwner(this.owner);
        }
        if ( this.group != null ) {
            attrView.setGroup(this.group);
        }
        if ( this.filePerms != null ) {
            attrView.setPermissions(this.filePerms);
        }

        ensureParents(this.instanceFile, attrView);
        FileUtil.safeMove(f, this.instanceFile, true);
    }


    /**
     * @param f
     * @param attrView
     * @throws IOException
     */
    private void ensureParents ( Path f, PosixFileAttributeView attrView ) throws IOException {
        if ( !Files.exists(f.getParent()) ) {
            Files.createDirectories(f.getParent(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            if ( this.owner != null ) {
                attrView.setOwner(this.owner);
            }
            if ( this.group != null ) {
                attrView.setGroup(this.group);
            }
            if ( this.dirPerms != null ) {
                attrView.setPermissions(this.dirPerms);
            }
        }
    }
}

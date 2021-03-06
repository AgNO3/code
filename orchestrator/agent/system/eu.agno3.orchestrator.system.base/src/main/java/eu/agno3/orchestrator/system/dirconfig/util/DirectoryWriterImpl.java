/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dirconfig.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Set;

import eu.agno3.orchestrator.system.file.util.FileHashUtil;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 *
 */
public class DirectoryWriterImpl implements DirectoryWriter {

    /**
     * 
     */
    private static final String CONF_SUFFIX = ".conf"; //$NON-NLS-1$
    private static final String GENERATED_COMMENT = "Generated by DirectoryWriter"; //$NON-NLS-1$
    private static final Charset CONFCHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$
    private Path confBase;
    private UserPrincipal user;
    private GroupPrincipal group;
    private boolean groupWrite;
    private boolean groupRead;


    /**
     * @param confBase
     * @param user
     * @param group
     * @param groupRead
     * @param groupWrite
     * 
     */
    public DirectoryWriterImpl ( Path confBase, UserPrincipal user, GroupPrincipal group, boolean groupRead, boolean groupWrite ) {
        this.confBase = confBase;
        this.user = user;
        this.group = group;
        this.groupRead = groupRead;
        this.groupWrite = groupWrite;
    }


    private Path getConfigFile ( String pid ) {
        return new File(this.confBase.toFile(), pid.concat(CONF_SUFFIX)).toPath();
    }


    private Path getConfigFile ( String pid, String instance ) {
        return new File(new File(this.confBase.toFile(), pid), instance.concat(CONF_SUFFIX)).toPath();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#readConfig(java.lang.String)
     */
    @Override
    public Properties readConfig ( String pid ) throws IOException {
        return readProperties(this.getConfigFile(pid));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#readConfig(java.lang.String, java.lang.String)
     */
    @Override
    public Properties readConfig ( String pid, String instance ) throws IOException {
        return readProperties(this.getConfigFile(pid));
    }


    private static Properties readProperties ( Path cfg ) throws IOException {
        Properties props = new Properties();
        try ( InputStream is = Files.newInputStream(cfg, StandardOpenOption.READ) ) {
            props.load(is);
        }
        return props;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#exists(java.lang.String)
     */
    @Override
    public boolean exists ( String pid ) {
        return Files.exists(this.getConfigFile(pid));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#exists(java.lang.String, java.lang.String)
     */
    @Override
    public boolean exists ( String pid, String instance ) {
        return Files.exists(this.getConfigFile(pid, instance));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#createConfig(java.lang.String,
     *      java.util.Properties)
     */
    @Override
    public byte[] createConfig ( String pid, Properties props ) throws IOException, NoSuchAlgorithmException {
        Path cfg = this.getConfigFile(pid);
        return replaceContents(cfg, props, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#createConfig(java.lang.String, java.lang.String,
     *      java.util.Properties)
     */
    @Override
    public byte[] createConfig ( String pid, String instance, Properties props ) throws IOException, NoSuchAlgorithmException {
        Path cfg = this.getConfigFile(pid, instance);
        ensureInstanceDirectory(pid);
        return replaceContents(cfg, props, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#updateConfig(java.lang.String,
     *      java.util.Properties)
     */
    @Override
    public byte[] updateConfig ( String pid, Properties props ) throws NoSuchAlgorithmException, IOException {
        Path cfg = this.getConfigFile(pid);
        return replaceContents(cfg, props, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#updateConfig(java.lang.String, java.lang.String,
     *      java.util.Properties)
     */
    @Override
    public byte[] updateConfig ( String pid, String instance, Properties props ) throws NoSuchAlgorithmException, IOException {
        Path cfg = this.getConfigFile(pid, instance);
        return replaceContents(cfg, props, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#removeConfig(java.lang.String)
     */
    @Override
    public void removeConfig ( String pid ) throws IOException {
        Path cfg = this.getConfigFile(pid);
        Files.deleteIfExists(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter#removeConfig(java.lang.String, java.lang.String)
     */
    @Override
    public void removeConfig ( String pid, String instance ) throws IOException {
        Path cfg = this.getConfigFile(pid, instance);
        Files.deleteIfExists(cfg);
    }


    private void ensureInstanceDirectory ( String pid ) throws IOException {
        File instanceDir = new File(this.confBase.toFile(), pid);
        if ( instanceDir.isDirectory() ) {
            return;
        }

        if ( instanceDir.exists() ) {
            throw new IOException("Instance directory is a file"); //$NON-NLS-1$
        }

        Files.createDirectory(instanceDir.toPath(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
        setPathPermissions(instanceDir.toPath(), true);
    }


    /**
     * 
     * @param confFile
     * @param props
     * @param replace
     * @return a sha512 hash of the file contents
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private byte[] replaceContents ( Path confFile, Properties props, boolean replace ) throws IOException, NoSuchAlgorithmException {
        Path temp = FileTemporaryUtils.createRelatedTemporaryFile(confFile);
        try {
            try ( BufferedWriter newBufferedWriter = Files.newBufferedWriter(temp, CONFCHARSET, StandardOpenOption.WRITE) ) {
                props.store(newBufferedWriter, GENERATED_COMMENT);
            }

            byte[] hash = FileHashUtil.sha512(temp);

            setPathPermissions(temp, false);
            FileUtil.safeMove(temp, confFile, replace);
            return hash;
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }


    private void setPathPermissions ( Path temp, boolean directory ) throws IOException {
        if ( this.user != null || this.group != null ) {
            PosixFileAttributeView posix = Files.getFileAttributeView(temp, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

            if ( this.user != null ) {
                posix.setOwner(this.user);
            }

            if ( this.group != null ) {
                posix.setGroup(this.group);
            }
        }

        if ( directory ) {
            Files.setPosixFilePermissions(temp, getDirectoryPermissions());
        }
        else {
            Files.setPosixFilePermissions(temp, getFilePermissions());
        }
    }


    private Set<PosixFilePermission> getFilePermissions () {
        if ( this.groupWrite ) {
            return PosixFilePermissions.fromString("rw-rw----"); //$NON-NLS-1$
        }
        else if ( this.groupRead ) {
            return PosixFilePermissions.fromString("rw-r-----"); //$NON-NLS-1$
        }
        else {
            return PosixFilePermissions.fromString("rw-------"); //$NON-NLS-1$
        }
    }


    private Set<PosixFilePermission> getDirectoryPermissions () {
        if ( this.groupWrite ) {
            return PosixFilePermissions.fromString("rwxrwx---"); //$NON-NLS-1$
        }
        else if ( this.groupRead ) {
            return PosixFilePermissions.fromString("rwxr-x---"); //$NON-NLS-1$
        }
        else {
            return PosixFilePermissions.fromString("rwx------"); //$NON-NLS-1$
        }
    }

}

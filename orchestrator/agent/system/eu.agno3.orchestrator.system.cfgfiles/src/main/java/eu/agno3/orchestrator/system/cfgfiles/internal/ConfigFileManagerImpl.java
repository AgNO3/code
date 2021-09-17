/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 *
 */
public class ConfigFileManagerImpl implements ConfigFileManager {

    private Path cfgFileRoot;
    private UserPrincipal owner;
    private GroupPrincipal group;
    private Set<PosixFilePermission> filePerms;
    private Set<PosixFilePermission> dirPerms;


    /**
     * @param cfgFilePath
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * @throws IOException
     */
    public ConfigFileManagerImpl ( Path cfgFilePath, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) throws IOException {
        this.cfgFileRoot = cfgFilePath;
        this.owner = owner;
        this.group = group;
        this.filePerms = filePerms;
        this.dirPerms = dirPerms;

        Path p = this.cfgFileRoot;
        if ( !Files.exists(p) ) {
            FileSecurityUtils.createDirectories(p, this.owner, this.group, this.dirPerms);
            if ( !Files.exists(p) ) {
                throw new IOException("Failed to create cfgfile dir " + cfgFilePath); //$NON-NLS-1$
            }
        }

        FileSecurityUtils.setPermissions(p, this.owner, this.group, this.dirPerms);
        this.cfgFileRoot = p.toRealPath();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager#getBasePath()
     */
    @Override
    public Path getBasePath () {
        return this.cfgFileRoot;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager#getFiles()
     */
    @Override
    public List<String> getFiles () {
        Collection<File> files = FileUtils.listFiles(this.cfgFileRoot.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        if ( files == null ) {
            return Collections.EMPTY_LIST;
        }
        List<String> fileNames = new ArrayList<>();
        for ( File f : files ) {
            if ( f.isDirectory() ) {
                continue;
            }
            fileNames.add(f.getName());
        }
        return fileNames;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager#remove(java.lang.String)
     */
    @Override
    public void remove ( String file ) throws IOException {
        Path cfgFile = makeCfgFilePath(file);
        if ( !Files.deleteIfExists(cfgFile) ) {
            throw new IOException("Failed to delete file " + cfgFile); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager#createOrReplace(java.lang.String,
     *      java.nio.file.Path)
     */
    @Override
    public void createOrReplace ( String file, Path f ) throws IOException {
        Path cfgFile = makeCfgFilePath(file);
        FileSecurityUtils.setPermissions(f, this.owner, this.group, this.filePerms);
        FileUtil.safeMove(f, cfgFile, true);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager#getPath(java.lang.String)
     */
    @Override
    public Path getPath ( String path ) throws IOException {
        return makeCfgFilePath(path);
    }


    /**
     * @param file
     * @return
     * @throws IOException
     */
    private Path makeCfgFilePath ( String file ) throws IOException {
        Path p = this.cfgFileRoot.resolve(file);
        if ( ! ( p.normalize().startsWith(this.cfgFileRoot) ) ) {
            throw new IOException("Directory traversal"); //$NON-NLS-1$
        }
        return p;
    }

}

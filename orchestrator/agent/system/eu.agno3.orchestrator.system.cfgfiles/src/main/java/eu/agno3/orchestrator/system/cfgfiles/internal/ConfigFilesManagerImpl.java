/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigManager;


/**
 * @author mbechler
 *
 */
public class ConfigFilesManagerImpl implements ConfigFilesManager {

    private static final Logger log = Logger.getLogger(ConfigFilesManagerImpl.class);

    private File cfgPath;

    private UserPrincipal owner;
    private GroupPrincipal group;
    private Set<PosixFilePermission> filePerms;
    private Set<PosixFilePermission> dirPerms;


    /**
     * @param cfgPath
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     */
    public ConfigFilesManagerImpl ( File cfgPath, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        this.cfgPath = cfgPath;
        this.owner = owner;
        this.group = group;
        this.filePerms = filePerms;
        this.dirPerms = dirPerms;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getPresentFactoryPIDs()
     */
    @Override
    public Set<String> getPresentFactoryPIDs () {
        if ( !checkCfgPath() ) {
            return Collections.EMPTY_SET;
        }

        File[] factoryDirs = this.cfgPath.listFiles(new FactoryDirFilter());

        if ( factoryDirs == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Could not read config directory " + this.cfgPath); //$NON-NLS-1$
            }
            return Collections.EMPTY_SET;
        }

        Set<String> factories = new HashSet<>();
        for ( File instanceFile : factoryDirs ) {
            factories.add(instanceFile.getName());
        }
        return factories;
    }


    /**
     * 
     */
    private boolean checkCfgPath () {
        if ( !this.cfgPath.exists() || !this.cfgPath.isDirectory() || !this.cfgPath.canRead() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not a valid config path " + this.cfgPath); //$NON-NLS-1$
            }
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getPresentFactoryInstances(java.lang.String)
     */
    @Override
    public Set<String> getPresentFactoryInstances ( String factoryId ) {
        File instanceDir = makeInstancePath(factoryId);

        if ( !instanceDir.exists() || !instanceDir.isDirectory() || !instanceDir.canRead() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not a valid instance " + instanceDir); //$NON-NLS-1$
            }
            return Collections.EMPTY_SET;
        }

        return listConfigFiles(instanceDir);
    }


    /**
     * @param path
     * @return
     */
    private static Set<String> listConfigFiles ( File path ) {
        File[] instanceFiles = path.listFiles(new ConfigFileFilter());

        if ( instanceFiles == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Could not read instance directory " + path); //$NON-NLS-1$
            }
            return Collections.EMPTY_SET;
        }

        Set<String> instances = new HashSet<>();
        for ( File instanceFile : instanceFiles ) {
            instances.add(instanceFile.getName().substring(0, instanceFile.getName().length() - ConfigFileFilter.CONF_SUFFIX.length()));
        }
        return instances;
    }


    /**
     * @param factoryId
     * @return
     */
    private File makeInstancePath ( String factoryId ) {
        return new File(this.cfgPath, factoryId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getInstancePIDs()
     */
    @Override
    public Set<String> getInstancePIDs () {
        if ( !checkCfgPath() ) {
            return Collections.EMPTY_SET;
        }

        return listConfigFiles(this.cfgPath);
    }


    /**
     * @return
     */
    private File makeCfgFileBase () {
        return new File(this.cfgPath, "files"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#listCfgFileRoots()
     */
    @Override
    public Set<String> listCfgFileRoots () {
        File cfgFileBase = makeCfgFileBase();

        if ( !cfgFileBase.exists() || !cfgFileBase.isDirectory() || !cfgFileBase.canRead() ) {
            return Collections.EMPTY_SET;
        }

        String[] list = cfgFileBase.list();
        if ( list == null ) {
            return Collections.EMPTY_SET;
        }
        return new HashSet<>(Arrays.asList(list));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getInstance(java.lang.String)
     */
    @Override
    public ConfigManager getInstance ( String pid ) {
        String fileName = pid + ConfigFileFilter.CONF_SUFFIX;
        File instanceFile = new File(this.cfgPath, fileName);
        return new ConfigManagerImpl(pid, instanceFile, this.owner, this.group, this.filePerms, this.dirPerms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getFactoryInstance(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public ConfigManager getFactoryInstance ( String factoryId, String instanceId ) {
        String fileName = factoryId + "/" + instanceId + ConfigFileFilter.CONF_SUFFIX; //$NON-NLS-1$
        File instanceFile = new File(this.cfgPath, fileName);
        return new ConfigManagerImpl(factoryId, instanceId, instanceFile, this.owner, this.group, this.filePerms, this.dirPerms);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager#getCfgFileRoot(java.lang.String)
     */
    @Override
    public ConfigFileManager getCfgFileRoot ( String rootName ) throws IOException {
        File cfgFilePath = new File(makeCfgFileBase(), rootName);
        return new ConfigFileManagerImpl(cfgFilePath.toPath(), this.owner, this.group, this.filePerms, this.dirPerms);
    }

}

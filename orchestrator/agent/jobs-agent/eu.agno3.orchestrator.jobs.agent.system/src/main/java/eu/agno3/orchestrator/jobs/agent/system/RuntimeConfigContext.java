/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.contents.PropertiesProvider;
import eu.agno3.orchestrator.system.base.units.file.remove.Remove;
import eu.agno3.orchestrator.system.base.units.file.touch.Touch;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 * @param <T>
 * @param <J>
 *
 */
public class RuntimeConfigContext <T extends ConfigurationInstance, J extends ConfigurationJob> {

    private static final Logger log = Logger.getLogger(RuntimeConfigContext.class);

    private @NonNull RuntimeServiceManager rsm;

    private @NonNull JobBuilder builder;

    private @NonNull ConfigurationJobContext<T, J> context;

    private @NonNull Set<String> configuredPids = new HashSet<>();
    private @NonNull Set<String> modifiedPids = new HashSet<>();
    private @NonNull Set<String> addedPids = new HashSet<>();

    private @NonNull ConfigFilesManagerFactory cfm;

    private ConfigFilesManager cfManager;

    private Path prefix;

    private ExecutionConfig execConfig;

    private boolean noRemove;


    /**
     * @param rsm
     * @param cfm
     * @param builder
     * @param context
     * @param cfg
     * @param noRemove
     * @throws ServiceManagementException
     */
    public RuntimeConfigContext ( @NonNull RuntimeServiceManager rsm, @NonNull ConfigFilesManagerFactory cfm, @NonNull JobBuilder builder,
            @NonNull ConfigurationJobContext<T, J> context, ExecutionConfig cfg, boolean noRemove ) throws ServiceManagementException {
        super();
        this.rsm = rsm;
        this.cfm = cfm;
        this.builder = builder;
        this.context = context;
        this.execConfig = cfg;
        this.noRemove = noRemove;

        this.prefix = cfg.getPrefix();

        File actualFile = PrefixUtil.resolvePrefix(cfg, this.rsm.getConfigFilesPath()).toFile();
        this.cfManager = this.cfm.getForPath(
            actualFile,
            this.rsm.getServicePrincipal(),
            this.rsm.getGroupPrincipal(),
            FileSecurityUtils.getGroupReadFilePermissions(),
            FileSecurityUtils.getGroupReadDirPermissions());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "New context for %s with user %s and group %s", //$NON-NLS-1$
                actualFile,
                this.rsm.getServicePrincipal(),
                this.rsm.getGroupPrincipal()));
        }
    }


    /**
     * @param b
     * @return a wrapped context with the given job builder
     * @throws ServiceManagementException
     */
    public @NonNull RuntimeConfigContext<T, J> withBuilder ( @NonNull JobBuilder b ) throws ServiceManagementException {
        return new RuntimeConfigContext<>(this.rsm, this.cfm, b, this.context, this.execConfig, this.noRemove);
    }


    /**
     * @return the context
     */
    public @NonNull ConfigurationJobContext<T, J> octx () {
        return this.context;
    }


    /**
     * @return the rsm
     */
    public RuntimeServiceManager getServiceManager () {
        return this.rsm;
    }


    /**
     * @param factoryId
     * @param instanceId
     * @param cfg
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    public void factory ( String factoryId, String instanceId, PropertyConfigBuilder cfg )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        String pid = String.format("%s@%s", factoryId, instanceId); //$NON-NLS-1$
        if ( !this.configuredPids.add(pid) ) {
            log.warn("Factory is being added multiple times " + instanceId); //$NON-NLS-1$
        }
        Path actualCfgFile = this.cfManager.getFactoryInstance(factoryId, instanceId).getPath();
        Properties props = cfg.build();
        if ( checkModification(actualCfgFile, pid, props) || this.context.isForce() ) {
            this.builder.add(Contents.class).file(stripPrefix(actualCfgFile)).group(this.rsm.getGroupPrincipal())
                    .perms(FileSecurityUtils.getGroupReadFilePermissions())
                    .createTargetDir(FileSecurityUtils.getGroupReadDirPermissions(), null, this.rsm.getGroupPrincipal())
                    .content(new PropertiesProvider(props)).createTargetDir();
        }
    }


    /**
     * @param factoryId
     * @param instanceId
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     */
    public void ensureFactory ( String factoryId, String instanceId ) throws UnitInitializationFailedException, ServiceManagementException {
        String pid = String.format("%s@%s", factoryId, instanceId); //$NON-NLS-1$
        this.configuredPids.add(pid);
        if ( !this.cfManager.getFactoryInstance(factoryId, instanceId).exists() ) {
            this.modifiedPids.add(pid);
            Path actualCfgFile = this.cfManager.getFactoryInstance(factoryId, instanceId).getPath();
            this.builder.add(Touch.class).file(stripPrefix(actualCfgFile))
                    .createTargetDir(FileSecurityUtils.getGroupReadDirPermissions(), null, this.rsm.getGroupPrincipal())
                    .group(this.rsm.getGroupPrincipal()).perms(FileSecurityUtils.getGroupReadFilePermissions());
        }
    }


    /**
     * @param instanceId
     * @param cfg
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    public void instance ( String instanceId, PropertyConfigBuilder cfg )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        if ( !this.configuredPids.add(instanceId) ) {
            log.warn("Instance is being added multiple times " + instanceId); //$NON-NLS-1$
        }
        Path actualCfgFile = this.cfManager.getInstance(instanceId).getPath();
        Properties props = cfg.build();
        if ( checkModification(actualCfgFile, instanceId, props) || this.context.isForce() ) {
            this.builder.add(Contents.class).file(stripPrefix(actualCfgFile))
                    .createTargetDir(FileSecurityUtils.getGroupReadDirPermissions(), null, this.rsm.getGroupPrincipal())
                    .group(this.rsm.getGroupPrincipal()).perms(FileSecurityUtils.getGroupReadFilePermissions()).content(new PropertiesProvider(props))
                    .createTargetDir();
        }
    }


    /**
     * @param actualCfgFile
     * @param props
     * @param instanceId
     */
    private boolean checkModification ( Path actualCfgFile, String pid, Properties props ) {
        if ( !Files.exists(actualCfgFile) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("PID was added, file does not exist " + actualCfgFile); //$NON-NLS-1$
            }
            this.modifiedPids.add(pid);
            this.addedPids.add(pid);
            return true;
        }

        Properties oldProps = new Properties();
        try ( FileChannel ch = FileChannel.open(actualCfgFile, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(ch) ) {
            oldProps.load(is);
        }
        catch ( IOException e ) {
            log.warn("Failed to read current config file", e); //$NON-NLS-1$
            this.modifiedPids.add(pid);
            return true;
        }

        if ( props.equals(oldProps) ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            Set<Object> oldKeys = new HashSet<>(oldProps.keySet());
            Set<Object> newKeys = new HashSet<>(props.keySet());
            Set<Object> commons = new HashSet<>(oldKeys);
            commons.retainAll(newKeys);
            oldKeys.removeAll(newKeys);
            newKeys.removeAll(oldKeys);

            log.debug("Removed keys " + oldKeys); //$NON-NLS-1$
            log.debug("Added keys " + newKeys); //$NON-NLS-1$

            for ( Object key : commons ) {
                Object oldVal = oldProps.get(key);
                Object newVal = props.get(key);
                if ( !Objects.equals(oldVal, newVal) ) {
                    log.debug(String.format("Key %s differs: %s -> %s", key, oldVal, newVal)); //$NON-NLS-1$
                }
            }
        }

        this.modifiedPids.add(pid);
        return true;
    }


    /**
     * @return the configured pids
     */
    public Set<String> getConfiguredPids () {
        return this.configuredPids;
    }


    /**
     * @return the modifiedPids
     */
    public Set<String> getModifiedPids () {
        return this.modifiedPids;
    }


    /**
     * 
     * @return the present but not configured pids
     * @throws ServiceManagementException
     */
    public Set<String> getRemovedPids () throws ServiceManagementException {

        Set<String> removed = new HashSet<>();

        if ( this.noRemove ) {
            return removed;
        }

        Set<String> instancePIDs = this.cfManager.getInstancePIDs();
        if ( log.isDebugEnabled() ) {
            log.debug("Configured are " + this.getConfiguredPids()); //$NON-NLS-1$
            log.debug("Present instances " + instancePIDs); //$NON-NLS-1$
        }

        for ( String instance : instancePIDs ) {
            if ( !this.getConfiguredPids().contains(instance) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Removed instance " + instance); //$NON-NLS-1$
                }
                removed.add(instance);
            }
        }

        for ( String factory : this.cfManager.getPresentFactoryPIDs() ) {
            Set<String> presentFactoryInstances = this.cfManager.getPresentFactoryInstances(factory);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Present Factory instances for %s: %s", //$NON-NLS-1$
                    factory,
                    presentFactoryInstances));
            }
            for ( String instance : presentFactoryInstances ) {
                String full = String.format("%s@%s", factory, instance); //$NON-NLS-1$
                if ( !getConfiguredPids().contains(full) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removed factory instance " + full); //$NON-NLS-1$
                    }
                    removed.add(full);
                }
            }
        }
        return removed;
    }


    /**
     * Removes all unconfigured instances
     * 
     * @return all affected instances ( configured and removed )
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     */
    public Set<String> complete () throws ServiceManagementException, UnitInitializationFailedException {
        Set<String> removedPids = getRemovedPids();
        if ( log.isDebugEnabled() ) {
            log.info("Removed, without ignores: " + removedPids); //$NON-NLS-1$
        }
        Set<String> ignorePids = this.rsm.getNoRemovePIDs();
        removedPids.removeAll(ignorePids);
        if ( log.isDebugEnabled() ) {
            log.debug("Removing config of " + removedPids); //$NON-NLS-1$
        }
        for ( String removedPid : removedPids ) {
            int sepPos = removedPid.indexOf('@');

            if ( sepPos >= 0 ) {
                String factoryId = removedPid.substring(0, sepPos);
                String instanceId = removedPid.substring(sepPos + 1);
                this.builder.add(Remove.class).file(stripPrefix(this.cfManager.getFactoryInstance(factoryId, instanceId).getPath()));
            }
            else {
                this.builder.add(Remove.class).file(stripPrefix(this.cfManager.getInstance(removedPid).getPath()));
            }
        }

        Set<String> allAffected = new HashSet<>(getModifiedPids());
        allAffected.addAll(removedPids);
        return allAffected;
    }


    /**
     * @param path
     * @return
     */
    private Path stripPrefix ( Path path ) {
        if ( path.startsWith(this.prefix) ) {
            return Paths.get("/" + path.subpath(this.prefix.getNameCount(), path.getNameCount()).toString()); //$NON-NLS-1$
        }
        return path;
    }

}

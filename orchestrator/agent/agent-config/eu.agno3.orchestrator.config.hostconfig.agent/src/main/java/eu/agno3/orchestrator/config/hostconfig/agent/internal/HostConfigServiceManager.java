/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.internal;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.backup.KeystoreBackupUnit;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.ConfigFileBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.FilesBackupUnit;
import eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.jmx.JMXClient;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    BaseServiceManager.class, RuntimeServiceManager.class
}, configurationPid = "orchagent" )
public class HostConfigServiceManager extends AbstractSingletonRuntimeServiceManager {

    private ConfigLoader configLoader;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected synchronized void activate ( ComponentContext ctx ) {
        super.activate(ctx);
    }


    @Reference
    protected synchronized void setConfigLoader ( ConfigLoader cl ) {
        this.configLoader = cl;
    }


    protected synchronized void unsetConfigLoader ( ConfigLoader cl ) {
        if ( this.configLoader == cl ) {
            this.configLoader = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#getServiceType()
     */
    @Override
    public String getServiceType () {
        return "urn:agno3:1.0:hostconfig"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultInstallLocation()
     */
    @Override
    protected String getDefaultInstallLocation () {
        return "/opt/agno3/agent"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultSystemServiceName()
     */
    @Override
    protected String getDefaultSystemServiceName () {
        return "orchagent"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultConfigFilePath()
     */
    @Override
    protected String getDefaultConfigFilePath () {
        return "/etc/agent/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getJMXConnection()
     */
    @Override
    public JMXClient getJMXConnection () throws ServiceManagementException {
        throw new ServiceManagementException("This is the local agent, no JMX available"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getRuntimeStatus(java.util.UUID)
     */
    @Override
    public ServiceRuntimeStatus getRuntimeStatus ( UUID instanceId ) throws ServiceManagementException {
        // well, we are currently running
        return ServiceRuntimeStatus.ACTIVE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServicePrincipalName()
     */
    @Override
    protected String getServicePrincipalName () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServiceGroupPrincipalName()
     */
    @Override
    protected String getServiceGroupPrincipalName () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#reconfigure(java.util.Set)
     */
    @Override
    public void reconfigure ( Set<String> pids ) throws ServiceManagementException {
        try {
            this.configLoader.reload(pids);
        }
        catch ( IOException e ) {
            throw new ServiceManagementException("Failed to reload config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#forceReloadAll(java.lang.String)
     */
    @Override
    public void forceReloadAll ( String string ) throws ServiceManagementException {
        try {
            this.configLoader.forceReload(string);
        }
        catch ( IOException e ) {
            throw new ServiceManagementException("Failed to force reload config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#start(java.util.UUID)
     */
    @Override
    public void start ( UUID instanceId ) throws ServiceManagementException {
        unsupported();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#stop(java.util.UUID)
     */
    @Override
    public void stop ( UUID instanceId ) throws ServiceManagementException {
        unsupported();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#restart(java.util.UUID)
     */
    @Override
    public void restart ( UUID instanceId ) throws ServiceManagementException {
        unsupported();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#enable(java.util.UUID)
     */
    @Override
    public void enable ( UUID instanceId ) throws ServiceManagementException {
        unsupported();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#disable(java.util.UUID)
     */
    @Override
    public void disable ( UUID instanceId ) throws ServiceManagementException {
        unsupported();
    }


    /**
     * @throws ServiceManagementException
     * 
     */
    private static void unsupported () throws ServiceManagementException {
        throw new ServiceManagementException("This operation is not supported for the local agent"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws BackupException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getBackupUnits(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public List<BackupUnit> getBackupUnits ( @NonNull ConfigurationInstance cfg ) throws BackupException {
        try {
            List<BackupUnit> units = super.getBackupUnits(cfg);
            units.add(new ConfigFileBackupUnit(
                "keys", //$NON-NLS-1$
                "keys", //$NON-NLS-1$
                getServicePrincipal(),
                getGroupPrincipal(),
                FileSecurityUtils.getOwnerOnlyFilePermissions(),
                null));
            // TODO: replace with special unit to get proper permissions
            // units.add(new FilesBackupUnit(
            // "keystores", //$NON-NLS-1$
            // Paths.get("/etc/keystores/"))); //$NON-NLS-1$
            units.add(new KeystoreBackupUnit());
            units.add(new FilesBackupUnit(
                "truststores", //$NON-NLS-1$
                Paths.get("/etc/truststores/"), //$NON-NLS-1$
                null,
                null,
                FileSecurityUtils.getWorldReadableFilePermissions(),
                FileSecurityUtils.getWorldReadableDirPermissions()));
            return units;
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Building backup units failed", e); //$NON-NLS-1$
        }
    }
}

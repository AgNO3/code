/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.agent;


import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.CleanupBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.ConfigFileBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.DerbyDatabaseBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.StorageFilesBackupUnit;
import eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.jmx.JMXClientFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    BaseServiceManager.class, RuntimeServiceManager.class
}, configurationPid = "orchserver" )
public class OrchestratorRuntimeServiceManager extends AbstractSingletonRuntimeServiceManager {

    /**
     * 
     */
    private static final String ORCHSERVER = "orchserver"; //$NON-NLS-1$


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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#setJMXClientFactory(eu.agno3.runtime.jmx.JMXClientFactory)
     */
    @Override
    @Reference
    protected synchronized void setJMXClientFactory ( JMXClientFactory jcf ) {
        super.setJMXClientFactory(jcf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#unsetJMXClientFactory(eu.agno3.runtime.jmx.JMXClientFactory)
     */
    @Override
    protected synchronized void unsetJMXClientFactory ( JMXClientFactory jcf ) {
        super.unsetJMXClientFactory(jcf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#setServiceSystem(eu.agno3.orchestrator.system.base.service.ServiceSystem)
     */
    @Reference
    @Override
    protected synchronized void setServiceSystem ( ServiceSystem ss ) {
        super.setServiceSystem(ss);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#unsetServiceSystem(eu.agno3.orchestrator.system.base.service.ServiceSystem)
     */
    @Override
    protected synchronized void unsetServiceSystem ( ServiceSystem ss ) {
        super.unsetServiceSystem(ss);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#getServiceType()
     */
    @Override
    public String getServiceType () {
        return OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultInstallLocation()
     */
    @Override
    protected String getDefaultInstallLocation () {
        return "/opt/agno3/server/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultSystemServiceName()
     */
    @Override
    protected String getDefaultSystemServiceName () {
        return ORCHSERVER;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultConfigFilePath()
     */
    @Override
    protected String getDefaultConfigFilePath () {
        return "/etc/server/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultJMXSocketPath()
     */
    @Override
    protected String getDefaultJMXSocketPath () {
        return "/run/orchserver/rmi/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServicePrincipalName()
     */
    @Override
    protected String getServicePrincipalName () {
        return ORCHSERVER;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServiceGroupPrincipalName()
     */
    @Override
    protected String getServiceGroupPrincipalName () {
        return ORCHSERVER;
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
            OrchestratorConfiguration oc = (OrchestratorConfiguration) cfg;
            String dataStorage = oc.getAdvancedConfig().getDataStorage();

            List<BackupUnit> units = super.getBackupUnits(cfg);
            units.add(new ConfigFileBackupUnit(
                "keys", //$NON-NLS-1$
                "keys", //$NON-NLS-1$
                getServicePrincipal(),
                getGroupPrincipal(),
                FileSecurityUtils.getOwnerOnlyFilePermissions(),
                null));

            units.add(new StorageFilesBackupUnit(
                "reslibrary", //$NON-NLS-1$
                dataStorage,
                "resourceLibrary", //$NON-NLS-1$
                true,
                this.getOverrideStoragePath(dataStorage)));

            units.add(new DerbyDatabaseBackupUnit("auth", dataStorage, this)); //$NON-NLS-1$
            units.add(new DerbyDatabaseBackupUnit("config", dataStorage, this)); //$NON-NLS-1$
            units.add(new DerbyDatabaseBackupUnit("orchestrator", dataStorage, this)); //$NON-NLS-1$
            units.add(new DerbyDatabaseBackupUnit("jobs", dataStorage, this)); //$NON-NLS-1$

            units.add(new CleanupBackupUnit("tx-cleanup", dataStorage, "tx/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("cache", dataStorage, "cache/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("jms", dataStorage, "jms/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("api-session-cleanup", dataStorage, "api-session/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("session-cleanup", dataStorage, "session/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("tmpfiles-auth-cleanup", dataStorage, "tmp-auth/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("tmpfiles-auth-gui", dataStorage, "tmp-gui/", false, getOverrideStoragePath(dataStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            return units;
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Building backup units failed", e); //$NON-NLS-1$
        }
    }

}

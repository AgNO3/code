/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.agent;


import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.CleanupBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.ConfigFileBackupUnit;
import eu.agno3.orchestrator.jobs.agent.backup.units.DerbyDatabaseBackupUnit;
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
}, configurationPid = "fileshare" )
public class FileshareRuntimeServiceManager extends AbstractSingletonRuntimeServiceManager implements RuntimeServiceManager {

    /**
     * 
     */
    private static final String FILESHARE = "fileshare"; //$NON-NLS-1$


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#setJMXClientFactory(eu.agno3.runtime.jmx.JMXClientFactory)
     */
    @Reference
    @Override
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
     * @return
     */
    @Override
    protected String getDefaultSystemServiceName () {
        return FILESHARE;
    }


    /**
     * @return
     */
    @Override
    protected String getDefaultConfigFilePath () {
        return "/etc/fileshare/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultJMXSocketPath()
     */
    @Override
    protected String getDefaultJMXSocketPath () {
        return "/run/fileshare/rmi/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#getServiceType()
     */
    @Override
    public String getServiceType () {
        return FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServicePrincipalName()
     */
    @Override
    protected String getServicePrincipalName () {
        return FILESHARE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getServiceGroupPrincipalName()
     */
    @Override
    protected String getServiceGroupPrincipalName () {
        return FILESHARE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getDefaultInstallLocation()
     */
    @Override
    protected String getDefaultInstallLocation () {
        return "/opt/agno3/fileshare/"; //$NON-NLS-1$
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
            FileshareConfiguration fsc = (FileshareConfiguration) cfg;
            String localStorage = fsc.getStorageConfiguration().getLocalStorage();
            List<BackupUnit> units = super.getBackupUnits(cfg);
            units.add(new ConfigFileBackupUnit(
                "keys", //$NON-NLS-1$
                "keys", //$NON-NLS-1$
                getServicePrincipal(),
                getGroupPrincipal(),
                FileSecurityUtils.getOwnerOnlyFilePermissions(),
                null));
            units.add(new DerbyDatabaseBackupUnit("auth", localStorage, this)); //$NON-NLS-1$
            units.add(new DerbyDatabaseBackupUnit("fileshare", localStorage, this)); //$NON-NLS-1$
            units.add(new CleanupBackupUnit("tx-cleanup", localStorage, "tx/", false, getOverrideStoragePath(localStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("session-cleanup", localStorage, "session/", false, getOverrideStoragePath(localStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            units.add(new CleanupBackupUnit("tmpfiles-cleanup", localStorage, "tmp-files/", false, getOverrideStoragePath(localStorage))); //$NON-NLS-1$ //$NON-NLS-2$
            return units;
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Building backup units failed", e); //$NON-NLS-1$
        }
    }
}

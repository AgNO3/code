/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.nio.file.Path;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.ConfigFileBackupUnit;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class ConfigFileBackupUnitGenerator extends AbstractFileBackupUnitGenerator<ConfigFileBackupUnit> {

    private ServiceManager serviceManager;
    private ExecutionConfigProperties executionConfig;


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties exc ) {
        this.executionConfig = exc;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties exc ) {
        if ( this.executionConfig == exc ) {
            this.executionConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<ConfigFileBackupUnit> getUnitType () {
        return ConfigFileBackupUnit.class;
    }


    /**
     * @param service
     * @param unit
     * @return
     * @throws ServiceManagementException
     */
    @Override
    protected Path resolvePath ( ServiceStructuralObject service, ConfigFileBackupUnit unit ) throws BackupException {
        RuntimeServiceManager sm;
        try {
            sm = this.serviceManager.getServiceManager(StructuralObjectReferenceImpl.fromObject(service), RuntimeServiceManager.class);
        }
        catch ( ServiceManagementException e ) {
            throw new BackupException("Failed to get target service", e); //$NON-NLS-1$
        }

        Path p = PrefixUtil.resolvePrefix(this.executionConfig, sm.getConfigFilesPath());

        if ( unit.getSubPath() != null ) {
            p = p.resolve(unit.getSubPath());
        }
        return p;
    }

}

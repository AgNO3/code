/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.nio.file.Path;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator;
import eu.agno3.orchestrator.jobs.agent.backup.units.FilesBackupUnit;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class FilesBackupUnitGenerator extends AbstractFileBackupUnitGenerator<FilesBackupUnit> {

    private ExecutionConfigProperties execProperties;


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties ecp ) {
        this.execProperties = ecp;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties ecp ) {
        if ( this.execProperties == ecp ) {
            this.execProperties = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<FilesBackupUnit> getUnitType () {
        return FilesBackupUnit.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.impl.AbstractFileBackupUnitGenerator#resolvePath(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.units.AbstractFileBackupUnit)
     */
    @Override
    protected Path resolvePath ( ServiceStructuralObject service, FilesBackupUnit unit ) throws BackupException {
        return PrefixUtil.resolvePrefix(this.execProperties, unit.getPath());
    }

}

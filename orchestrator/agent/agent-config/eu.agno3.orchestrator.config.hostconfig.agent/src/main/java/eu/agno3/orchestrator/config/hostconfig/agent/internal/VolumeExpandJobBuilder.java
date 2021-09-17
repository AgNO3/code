/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.internal;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.info.jobs.ExpandVolumeJob;
import eu.agno3.orchestrator.system.info.storage.StorageInformationException;
import eu.agno3.orchestrator.system.info.storage.StorageInformationProvider;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.system.info.jobs.ExpandVolumeJob" )
@JobType ( ExpandVolumeJob.class )
public class VolumeExpandJobBuilder extends AbstractSystemJobRunnableFactory<ExpandVolumeJob> {

    private StorageInformationProvider storageInfoProvider;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Override
    @Reference
    protected void setExecutionConfig ( ExecutionConfig cfg ) {
        super.setExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Override
    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        super.unsetExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Override
    @Reference
    protected void setRunnerFactory ( RunnerFactory factory ) {
        super.setRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Override
    protected void unsetRunnerFactory ( RunnerFactory factory ) {
        super.unsetRunnerFactory(factory);
    }


    /**
     * 
     * @param sip
     */
    @Reference
    public void setStorageInfoProvider ( StorageInformationProvider sip ) {
        this.storageInfoProvider = sip;
    }


    /**
     * 
     * @param sip
     */
    public void unsetStorageInfoProvider ( StorageInformationProvider sip ) {
        if ( this.storageInfoProvider == sip ) {
            this.storageInfoProvider = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#buildJob(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void buildJob ( @NonNull JobBuilder b, @NonNull ExpandVolumeJob j ) throws JobBuilderException {

        try {
            Drive d = this.storageInfoProvider.getInformation().getDriveById(j.getDriveId());
            if ( d == null ) {
                throw new JobBuilderException("Drive not found " + d); //$NON-NLS-1$
            }

            if ( ! ( d instanceof PhysicalDrive ) ) {
                throw new JobBuilderException("Not a physical drive " + d); //$NON-NLS-1$
            }
            PhysicalDrive pd = (PhysicalDrive) d;

            List<String> args = new ArrayList<>();
            args.add(pd.getBlockDeviceName());
            args.add(j.getVolume());
            b.add(Exec.class).cmd("/usr/share/agno3-base/scripts/expand-volume.sh").args(args).stderrIsNotError(); //$NON-NLS-1$
        }
        catch (
            UnitInitializationFailedException |
            StorageInformationException e ) {
            throw new JobBuilderException("Failed to build drive initialization job", e); //$NON-NLS-1$
        }
    }

}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.system.info.units.RefreshSystemInformation;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.info.jobs.RescanDrivesJob;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.system.info.jobs.RescanDrivesJob" )
@JobType ( RescanDrivesJob.class )
public class DriveRescanJobBuilder extends AbstractSystemJobRunnableFactory<RescanDrivesJob> {

    /**
     * 
     */
    private static final String RESCAN_SCSI_BUS = "/usr/bin/rescan-scsi-bus.sh"; //$NON-NLS-1$
    private static final String RESCAN_SCSI_BUS_ARGS[] = new String[] {
        "-a", //$NON-NLS-1$
        "-s", //$NON-NLS-1$
        "-w", //$NON-NLS-1$
    };


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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#buildJob(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void buildJob ( @NonNull JobBuilder b, @NonNull RescanDrivesJob j ) throws JobBuilderException {
        try {
            b.add(Exec.class).cmd(RESCAN_SCSI_BUS).args(RESCAN_SCSI_BUS_ARGS);
            b.add(RefreshSystemInformation.class).rescanPartitions();
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to build drive rescan job", e); //$NON-NLS-1$
        }
    }

}

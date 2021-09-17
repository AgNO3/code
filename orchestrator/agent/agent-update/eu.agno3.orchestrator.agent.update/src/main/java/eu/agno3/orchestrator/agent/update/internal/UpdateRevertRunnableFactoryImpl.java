/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.UpdateInstallation;
import eu.agno3.orchestrator.agent.update.UpdateTracker;
import eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemException;
import eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.update.jobs.UpdateRevertJob;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.system.update.jobs.UpdateRevertJob" )
@JobType ( value = UpdateRevertJob.class )
public class UpdateRevertRunnableFactoryImpl extends AbstractSystemJobRunnableFactory<UpdateRevertJob> {

    private UpdateTracker updateTracker;
    private BaseSystemIntegration systemIntegration;


    @Reference
    protected synchronized void setUpdateTracker ( UpdateTracker ut ) {
        this.updateTracker = ut;
    }


    protected synchronized void unsetUpdateTracker ( UpdateTracker ut ) {
        if ( this.updateTracker == ut ) {
            this.updateTracker = null;
        }
    }


    @Reference
    protected synchronized void setSystemIntegration ( BaseSystemIntegration bsi ) {
        this.systemIntegration = bsi;
    }


    protected synchronized void unsetSystemIntegration ( BaseSystemIntegration bsi ) {
        if ( this.systemIntegration == bsi ) {
            this.systemIntegration = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setServiceManager(eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
    @Override
    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        super.setServiceManager(sm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetServiceManager(eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
    @Override
    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        super.unsetServiceManager(sm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Reference
    @Override
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
    @Reference
    @Override
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
    protected void buildJob ( @NonNull JobBuilder b, @NonNull UpdateRevertJob j ) throws JobBuilderException {

        UpdateInstallation revert = this.updateTracker.getRevert();
        if ( revert == null ) {
            throw new JobBuilderException("Nothing to revert to"); //$NON-NLS-1$
        }

        if ( revert.getSequence() != j.getRevertSequence() || !revert.getStream().equals(j.getRevertStream()) ) {
            throw new JobBuilderException("Mismatch in revert details"); //$NON-NLS-1$
        }

        try {
            b.add(Exec.class).cmd("/usr/bin/grub-editenv") //$NON-NLS-1$
                    .args("/boot/grub/grubenv", "set", "systemfail=1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.systemIntegration.reboot(b, 5);
        }
        catch (
            UnitInitializationFailedException |
            BaseSystemException e ) {
            throw new JobBuilderException("Failed to create revert job", e); //$NON-NLS-1$
        }
    }

}

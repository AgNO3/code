/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.agent.update.ServiceReconfigurator;
import eu.agno3.orchestrator.agent.update.UpdateDescriptorGenerator;
import eu.agno3.orchestrator.agent.update.UpdateInstallRunnableFactory;
import eu.agno3.orchestrator.agent.update.UpdateJobFactory;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.AbstractResumableSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.agent.AgentConnectionService;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.update.jobs.UpdateInstallJob;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    UpdateInstallRunnableFactory.class, JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.system.update.jobs.UpdateInstallJob" )
@JobType ( value = UpdateInstallJob.class )
public class UpdateInstallRunnableFactoryImpl extends AbstractResumableSystemJobRunnableFactory<UpdateInstallJob>
        implements UpdateInstallRunnableFactory {

    private UpdateJobFactory updateJobFactory;
    private UpdateDescriptorGenerator descriptorGenerator;
    private ServiceReconfigurator serviceReconfigurator;
    private AgentServerConnector connector;


    @Reference
    protected synchronized void setAgentConnector ( AgentServerConnector asc ) {
        this.connector = asc;
    }


    protected synchronized void unsetAgentConnector ( AgentServerConnector asc ) {
        if ( this.connector == asc ) {
            this.connector = null;
        }
    }


    @Reference
    protected synchronized void setUpdateJobFactory ( UpdateJobFactory r ) {
        this.updateJobFactory = r;
    }


    protected synchronized void unsetUpdateJobFactory ( UpdateJobFactory r ) {
        if ( this.updateJobFactory == r ) {
            this.updateJobFactory = null;
        }
    }


    @Reference
    protected synchronized void setUpdateDescriptorGenerator ( UpdateDescriptorGenerator udg ) {
        this.descriptorGenerator = udg;
    }


    protected synchronized void unsetUpdateDescriptorGenerator ( UpdateDescriptorGenerator udg ) {
        if ( this.descriptorGenerator == udg ) {
            this.descriptorGenerator = null;
        }
    }


    @Reference
    protected synchronized void setServiceReconfigurator ( ServiceReconfigurator sr ) {
        this.serviceReconfigurator = sr;
    }


    protected synchronized void unsetServiceReconfigurator ( ServiceReconfigurator sr ) {
        if ( this.serviceReconfigurator == sr ) {
            this.serviceReconfigurator = null;
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
    protected void buildJob ( @NonNull JobBuilder b, @NonNull UpdateInstallJob j ) throws JobBuilderException {
        this.updateJobFactory.buildJob(b, j.getAllowReboot(), j.getDescriptorStream(), j.getDescriptor());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#initServices(eu.agno3.orchestrator.system.base.execution.JobBuilder)
     */
    @Override
    protected void initServices ( @NonNull JobBuilder b ) throws InvalidUnitConfigurationException {
        super.initServices(b);

        if ( !b.getServices().containsKey(ServiceReconfigurator.class) ) {
            b.withService(ServiceReconfigurator.class, this.serviceReconfigurator);
        }

        if ( !b.getServices().containsKey(AgentServerConnector.class) ) {
            b.withService(AgentConnectionService.class, new AgentConnectionServiceImpl(this.connector));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractResumableSystemJobRunnableFactory#getExtraUnitClassLoaders()
     */
    @Override
    protected Collection<ClassLoader> getExtraUnitClassLoaders ( @NonNull UpdateInstallJob j ) {
        return this.updateJobFactory.getReconfigurationClassLoaders(j.getDescriptor());
    }

}

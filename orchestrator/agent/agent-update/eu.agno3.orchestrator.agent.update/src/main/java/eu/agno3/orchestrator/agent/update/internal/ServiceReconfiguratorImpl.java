/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.ServiceReconfigurator;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.system.ConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.ExtraServiceExecutionConfigWrapper;
import eu.agno3.orchestrator.jobs.agent.system.FillMissingInvocationHandler;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ServiceReconfigurator.class
} )
@SystemServiceType ( ServiceReconfigurator.class )
public class ServiceReconfiguratorImpl implements ServiceReconfigurator {

    private static final Logger log = Logger.getLogger(ServiceReconfiguratorImpl.class);

    private BundleContext context;
    private ConfigRepository configRepository;
    private ServiceTypeRegistry serviceTypeRegistry;
    private RunnerFactory runnerFactory;

    private ObjectTypeRegistry objectRegistry;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.context = ctx.getBundleContext();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository repo ) {
        this.configRepository = repo;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository repo ) {
        if ( this.configRepository == repo ) {
            this.configRepository = null;
        }
    }


    @Reference
    protected synchronized void setServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        this.serviceTypeRegistry = reg;
    }


    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        if ( this.serviceTypeRegistry == reg ) {
            this.serviceTypeRegistry = null;
        }
    }


    @Reference
    protected synchronized void setRunnerFactory ( RunnerFactory rf ) {
        this.runnerFactory = rf;
    }


    protected synchronized void unsetRunnerFactory ( RunnerFactory rf ) {
        if ( this.runnerFactory == rf ) {
            this.runnerFactory = null;
        }
    }


    @Reference
    protected synchronized void setObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        this.objectRegistry = otr;
    }


    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        if ( this.objectRegistry == otr ) {
            this.objectRegistry = null;
        }
    }


    @Override
    public void reconfigureAll ( Context ctx, boolean allowRestart, String serviceType )
            throws ModelServiceException, ConfigRepositoryException, JobBuilderException {
        Collection<ServiceStructuralObject> servicesByType = this.configRepository.getServicesByType(serviceType);
        for ( ServiceStructuralObject affected : servicesByType ) {
            reconfigure(ctx, allowRestart, StructuralObjectReferenceImpl.fromObject(affected));
        }
    }


    @Override
    public void reconfigure ( Context ctx, boolean allowRestart, StructuralObjectReference serviceRef )
            throws ConfigRepositoryException, ModelServiceException, JobBuilderException {

        Runner r = this.runnerFactory.createRunner();
        JobBuilder builder = r.makeJobBuilder();

        if ( builder == null ) {
            throw new JobBuilderException("Failed to create job builder"); //$NON-NLS-1$
        }

        if ( serviceRef.getType() != StructuralObjectType.SERVICE ) {
            throw new ModelServiceException("Not a service"); //$NON-NLS-1$
        }

        ServiceStructuralObject service = this.configRepository.getService(serviceRef);

        String serviceType = serviceRef.getLocalType();
        ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.serviceTypeRegistry
                .getDescriptor(serviceType);
        Optional<@NonNull ConfigurationInstance> activeConfiguration = this.configRepository.getActiveConfiguration(service);

        if ( activeConfiguration.isPresent() ) {
            @NonNull
            ConfigurationInstance config = activeConfiguration.get();

            config = FillMissingInvocationHandler.makeProxy(config, this.objectRegistry);

            @NonNull
            ConfigurationJob job = descriptor.makeConfigurationJob(config);
            job.setService(service);
            job.setAnchor(service);
            job.getApplyInfo().setForce(true);
            job.setNoRestart(!allowRestart);

            try {
                @SuppressWarnings ( "rawtypes" )
                Collection<ServiceReference<ConfigJobBuilder>> serviceReferences = this.context
                        .getServiceReferences(ConfigJobBuilder.class, FilterBuilder.get().eq("jobType", job.getClass().getName()).toString()); //$NON-NLS-1$

                if ( serviceReferences == null || serviceReferences.isEmpty() ) {
                    throw new ModelServiceException("Failed to find config job builder for " + serviceType); //$NON-NLS-1$
                }

                ConfigJobBuilder<ConfigurationInstance, ConfigurationJob> serviceBuilder = this.context
                        .getService(serviceReferences.iterator().next());
                if ( serviceBuilder == null ) {
                    throw new ModelServiceException("Failed to find config job builder is null for " + serviceType); //$NON-NLS-1$
                }

                serviceBuilder.addTo(builder, job);
            }
            catch ( InvalidSyntaxException e ) {
                throw new ModelServiceException(e);
            }
        }
        r.run(builder.getJob(), ctx.getOutput(), new ExtraServiceExecutionConfigWrapper(ctx.getConfig(), builder.getServices()), null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.ServiceReconfigurator#getUnitClassLoader(java.lang.String)
     */
    @Override
    public ClassLoader getUnitClassLoader ( String serviceType ) {
        try {
            ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.serviceTypeRegistry
                    .getDescriptor(serviceType);

            @SuppressWarnings ( "rawtypes" )
            Collection<ServiceReference<ConfigJobBuilder>> serviceReferences = this.context.getServiceReferences(
                ConfigJobBuilder.class,
                FilterBuilder.get().eq("jobType", descriptor.getConfigurationJobClass().getName()).toString()); //$NON-NLS-1$

            if ( serviceReferences == null || serviceReferences.isEmpty() ) {
                return null;
            }

            ConfigJobBuilder<ConfigurationInstance, ConfigurationJob> service = this.context.getService(serviceReferences.iterator().next());

            if ( service != null ) {
                return service.getClass().getClassLoader();
            }

            return null;
        }
        catch (
            ModelServiceException |
            InvalidSyntaxException e ) {
            log.warn("Failed to get unit classloader for " + serviceType); //$NON-NLS-1$
            return null;
        }
    }
}

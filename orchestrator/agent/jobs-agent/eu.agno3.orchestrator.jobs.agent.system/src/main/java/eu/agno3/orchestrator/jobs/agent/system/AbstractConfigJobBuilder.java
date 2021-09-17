/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.UnitFlags;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.file.contents.TemplateBuilder;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;


/**
 * @author mbechler
 * 
 * @param <TConfig>
 * @param <TJob>
 * 
 */
public abstract class AbstractConfigJobBuilder <TConfig extends ConfigurationInstance, TJob extends ConfigurationJob>
        extends AbstractSystemJobRunnableFactory<TJob> implements ConfigJobBuilder<TConfig, TJob> {

    private static final Logger log = Logger.getLogger(AbstractConfigJobBuilder.class);

    private ValidatorFactory validatorFactory;
    private ConfigRepository cfgRepository;
    private ServiceTypeRegistry serviceTypeRegistry;
    private BaseSystemInformationContext systemInfoContext;
    private TemplateConfigurationBuilder tplConfigBuilder;
    private ObjectTypeRegistry objectRegistry;

    private ConfigEventProducer configEvent;


    /**
     * 
     */
    public AbstractConfigJobBuilder () {
        super();
    }


    @Reference
    @Override
    protected synchronized void setRunnerFactory ( RunnerFactory factory ) {
        super.setRunnerFactory(factory);
    }


    @Override
    protected synchronized void unsetRunnerFactory ( RunnerFactory factory ) {
        super.unsetRunnerFactory(factory);
    }


    @Reference
    protected synchronized void setValidatorFactory ( ValidatorFactory vf ) {
        this.validatorFactory = vf;
    }


    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        if ( this.validatorFactory == vf ) {
            this.validatorFactory = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository repo ) {
        this.cfgRepository = repo;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository repo ) {
        if ( this.cfgRepository == repo ) {
            this.cfgRepository = null;
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
    protected synchronized void setTemplateConfigBuilder ( TemplateConfigurationBuilder tcb ) {
        this.tplConfigBuilder = tcb;
    }


    protected synchronized void unsetTemplateConfigBuilder ( TemplateConfigurationBuilder tcb ) {
        if ( this.tplConfigBuilder == tcb ) {
            this.tplConfigBuilder = null;
        }
    }


    @Reference
    protected synchronized void setSystemInfoContext ( BaseSystemInformationContext ctx ) {
        this.systemInfoContext = ctx;
    }


    protected synchronized void unsetSystemInfoContext ( BaseSystemInformationContext ctx ) {
        if ( this.systemInfoContext == ctx ) {
            this.systemInfoContext = null;
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


    @Reference
    protected synchronized void setConfigEventProducer ( ConfigEventProducer cep ) {
        this.configEvent = cep;
    }


    protected synchronized void unsetConfigEventProducer ( ConfigEventProducer cep ) {
        if ( this.configEvent == cep ) {
            this.configEvent = null;
        }
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


    @Override
    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        super.unsetExecutionConfig(cfg);
    }


    @Override
    public void addTo ( @NonNull JobBuilder b, @NonNull TJob j ) throws JobBuilderException {
        buildJob(b, j);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobBuilderException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#buildJob(eu.agno3.orchestrator.jobs.agent.system.JobBuilder,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void buildJob ( @NonNull JobBuilder b, @NonNull TJob j ) throws JobBuilderException {
        TConfig newConfig;
        try {
            newConfig = proxyOldConfig(j);
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Could not get config", e); //$NON-NLS-1$
        }
        validateConfig(newConfig);
        b.named(this.getJobName(j));
        try {
            if ( !b.getServices().containsKey(TemplateBuilder.class) ) {
                b.withService(TemplateBuilder.class, new TemplateBuilder(this.tplConfigBuilder));
            }

            initServices(b);
        }
        catch ( InvalidUnitConfigurationException e ) {
            throw new JobBuilderException("Failed to register services", e); //$NON-NLS-1$
        }

        if ( j.getApplyInfo().isForce() ) {
            b.withFlag(UnitFlags.FORCE);
        }

        ConfigurationJobServiceContext sctx = makeServiceContext(j);
        ConfigurationJobContext<TConfig, TJob> ctx = makeJobContext(j, newConfig, sctx);

        try {
            if ( !j.isBootstrapping() ) {
                b.onFail().add(FailedConfiguration.class).service(sctx.getService()).config(newConfig);
            }
            this.buildConfigJob(b, ctx);
            b.add(SetActiveServiceConfiguration.class).service(sctx.getService()).config(newConfig).noNotify(j.isBootstrapping());
        }
        catch ( Exception e ) {
            if ( !j.isBootstrapping() ) {
                this.configEvent.configFailed(sctx.getAnchor(), sctx.getService(), newConfig);
            }
            throw new JobBuilderException("Failed to build configuration job for " + this.getConfigClass().getName(), e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @return
     * @throws UnitInitializationFailedException
     */
    @NonNull
    TConfig proxyOldConfig ( @NonNull TJob j ) throws UnitInitializationFailedException {
        try {
            @NonNull
            TConfig cfg = this.getConfigFromJob(j);
            return FillMissingInvocationHandler.makeProxy(cfg, this.objectRegistry);
        }
        catch ( ModelServiceException e ) {
            throw new UnitInitializationFailedException("Failed to produce config proxy", e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @param newConfig
     * @param sctx
     * @return
     * @throws JobBuilderException
     */
    private @NonNull ConfigurationJobContext<TConfig, TJob> makeJobContext ( @NonNull TJob j, @NonNull TConfig newConfig,
            ConfigurationJobServiceContext sctx ) throws JobBuilderException {
        Optional<TConfig> current;

        if ( !j.isBootstrapping() ) {
            current = fetchCurrentConfig(j);
        }
        else {
            current = Optional.empty();
        }

        ConfigurationJobContext<TConfig, TJob> ctx = new ConfigurationJobContext<>(
            this,
            this.getClass().getClassLoader(),
            this.getConfigClass(),
            j,
            sctx,
            this.systemInfoContext,
            newConfig,
            current,
            j.getApplyInfo().isForce());
        return ctx;
    }


    /**
     * @param j
     * @return
     * @throws JobBuilderException
     */
    private Optional<TConfig> fetchCurrentConfig ( TJob j ) throws JobBuilderException {
        try {
            return this.cfgRepository.getActiveConfiguration(j.getService());
        }
        catch ( ConfigRepositoryException e ) {
            throw new JobBuilderException("Failed to get current configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @return
     * @throws JobBuilderException
     */
    private @NonNull ConfigurationJobServiceContext makeServiceContext ( TJob j ) throws JobBuilderException {

        if ( j.isBootstrapping() ) {
            return makeBootstrapServiceContext(j);
        }

        ServiceStructuralObject service;
        StructuralObject anchor = j.getAnchor();
        ServiceTypeDescriptor<@NonNull ?, @NonNull ?> serviceTypeDescriptor;
        try {
            ServiceStructuralObject passedService = j.getService();
            service = this.cfgRepository.ensureServiceRegistered(passedService);
            serviceTypeDescriptor = this.cfgRepository.getServiceTypeDescriptor(service);

            if ( service == null ) {
                throw new JobBuilderException("Service is null"); //$NON-NLS-1$
            }

            if ( serviceTypeDescriptor == null ) {
                throw new JobBuilderException("Service descriptor not found for " + service.getServiceType()); //$NON-NLS-1$
            }

            if ( anchor == null ) {
                throw new JobBuilderException("Anchor is null"); //$NON-NLS-1$
            }
        }
        catch ( ConfigRepositoryException e ) {
            throw new JobBuilderException("Could not register or get service", e); //$NON-NLS-1$
        }

        Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes = new HashMap<>();
        Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices = new HashMap<>();
        setupContextServices(serviceTypeDescriptor, contextServiceTypes, contextServices);

        return new ConfigurationJobServiceContextImpl(anchor, service, serviceTypeDescriptor, contextServiceTypes, contextServices);
    }


    /**
     * @param j
     * @return
     * @throws JobBuilderException
     * @throws ModelServiceException
     */
    protected @NonNull ConfigurationJobServiceContext makeBootstrapServiceContext ( TJob j ) throws JobBuilderException {
        try {
            ServiceStructuralObject service = j.getService();
            StructuralObject anchor = j.getAnchor();
            if ( service == null ) {
                throw new JobBuilderException("Service is null"); //$NON-NLS-1$
            }
            if ( anchor == null ) {
                anchor = service;
            }
            ServiceTypeDescriptor<@NonNull ?, @NonNull ?> serviceTypeDescriptor = this.serviceTypeRegistry.getDescriptor(getServiceType());
            if ( serviceTypeDescriptor == null ) {
                throw new JobBuilderException("Failed to get bootstrap service descriptor"); //$NON-NLS-1$
            }
            Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes = new HashMap<>();
            Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices = new HashMap<>();

            for ( Entry<String, ? extends ConfigurationInstance> inst : this.getBootstrapContextServices(j).entrySet() ) {
                @SuppressWarnings ( "unchecked" )
                Class<? extends ConfigurationInstance> type = (Class<? extends ConfigurationInstance>) inst.getValue().getType();
                contextServiceTypes.put(inst.getKey(), type);
                contextServices.put(type, inst.getValue());
            }
            return new ConfigurationJobServiceContextImpl(anchor, service, serviceTypeDescriptor, contextServiceTypes, contextServices);
        }
        catch ( ModelServiceException e ) {
            throw new JobBuilderException("Failed to produce bootstrap context"); //$NON-NLS-1$
        }
    }


    /**
     * @return configurations to inject as context services
     */
    protected Map<String, ? extends ConfigurationInstance> getBootstrapContextServices ( TJob j ) {
        return Collections.EMPTY_MAP;
    }


    /**
     * @return
     */
    protected abstract @NonNull String getServiceType ();


    /**
     * @param serviceTypeDescriptor
     * @param contextServiceTypes
     * @param contextServices
     * @throws JobBuilderException
     */
    private void setupContextServices ( ServiceTypeDescriptor<?, ?> serviceTypeDescriptor,
            Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes,
            Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices ) throws JobBuilderException {
        try {
            for ( String serviceType : serviceTypeDescriptor.getRequiredServices() ) {
                addServiceConfig(contextServiceTypes, contextServices, serviceType, true);
            }
        }
        catch (
            ModelServiceException |
            ConfigRepositoryException e ) {
            throw new JobBuilderException("Error retrieving required service", e); //$NON-NLS-1$
        }

        for ( String serviceType : serviceTypeDescriptor.getUsedServices() ) {
            try {
                addServiceConfig(contextServiceTypes, contextServices, serviceType, false);
            }
            catch (
                ModelServiceException |
                ConfigRepositoryException e ) {
                throw new JobBuilderException("Error retrieving used service", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param contextServiceTypes
     * @param contextServices
     * @param serviceType
     * @throws ModelServiceException
     * @throws JobBuilderException
     * @throws ConfigRepositoryException
     */
    private void addServiceConfig ( Map<String, Class<? extends ConfigurationInstance>> contextServiceTypes,
            Map<Class<? extends ConfigurationInstance>, ConfigurationInstance> contextServices, String serviceType, boolean required )
                    throws ModelServiceException, JobBuilderException, ConfigRepositoryException {
        ServiceTypeDescriptor<@NonNull ConfigurationInstance, ConfigurationInstance> desc = this.serviceTypeRegistry.getDescriptor(serviceType);
        ServiceStructuralObject reqService = ensureServiceRequirements(desc, required, serviceType);
        ConfigurationInstance config = ensureServiceRequirementConfig(serviceType, required, desc, reqService);

        if ( config == null ) {
            return;
        }

        contextServiceTypes.put(serviceType, desc.getConfigurationType());
        if ( contextServices.put(desc.getConfigurationType(), config) != null ) {
            throw new JobBuilderException("Multiple service dependencies for " + serviceType); //$NON-NLS-1$
        }
    }


    /**
     * @param serviceType
     * @param required
     * @param desc
     * @param reqService
     * @return
     * @throws ConfigRepositoryException
     * @throws JobBuilderException
     */
    private ConfigurationInstance ensureServiceRequirementConfig ( String serviceType, boolean required,
            ServiceTypeDescriptor<@NonNull ConfigurationInstance, ConfigurationInstance> desc, ServiceStructuralObject reqService )
                    throws ConfigRepositoryException, JobBuilderException {

        if ( reqService == null ) {
            return null;
        }

        Optional<@NonNull ConfigurationInstance> config = this.cfgRepository.getActiveConfiguration(reqService);

        if ( !config.isPresent() && !required ) {
            return null;
        }
        else if ( !config.isPresent() ) {
            throw new JobBuilderException("Required service is not configured " + serviceType); //$NON-NLS-1$
        }
        return config.get();
    }


    /**
     * @param desc
     * @return
     * @throws JobBuilderException
     * @throws ConfigRepositoryException
     */
    private ServiceStructuralObject ensureServiceRequirements ( ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> desc,
            boolean required, String serviceType ) throws JobBuilderException, ConfigRepositoryException {
        if ( desc.isMultiInstance() ) {
            throw new JobBuilderException("Found dependency services that is multi instance, unsupported"); //$NON-NLS-1$
        }

        ServiceStructuralObject reqService = this.cfgRepository.getSingletonServiceByType(serviceType);

        if ( reqService == null && !required ) {
            return reqService;
        }
        else if ( reqService == null ) {
            throw new JobBuilderException("Required service not found " + serviceType); //$NON-NLS-1$
        }

        return reqService;
    }


    /**
     * @return
     */
    protected abstract String getJobName ( TJob j );


    private void validateConfig ( TConfig cfg ) throws JobBuilderException {
        Validator v = this.validatorFactory.getValidator();
        Set<ConstraintViolation<TConfig>> violations = v.validate(cfg, Materialized.class);
        if ( !violations.isEmpty() ) {
            for ( ConstraintViolation<TConfig> violation : violations ) {
                log.warn(String.format(
                    "Violation on %s for constraint %s: %s", //$NON-NLS-1$
                    violation.getPropertyPath(),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getName(),
                    violation.getMessage()));
            }
            throw new JobBuilderException("Materialized configuration is not valid"); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    protected abstract @NonNull Class<TConfig> getConfigClass ();


    /**
     * @param j
     * @return
     * @throws UnitInitializationFailedException
     */
    protected abstract @NonNull TConfig getConfigFromJob ( @NonNull TJob j ) throws UnitInitializationFailedException;


    /**
     * @param ctx
     * @throws JobBuilderException
     * @throws Exception
     */
    protected abstract void buildConfigJob ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<TConfig, TJob> ctx ) throws Exception;

}
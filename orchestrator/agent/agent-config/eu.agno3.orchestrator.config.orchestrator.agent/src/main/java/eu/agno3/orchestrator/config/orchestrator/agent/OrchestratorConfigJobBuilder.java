/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.agent;


import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureGeneratedKey;
import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureKeystore;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil;
import eu.agno3.orchestrator.config.hostconfig.agent.api.StorageContext;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorAuthenticationConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorEventLogConfiguration;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.jobs.OrchestratorConfigurationJob;
import eu.agno3.orchestrator.config.realms.RealmConfig;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.config.web.WebEndpointConfig;
import eu.agno3.orchestrator.config.web.agent.RuntimeConfigUtil;
import eu.agno3.orchestrator.config.web.agent.WebConfigUtil;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.monitor.units.ServiceCheck;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.units.ReloadServiceConfig;
import eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer;
import eu.agno3.orchestrator.jobs.agent.system.ConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.GroupJobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    OrchestratorConfigJobBuilder.class, JobRunnableFactory.class, ConfigJobBuilder.class
}, property = "jobType=eu.agno3.orchestrator.config.orchestrator.jobs.OrchestratorConfigurationJob" )
@JobType ( OrchestratorConfigurationJob.class )
public class OrchestratorConfigJobBuilder extends AbstractRuntimeConfigJobBuilder<@NonNull OrchestratorConfiguration, OrchestratorConfigurationJob> {

    private static final Logger log = Logger.getLogger(OrchestratorConfigJobBuilder.class);

    private static final String ORCHSERVER = "orchserver"; //$NON-NLS-1$

    private ServiceStorageUtil serviceStorage;
    private AuthenticatorConfigurator authenticatorConfigurator;

    private KerberosConfigUtil kerberosConfigUtil;

    private RuntimeConfigUtil runtimeConfigBuilder;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setConfigRepository(eu.agno3.orchestrator.jobs.agent.system.ConfigRepository)
     */
    @Reference
    @Override
    protected synchronized void setConfigRepository ( ConfigRepository repo ) {
        super.setConfigRepository(repo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetConfigRepository(eu.agno3.orchestrator.jobs.agent.system.ConfigRepository)
     */
    @Override
    protected synchronized void unsetConfigRepository ( ConfigRepository repo ) {
        super.unsetConfigRepository(repo);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setTemplateConfigBuilder(eu.agno3.runtime.tpl.TemplateConfigurationBuilder)
     */
    @Override
    @Reference
    protected synchronized void setTemplateConfigBuilder ( TemplateConfigurationBuilder tcb ) {
        super.setTemplateConfigBuilder(tcb);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetTemplateConfigBuilder(eu.agno3.runtime.tpl.TemplateConfigurationBuilder)
     */
    @Override
    protected synchronized void unsetTemplateConfigBuilder ( TemplateConfigurationBuilder tcb ) {
        super.unsetTemplateConfigBuilder(tcb);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Reference
    @Override
    protected synchronized void setRunnerFactory ( RunnerFactory factory ) {
        super.setRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Override
    protected synchronized void unsetRunnerFactory ( RunnerFactory factory ) {
        super.unsetRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setValidatorFactory(javax.validation.ValidatorFactory)
     */
    @Reference
    @Override
    protected synchronized void setValidatorFactory ( ValidatorFactory vf ) {
        super.setValidatorFactory(vf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetValidatorFactory(javax.validation.ValidatorFactory)
     */
    @Override
    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        super.unsetValidatorFactory(vf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Reference
    @Override
    protected void setExecutionConfig ( ExecutionConfig cfg ) {
        super.setExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Override
    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        super.unsetExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setSystemInfoContext(eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext)
     */
    @Override
    @Reference
    protected synchronized void setSystemInfoContext ( BaseSystemInformationContext ctx ) {
        super.setSystemInfoContext(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetSystemInfoContext(eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext)
     */
    @Override
    protected synchronized void unsetSystemInfoContext ( BaseSystemInformationContext ctx ) {
        super.unsetSystemInfoContext(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setServiceTypeRegistry(eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry)
     */
    @Override
    @Reference
    protected synchronized void setServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        super.setServiceTypeRegistry(reg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetServiceTypeRegistry(eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry)
     */
    @Override
    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        super.unsetServiceTypeRegistry(reg);
    }


    /**
     * 
     * @param ssu
     */
    @Reference
    protected synchronized void setServiceStorageUtil ( ServiceStorageUtil ssu ) {
        this.serviceStorage = ssu;
    }


    protected synchronized void unsetServiceStorageUtil ( ServiceStorageUtil ssu ) {
        if ( this.serviceStorage == ssu ) {
            this.serviceStorage = null;
        }
    }


    @Override
    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        super.setServiceManager(sm);
    }


    @Override
    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        super.unsetServiceManager(sm);
    }


    @Reference
    protected synchronized void setAuthenticatorConfigurator ( AuthenticatorConfigurator ac ) {
        this.authenticatorConfigurator = ac;
    }


    protected synchronized void unsetAuthenticatorConfigurator ( AuthenticatorConfigurator ac ) {
        if ( this.authenticatorConfigurator == ac ) {
            this.authenticatorConfigurator = null;
        }
    }


    @Reference
    protected synchronized void setKerberosConfigUtil ( KerberosConfigUtil rm ) {
        this.kerberosConfigUtil = rm;
    }


    protected synchronized void unsetKerberosConfigUtil ( KerberosConfigUtil rm ) {
        if ( this.kerberosConfigUtil == rm ) {
            this.kerberosConfigUtil = null;
        }
    }


    @Reference
    protected synchronized void setRuntimeConfigBuilder ( RuntimeConfigUtil rtc ) {
        this.runtimeConfigBuilder = rtc;
    }


    protected synchronized void unsetRuntimeConfigBuilder ( RuntimeConfigUtil rtc ) {
        if ( this.runtimeConfigBuilder == rtc ) {
            this.runtimeConfigBuilder = null;
        }
    }


    @Override
    @Reference
    protected synchronized void setConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        super.setConfigFilesManagerFactory(cfmf);
    }


    @Override
    protected synchronized void unsetConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        super.unsetConfigFilesManagerFactory(cfmf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setObjectTypeRegistry(eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry)
     */
    @Override
    @Reference
    protected synchronized void setObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        super.setObjectTypeRegistry(otr);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetObjectTypeRegistry(eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry)
     */
    @Override
    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        super.unsetObjectTypeRegistry(otr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigClass()
     */
    @SuppressWarnings ( "null" )
    @Override
    protected @NonNull Class<@NonNull OrchestratorConfiguration> getConfigClass () {
        return OrchestratorConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setConfigEventProducer(eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer)
     */
    @Override
    @Reference
    protected synchronized void setConfigEventProducer ( ConfigEventProducer cep ) {
        super.setConfigEventProducer(cep);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#unsetConfigEventProducer(eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer)
     */
    @Override
    protected synchronized void unsetConfigEventProducer ( ConfigEventProducer cep ) {
        super.unsetConfigEventProducer(cep);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnitInitializationFailedException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigFromJob(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected @NonNull OrchestratorConfiguration getConfigFromJob ( @NonNull OrchestratorConfigurationJob j )
            throws UnitInitializationFailedException {
        OrchestratorConfiguration orchConfig = j.getOrchestratorConfig();
        if ( orchConfig == null ) {
            throw new UnitInitializationFailedException("Orchestrator config is null"); //$NON-NLS-1$
        }
        return orchConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getServiceType()
     */
    @Override
    protected @NonNull String getServiceType () {
        return OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getBootstrapContextServices(eu.agno3.orchestrator.config.model.jobs.ConfigurationJob)
     */
    @Override
    protected Map<String, ? extends ConfigurationInstance> getBootstrapContextServices ( OrchestratorConfigurationJob j ) {
        HostConfiguration bcHostConfig = j.getBootstrapHostConfig();
        if ( bcHostConfig == null ) {
            return Collections.EMPTY_MAP;
        }
        return Collections.singletonMap(HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE, bcHostConfig);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getJobName(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected String getJobName ( OrchestratorConfigurationJob j ) {
        return "Reconfiguring orchestrator server"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder#buildConfigJob(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext)
     */
    @SuppressWarnings ( "nls" )
    @Override
    protected void buildConfigJob ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<@NonNull OrchestratorConfiguration, OrchestratorConfigurationJob> ctx ) throws Exception {
        @NonNull
        GroupJobBuilder cfgGroup = b.beginGroup();
        @NonNull
        RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> gctx = ctx.withBuilder(cfgGroup);

        ensureKeystores(cfgGroup, gctx);
        HostConfiguration hc = ctx.octx().sctx().getContextService(HostConfiguration.class);

        StorageContext localStorage = getStorage(cfgGroup, gctx, hc, ctx.octx().cfg().getAdvancedConfig().getDataStorage(), null);
        StorageContext tempStorage = getStorage(cfgGroup, gctx, hc, ctx.octx().cfg().getAdvancedConfig().getTempStorage(), null);

        gctx.factory(
            "tls.mapping",
            "agent-server",
            PropertyConfigBuilder.get().p("subsystem", "jms/server").p("keyStore", ORCHSERVER).p("priority", 1000).p("trustStore", "internal")
                    .p("keyAlias", "server"));

        StorageContext eventStorage = getStorage(
            cfgGroup,
            gctx,
            hc,
            ctx.octx().cfg().getEventLogConfig().getEventStorage(),
            !ctx.octx().cur().isPresent() ? null : ctx.octx().cur().get().getEventLogConfig().getEventStorage());
        setupStorage(cfgGroup, gctx, localStorage, tempStorage);
        setupWebFrontend(cfgGroup, gctx, hc, localStorage, tempStorage);
        Set<String> extraAuthServerNames = setupAuth(cfgGroup, gctx, hc);
        setupEventLogConfig(cfgGroup, gctx, eventStorage);

        gctx.instance(
            "server",
            PropertyConfigBuilder.get().p("id", ctx.octx().sctx().getService().getId().toString()).p("allowedAuthServerNames", extraAuthServerNames));

        this.kerberosConfigUtil.writeKerberosConfig(gctx);

        boolean needRestart = false;
        needRestart |= setupRuntimeConfig(cfgGroup, gctx, needRestart);

        Set<String> modifiedPids = gctx.complete();

        if ( !ctx.octx().job().isBootstrapping() ) {
            if ( ctx.octx().preventRestart() ) {
                log.info("Not restarting service (forced)"); //$NON-NLS-1$
            }
            else if ( needRestart ) {
                log.info("Restarting service"); //$NON-NLS-1$
            }
            else {
                log.info("Reload " + modifiedPids); //$NON-NLS-1$
            }
            cfgGroup.after(ReloadServiceConfig.class).service(ctx.octx().job().getService()).modified(modifiedPids)
                    .forceRestart(needRestart || ctx.octx().job().getApplyInfo().isForce()).skipIf(ctx.octx().preventRestart())
                    .runIf(!modifiedPids.isEmpty() || needRestart);
            cfgGroup.after(ServiceCheck.class).service(StructuralObjectReferenceImpl.fromObject(ctx.octx().job().getService()))
                    .timeout(Duration.standardMinutes(5)).skipIf(ctx.octx().preventRestart());
        }
    }


    /**
     * @param b
     * @param ctx
     * @param hc
     * @return
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    private Set<String> setupAuth ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx,
            HostConfiguration hc )
                    throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {
        OrchestratorAuthenticationConfiguration ac = ctx.octx().cfg().getAuthenticationConfig();
        this.authenticatorConfigurator.setupAuthenticators(b, ctx, ac.getAuthenticatorsConfig());
        this.authenticatorConfigurator.setupRoles(b, ctx, ac.getRoleConfig());

        Set<String> extraAuthServerNames = new HashSet<>();
        for ( RealmConfig rc : hc.getRealmsConfiguration().getRealms() ) {
            if ( !StringUtils.isBlank(rc.getOverrideLocalHostname()) ) {
                extraAuthServerNames.add(rc.getOverrideLocalHostname());
            }
        }
        return extraAuthServerNames;
    }


    /**
     * 
     * /**
     * 
     * @param b
     * @param ctx
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     */
    private static void ensureKeystores ( JobBuilder b, @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx )
            throws UnitInitializationFailedException, ServiceManagementException, InvalidParameterException {
        b.add(EnsureKeystore.class).keystore(ORCHSERVER).user(ctx.getServiceManager().getServicePrincipal()).internal();
        b.add(EnsureGeneratedKey.class).keystore(ORCHSERVER).alias("server"); //$NON-NLS-1$
        ctx.factory("keystore", ORCHSERVER, PropertyConfigBuilder.get()); //$NON-NLS-1$
    }


    /**
     * @param b
     * @param ctx
     * @param needRestart
     * @return
     * @throws MatcherException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     * @throws SystemInformationException
     */
    private boolean setupRuntimeConfig ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx, boolean needRestart ) throws MatcherException,
                    UnitInitializationFailedException, ServiceManagementException, InvalidParameterException, SystemInformationException {
        RuntimeConfiguration runtimeConfig = ctx.octx().cfg().getAdvancedConfig().getRuntimeConfig();

        this.runtimeConfigBuilder.configureDebugging(ctx, runtimeConfig);

        ctx.instance("license", PropertyConfigBuilder.get()); //$NON-NLS-1$
        if ( ctx.octx().changed(ctx.octx().match().getAdvancedConfig().getRuntimeConfig().getMemoryLimit()) ) {
            return this.runtimeConfigBuilder.setupRuntimeConfig(
                b,
                ctx.octx(),
                runtimeConfig,
                512,
                2048,
                0.3f,
                FileSystems.getDefault().getPath("/opt/agno3/server/orchserver.config.local"), //$NON-NLS-1$
                Collections.EMPTY_LIST,
                Collections.EMPTY_MAP,
                ctx.getServiceManager().getGroupPrincipal());
        }
        return false;
    }


    /**
     * @param b
     * @param ctx
     * @param localStorage
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    @SuppressWarnings ( "nls" )
    private static void setupEventLogConfig ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx, StorageContext eventStorage )
                    throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {

        String agentId;
        try {
            agentId = ctx.octx().platformInfo().getAgentId().toString();
        }
        catch ( SystemInformationException e ) {
            throw new JobBuilderException("Failed to get agent id", e);
        }

        ctx.instance(
            "es.node",
            PropertyConfigBuilder.get().p("es.path.data", eventStorage.getLocalStorage().resolve("es/").toString()).p("es.node.name", agentId)
                    .p("es.cluster.name", "orchestrator"));

        OrchestratorEventLogConfiguration evCfg = ctx.octx().cfg().getEventLogConfig();
        long retain = evCfg.getDisableLogExpiration() ? -1 : evCfg.getRetainDays();

        if ( evCfg.getWriteLogFiles() ) {
            PropertyConfigBuilder fileCfg = PropertyConfigBuilder.get().p("logPath", eventStorage.getLocalStorage().resolve("logs/").toString())
                    .p("retainDays", retain);
            ctx.factory("event.filelog", "default", fileCfg);
        }

        PropertyConfigBuilder esCfg = PropertyConfigBuilder.get().p("retainDays", retain);
        ctx.factory("event.elastic", "default", esCfg);
        ctx.instance("eventLog", PropertyConfigBuilder.get().p("retainDays", retain).p("retainIndexedDays", evCfg.getRetainIndexedDays()));
    }


    /**
     * @param b
     * @param ctx
     * @param storageAlias
     * @param hc
     * @return
     * @throws ServiceManagementException
     * @throws JobBuilderException
     * @throws InvalidUnitConfigurationException
     * @throws UnitInitializationFailedException
     */
    protected StorageContext getStorage ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx, HostConfiguration hc, String storageAlias,
            String oldAlias )
                    throws UnitInitializationFailedException, InvalidUnitConfigurationException, JobBuilderException, ServiceManagementException {

        Path overridePath = ctx.getServiceManager().getOverrideStoragePath(storageAlias);
        Path oldOverridePath = !StringUtils.isBlank(oldAlias) ? ctx.getServiceManager().getOverrideStoragePath(oldAlias) : null;
        UserPrincipal servicePrincipal = ctx.getServiceManager().getServicePrincipal();
        String userName = servicePrincipal != null ? servicePrincipal.getName() : null;
        if ( this.serviceStorage.checkMigrationNeeded(storageAlias, oldAlias, overridePath, oldOverridePath) ) {
            return this.serviceStorage
                    .migrateStorage(b, ctx.octx(), hc.getStorageConfiguration(), storageAlias, oldAlias, overridePath, oldOverridePath, userName);
        }

        return this.serviceStorage.ensureStorageAccess(b, ctx.octx(), hc.getStorageConfiguration(), storageAlias, overridePath, userName);
    }


    /**
     * @param b
     * @param ctx
     * @param filesStorage
     * @param localStorage
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    @SuppressWarnings ( "nls" )
    private static void setupStorage ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx, StorageContext localStorage,
            StorageContext tempStorage )
                    throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {

        ctx.instance("db.server.derby", PropertyConfigBuilder.get().p("systemHome", localStorage.getLocalStorage().resolve("db/").toString()));

        ctx.instance(
            "messaging.broker",
            PropertyConfigBuilder.get().p("dataDir", localStorage.getLocalStorage().resolve("jms/data/").toString())
                    .p("tmpDir", tempStorage.getLocalStorage().resolve("jms/tmp/").toString()));

        ctx.instance("cacheManager", PropertyConfigBuilder.get().p("cacheManager", tempStorage.getLocalStorage().resolve("cache/").toString()));

        String agentId;
        try {
            agentId = ctx.octx().platformInfo().getAgentId().toString();
        }
        catch ( SystemInformationException e ) {
            throw new JobBuilderException("Failed to get agent id", e);
        }
        ctx.instance(
            "txservice",
            PropertyConfigBuilder.get().p("logBaseDir", tempStorage.getLocalStorage().resolve("tx/").toString())
                    .p("uniqueName", "orchserver-" + agentId));

        ctx.instance(
            "resourceLibrary",
            PropertyConfigBuilder.get().p("storagePath", localStorage.getSharedStorage().resolve("resourceLibraries").toString()));
    }


    /**
     * @param b
     * @param ctx
     * @param hc
     * @param localStoragePath
     * @param filesStoragePath
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     * @throws SystemInformationException
     */
    @SuppressWarnings ( "nls" )
    private static void setupWebFrontend ( JobBuilder b, RuntimeConfigContext<OrchestratorConfiguration, OrchestratorConfigurationJob> ctx,
            HostConfiguration hc, StorageContext localStoragePath, StorageContext tempStoragePath ) throws UnitInitializationFailedException,
                    ServiceManagementException, InvalidParameterException, SystemInformationException, JobBuilderException {

        WebEndpointConfig webEndpointConfig = ctx.octx().cfg().getWebConfig().getWebEndpointConfig();
        String webKeystore = webEndpointConfig.getSslEndpointConfiguration().getKeystoreAlias();
        ctx.factory(
            "tls.mapping",
            "api-cas",
            PropertyConfigBuilder.get().p("subsystem", "api/casClient").p("hostnameVerifier", "defaultOrLocalhost")
                    .p("trustStore", "keyStore:" + webKeystore));

        ctx.factory(
            "tls.mapping",
            "web-cas",
            PropertyConfigBuilder.get().p("subsystem", "webgui/casClient").p("hostnameVerifier", "defaultOrLocalhost")
                    .p("trustStore", "keyStore:" + webKeystore));

        ctx.factory(
            "tls.mapping",
            "cas-validate",
            PropertyConfigBuilder.get().p("subsystem", "auth/casServer").p("hostnameVerifier", "defaultOrLocalhost")
                    .p("trustStore", "keyStore:" + webKeystore));

        ctx.factory(
            "tls.mapping",
            "ws-client",
            PropertyConfigBuilder.get().p("subsystem", "wsClient").p("hostnameVerifier", "defaultOrLocalhost")
                    .p("trustStore", "keyStore:" + webKeystore));

        ctx.factory(
            "tls.mapping",
            "ws-wsdl-client",
            PropertyConfigBuilder.get().p("subsystem", "wsWsdlClient").p("hostnameVerifier", "defaultOrLocalhost")
                    .p("trustStore", "keyStore:" + webKeystore));

        ctx.factory("truststore", "internal", PropertyConfigBuilder.get());

        WebConfigUtil.setupSessionManager(b, ctx, webEndpointConfig, tempStoragePath.getLocalStorage().resolve("session/"));

        WebConfigUtil.makeWebEndpointConfig(
            b,
            ctx,
            ctx.octx().cfg().getWebConfig().getApiEndpointConfig(),
            "api",
            hc.getNetworkConfiguration().getIpv6Enabled());

        WebConfigUtil.makeWebEndpointConfig(b, ctx, webEndpointConfig, "web", hc.getNetworkConfiguration().getIpv6Enabled());

        WebConfigUtil.makeSecurityHeaderConfig(b, ctx, webEndpointConfig);

        WebConfigUtil.makeWebappConfig(
            b,
            ctx,
            "webgui",
            "eu.agno3.orchestrator.server.webgui",
            "/gui",
            true,
            webEndpointConfig,
            tempStoragePath.getLocalStorage().resolve("tmp-gui/"),
            Collections.EMPTY_MAP,
            "web");

        WebConfigUtil.makeWebappConfig(
            b,
            ctx,
            "auth",
            "eu.agno3.orchestrator.server.auth.webapp",
            "/auth",
            true,
            ctx.octx().cfg().getWebConfig().getWebEndpointConfig(),
            tempStoragePath.getLocalStorage().resolve("tmp-auth/"),
            Collections.EMPTY_MAP,
            "web");
    }

}

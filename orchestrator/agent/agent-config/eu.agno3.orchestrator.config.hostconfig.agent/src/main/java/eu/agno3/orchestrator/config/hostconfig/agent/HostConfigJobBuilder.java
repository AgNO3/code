/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import javax.validation.ValidatorFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.agent.system.info.units.RefreshSystemInformation;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.agent.api.StorageContext;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.config.web.agent.RuntimeConfigUtil;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer;
import eu.agno3.orchestrator.jobs.agent.system.ConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.DebugOutputHandler;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.contents.ContentsConfigurator;
import eu.agno3.orchestrator.system.base.units.file.contents.PropertiesProvider;
import eu.agno3.orchestrator.system.base.units.file.remove.Remove;
import eu.agno3.orchestrator.system.base.units.file.touch.Touch;
import eu.agno3.orchestrator.system.base.units.service.DisableService;
import eu.agno3.orchestrator.system.base.units.service.EnableService;
import eu.agno3.orchestrator.system.base.units.service.ReloadService;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.ssh.OpenSSHKeyCodec;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    HostConfigJobBuilder.class, JobRunnableFactory.class, ConfigJobBuilder.class
}, property = "jobType=eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob" )
@JobType ( HostConfigurationJob.class )
public class HostConfigJobBuilder extends AbstractConfigJobBuilder<@NonNull HostConfiguration, HostConfigurationJob> {

    private static final Logger log = Logger.getLogger(HostConfigJobBuilder.class);

    private static final double MAX_DIRTY_BCKG_BYTES_RATIO = 0.05;
    private static final double MAX_DIRTY_BYTES_RATIO = 0.1;
    private static final int MAX_MAX_DIRTY_BCKG_BYTES = 128 * 1024 * 1024;
    private static final int MAX_MAX_DIRTY_BYTES = 256 * 1024 * 1024;

    /**
     * 
     */
    static final String WORLD_READABLE_CONFIG = "rw-r--r--"; //$NON-NLS-1$

    private static final String RESOLV_CONF = "/etc/resolvconf/resolv.conf.d/base"; //$NON-NLS-1$
    private static final String LWRES_FORWARDERS_CONF = "/etc/resolvconf/resolv.conf.d/base.lwresd"; //$NON-NLS-1$
    private static final String ETC_HOSTS = "/etc/hosts.agno3"; //$NON-NLS-1$
    private static final String ETC_NTP_CONF = "/etc/ntp.conf.agno3"; //$NON-NLS-1$
    private static final String ETC_SYSCTL_CONF = "/etc/sysctl.d/50-appliance.conf"; //$NON-NLS-1$
    private static final String SSHD_CONFIG = "/etc/ssh/sshd_config"; //$NON-NLS-1$

    private static final String LWRESD = "lwresd"; //$NON-NLS-1$
    private static final String NTP = "ntp"; //$NON-NLS-1$
    private static final String SSH = "ssh"; //$NON-NLS-1$

    private static final String SBIN_RESOLVCONF = "/sbin/resolvconf"; //$NON-NLS-1$

    private static final String AGENT_LOGSINK_CONF = "/etc/agent/journalsink.conf"; //$NON-NLS-1$
    private static final String AGENT_LICENSE_CONF = "/etc/agent/license.conf"; //$NON-NLS-1$
    private static final String AGENT_BACKUP_CONF = "/etc/agent/backup.conf"; //$NON-NLS-1$
    private static final String AGENT_LOGGING_CONF = "/etc/agent/log.conf"; //$NON-NLS-1$

    private static final String ADMIN_AUTHORIZED_KEYS = "/var/lib/admin/.ssh/authorized_keys"; //$NON-NLS-1$

    private BaseSystemIntegration baseSys;
    private CryptoConfigJobBuilder cryptoConfigJobBuilder;
    private RealmConfigJobBuilder realmConfigJobBuilder;
    private StorageConfigJobBuilder storageConfigJobBuilder;

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


    @Reference
    protected synchronized void setBaseSystemIntegration ( BaseSystemIntegration sysint ) {
        this.baseSys = sysint;
    }


    protected synchronized void unsetBaseSystemIntegration ( BaseSystemIntegration sysint ) {
        if ( this.baseSys == sysint ) {
            this.baseSys = null;
        }
    }


    @Reference
    protected void setCryptoConfigJobBuilder ( CryptoConfigJobBuilder ccjb ) {
        this.cryptoConfigJobBuilder = ccjb;
    }


    protected void unsetCryptoConfigJobBuilder ( CryptoConfigJobBuilder ccjb ) {
        if ( this.cryptoConfigJobBuilder == ccjb ) {
            this.cryptoConfigJobBuilder = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#setObjectTypeRegistry(eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry)
     */
    @Reference
    @Override
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
     * @param realmConfigJobBuilder
     *            the realmConfigJobBuilder to set
     */
    @Reference
    protected void setRealmConfigJobBuilder ( RealmConfigJobBuilder rcjb ) {
        this.realmConfigJobBuilder = rcjb;
    }


    protected void unsetRealmConfigJobBuilder ( RealmConfigJobBuilder rcjb ) {
        if ( this.realmConfigJobBuilder == rcjb ) {
            this.realmConfigJobBuilder = null;
        }
    }


    @Reference
    protected void setStorageConfigJobBuilder ( StorageConfigJobBuilder scjb ) {
        this.storageConfigJobBuilder = scjb;
    }


    protected void unsetStorageConfigJobBuilder ( StorageConfigJobBuilder scjb ) {
        if ( this.storageConfigJobBuilder == scjb ) {
            this.storageConfigJobBuilder = null;
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setServiceManager(eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
    @Reference
    @Override
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
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigClass()
     */
    @SuppressWarnings ( "null" )
    @Override
    protected @NonNull Class<@NonNull HostConfiguration> getConfigClass () {
        return HostConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getServiceType()
     */
    @Override
    protected @NonNull String getServiceType () {
        return HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnitInitializationFailedException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigFromJob(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected @NonNull HostConfiguration getConfigFromJob ( @NonNull HostConfigurationJob j ) throws UnitInitializationFailedException {
        HostConfiguration config = j.getHostConfig();

        if ( config == null ) {
            throw new UnitInitializationFailedException("Config is null"); //$NON-NLS-1$
        }
        return config;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getJobName(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected String getJobName ( HostConfigurationJob j ) {
        return "Reconfiguring base system"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobBuilderException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#buildConfigJob(eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext)
     */
    @Override
    protected void buildConfigJob ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws Exception {
        configureHostId(b, ctx);
        configureSysctl(b, ctx);
        configureDateTime(b, ctx);
        NetworkConfigJobBuilder.build(b, ctx);
        configureResolver(b, ctx);
        this.cryptoConfigJobBuilder.build(b, ctx);
        this.realmConfigJobBuilder.build(b, ctx);
        this.storageConfigJobBuilder.configureStorage(b, ctx);
        configureAgent(b, ctx);
        MailingConfigJobBuilder.configureMailing(b, ctx);
        configureSystem(b, ctx);

        b.add(RefreshSystemInformation.class).ignoreErrors();
    }


    /**
     * @param b
     * @param ctx
     * @throws UnitInitializationFailedException
     * @throws JobBuilderException
     * @throws InvalidUnitConfigurationException
     * @throws ServiceManagementException
     * @throws SystemInformationException
     */
    private void configureAgent ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws UnitInitializationFailedException, InvalidUnitConfigurationException, JobBuilderException, ServiceManagementException,
            SystemInformationException {
        b.add(Contents.class).file(AGENT_LOGSINK_CONF).content(new PropertiesProvider(new Properties()));
        b.add(Touch.class).file(AGENT_LICENSE_CONF);
        Properties backupProperties = new Properties();

        StorageConfiguration cfg = ctx.cfg().getStorageConfiguration();
        StorageContext backupCtx = getStorageContext(
            b,
            ctx,
            cfg,
            cfg.getBackupStorage(),
            !ctx.cur().isPresent() ? null : ctx.cur().get().getStorageConfiguration().getBackupStorage());
        backupProperties.put(
            "backupPath", //$NON-NLS-1$
            backupCtx.getLocalStorage().resolve("backups")); //$NON-NLS-1$
        b.add(Contents.class).file(AGENT_BACKUP_CONF).content(new PropertiesProvider(backupProperties));

        RuntimeConfiguration agentConfig = ctx.cfg().getSystemConfiguration().getAgentConfig();
        setupLogging(b, agentConfig);

        this.runtimeConfigBuilder.setupRuntimeConfig(
            b,
            ctx,
            agentConfig,
            160,
            320,
            0.05f,
            FileSystems.getDefault().getPath("/opt/agno3/agent/orchagent.config.local"), //$NON-NLS-1$
            Collections.EMPTY_LIST,
            Collections.EMPTY_MAP,
            null);
    }


    /**
     * @param b
     * @param agentConfig
     * @throws UnitInitializationFailedException
     */
    void setupLogging ( JobBuilder b, RuntimeConfiguration agentConfig ) throws UnitInitializationFailedException {
        Properties logProps = new Properties();
        if ( agentConfig.getDebugPackages() != null ) {
            for ( String dbg : agentConfig.getDebugPackages() ) {
                logProps.put(dbg, "DEBUG"); //$NON-NLS-1$
            }
        }
        if ( agentConfig.getTracePackages() != null ) {
            for ( String dbg : agentConfig.getTracePackages() ) {
                logProps.put(dbg, "TRACE"); //$NON-NLS-1$
            }
        }
        b.add(Contents.class).file(AGENT_LOGGING_CONF).content(new PropertiesProvider(logProps));
    }


    private StorageContext getStorageContext ( @NonNull JobBuilder b,
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, StorageConfiguration cfg, String storageAlias,
            String oldAlias )
                    throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException, ServiceManagementException {

        BaseServiceManager sm = ctx.sctx().getServiceManager(BaseServiceManager.class, this.getServiceManager());
        Path overridePath = sm.getOverrideStoragePath(storageAlias);
        Path oldOverridePath = !StringUtils.isBlank(oldAlias) ? sm.getOverrideStoragePath(oldAlias) : null;
        if ( this.storageConfigJobBuilder.checkMigrationNeeded(storageAlias, oldAlias, overridePath, oldOverridePath) ) {
            return this.storageConfigJobBuilder.migrateStorage(b, ctx, cfg, storageAlias, oldAlias, overridePath, oldOverridePath, null);
        }

        return this.storageConfigJobBuilder.ensureStorageAccess(b, ctx, cfg, storageAlias, overridePath, null);
    }


    /**
     * @param b
     * @param ctx
     * @throws MatcherException
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws SystemInformationException
     */
    protected void configureSysctl ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws MatcherException, InvalidParameterException, UnitInitializationFailedException, IOException, SystemInformationException {

        boolean changedSysctlSettings = ctx.changed(ctx.match().getSystemConfiguration().getSwapiness());

        b.add(Contents.class).file(ETC_SYSCTL_CONF).content(ctx.tpl(ETC_SYSCTL_CONF, makeSysctlExtraContext(ctx))).perms(WORLD_READABLE_CONFIG)
                .runIf(changedSysctlSettings);

        b.add(Exec.class).cmd("/sbin/sysctl") //$NON-NLS-1$
                .args("--system") //$NON-NLS-1$
                .stdout(new DebugOutputHandler(log)).runIf(changedSysctlSettings);
    }


    /**
     * @param ctx
     * @return
     * @throws SystemInformationException
     */
    private static Map<String, Serializable> makeSysctlExtraContext (
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) throws SystemInformationException {
        Map<String, Serializable> res = new HashMap<>();
        PlatformInformation platformInfo = ctx.platformInfo();
        long totalPhysMem = platformInfo.getMemoryInformation().getTotalPhysicalMemory();
        long maxDirtyBytes = Math.max(MAX_MAX_DIRTY_BYTES, (long) ( totalPhysMem * MAX_DIRTY_BYTES_RATIO ));
        long maxDirtyBackgroundBytes = Math.max(MAX_MAX_DIRTY_BCKG_BYTES, (long) ( totalPhysMem * MAX_DIRTY_BCKG_BYTES_RATIO ));
        res.put("vmmaxDirtyBytes", maxDirtyBytes); //$NON-NLS-1$
        res.put("vmmaxDirtyBackgroundBytes", maxDirtyBackgroundBytes); //$NON-NLS-1$
        return res;
    }


    /**
     * @param b
     * @param ctx
     * @throws MatcherException
     * @throws UnitInitializationFailedException
     * @throws IOException
     * @throws JobBuilderException
     * @throws InvalidUnitConfigurationException
     */
    protected void configureSystem ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws MatcherException, UnitInitializationFailedException, IOException, JobBuilderException, InvalidUnitConfigurationException {

        if ( ctx.cfg().getSystemConfiguration().getEnableSshAccess() ) {
            b.add(EnableService.class).service(SSH);
            b.add(Remove.class).file("/etc/ssh/sshd_not_to_be_run"); //$NON-NLS-1$
        }
        else {
            b.add(DisableService.class).service(SSH);
            b.add(Touch.class).file("/etc/ssh/sshd_not_to_be_run"); //$NON-NLS-1$
        }

        // b.add(DisableService.class).service("bootstrap-nginx"); //$NON-NLS-1$

        boolean changedSSHSettings = ctx.changed(ctx.match().getSystemConfiguration().getSshKeyOnly());
        b.add(Contents.class).file(SSHD_CONFIG).content(ctx.tpl(SSHD_CONFIG)).perms(WORLD_READABLE_CONFIG).runIf(changedSSHSettings);
        b.add(ReloadService.class).service(SSH).runIf(changedSSHSettings);
        configureAdminAuthorizedKeys(b, ctx);
    }


    /**
     * @param b
     * @param ctx
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws InvalidUnitConfigurationException
     */
    private static void configureAdminAuthorizedKeys ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws JobBuilderException, UnitInitializationFailedException, InvalidUnitConfigurationException {
        StringBuilder akb = new StringBuilder();
        for ( PublicKeyEntry pke : ctx.cfg().getSystemConfiguration().getAdminSshPublicKeys() ) {
            try {
                akb.append(OpenSSHKeyCodec.getKeyType(pke.getPublicKey()));
                akb.append(' ');
                akb.append(Base64.encodeBase64String(OpenSSHKeyCodec.encodePublic(pke.getPublicKey())));
                akb.append(' ');
                if ( !StringUtils.isBlank(pke.getComment()) ) {
                    akb.append(pke.getComment());
                }
                akb.append('\n');
            }
            catch ( CryptoException e ) {
                throw new JobBuilderException("Invalid public key", e); //$NON-NLS-1$
            }
        }

        byte[] authKeyData = akb.toString().getBytes(StandardCharsets.US_ASCII);
        ContentsConfigurator authKey = b.add(Contents.class).file(ADMIN_AUTHORIZED_KEYS).content(authKeyData)
                .perms(FileSecurityUtils.getOwnerOnlyFilePermissions()); // $NON-NLS-1$
        if ( FileSecurityUtils.isRunningAsRoot() ) {
            authKey.owner("admin"); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @param force
     * @param resolverConfiguration
     * @param cur
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws IOException
     * @throws BaseSystemException
     * @throws MatcherException
     */
    protected void configureResolver ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, IOException, BaseSystemException, MatcherException {

        boolean changedNameservers = ctx.changed(ctx.match().getResolverConfiguration().getNameservers());

        // write base fallback resolver config
        b.add(Contents.class).file(RESOLV_CONF).content(ctx.tpl(RESOLV_CONF)).perms(WORLD_READABLE_CONFIG).runIf(changedNameservers);

        b.add(Contents.class).file(LWRES_FORWARDERS_CONF).content(ctx.tpl(LWRES_FORWARDERS_CONF)).perms(WORLD_READABLE_CONFIG)
                .runIf(changedNameservers);

        // remove stale dhcp resolveconf files
        if ( !ctx.cfg().getResolverConfiguration().getAutoconfigureDns() ) {
            cleanResolvconf(b, ctx);
        }

        // update resolver configs
        b.add(Exec.class).cmd(SBIN_RESOLVCONF).args("-u"); //$NON-NLS-1$

        // reload resolver daemon
        b.add(ReloadService.class).service(this.baseSys.mapServiceName(LWRESD)).runIf(changedNameservers);

    }


    private static void cleanResolvconf ( @NonNull JobBuilder b,
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) {
        Path intfConfig = Paths.get("/run/resolvconf/interface/"); //$NON-NLS-1$

        if ( !Files.isDirectory(intfConfig) ) {
            return;
        }

        try ( Stream<Path> list = Files.list(intfConfig) ) {
            list.filter(x -> {
                return x.getFileName().toString().endsWith(".dhclient"); //$NON-NLS-1$
            }).forEach(x -> {
                try {
                    b.add(Exec.class).cmd(SBIN_RESOLVCONF).args("-d", x.getFileName().toString()); //$NON-NLS-1$
                    b.add(Remove.class).file(x);
                }
                catch ( Exception e ) {
                    log.warn("Failed to remove dhcp nameservice config file " + x, e); //$NON-NLS-1$
                }
            });
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate dhcp nameservice config files", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param b
     * @param force
     * @param nc
     * @param cur
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws BaseSystemException
     * @throws MatcherException
     */
    protected void configureHostId ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, IOException, BaseSystemException, MatcherException {

        boolean changed = ctx.changed(ctx.match().getHostIdentification().getHostName())
                || ctx.changed(ctx.match().getHostIdentification().getDomainName())
                || ctx.changed(ctx.match().getNetworkConfiguration().getIpv6Enabled());
        b.add(Contents.class).file(ETC_HOSTS).content(ctx.tpl(ETC_HOSTS)).perms(WORLD_READABLE_CONFIG).runIf(changed);

        this.baseSys.setHostName(b, ctx);
    }


    /**
     * @param b
     * @param force
     * @param nc
     * @param cur
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws BaseSystemException
     * @throws MatcherException
     */
    void configureDateTime ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, IOException, BaseSystemException, MatcherException {

        this.baseSys.setTimezone(b, ctx);
        this.baseSys.setHwClockUTC(b, ctx);

        if ( ctx.cfg().getDateTimeConfiguration().getNtpEnabled() ) {
            b.add(Contents.class).file(ETC_NTP_CONF).content(ctx.tpl(ETC_NTP_CONF)).perms(WORLD_READABLE_CONFIG)
                    .runIf(ctx.changed(ctx.match().getDateTimeConfiguration().getNtpServers()));
        }

        if ( ctx.changed(ctx.match().getDateTimeConfiguration().getNtpEnabled()) ) {
            String ntpService = this.baseSys.mapServiceName(NTP);
            if ( ctx.cfg().getDateTimeConfiguration().getNtpEnabled() ) {
                b.add(EnableService.class).service(ntpService);
            }
            else {
                b.add(DisableService.class).service(ntpService);
            }
        }

    }

}

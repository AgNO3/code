/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.fileshare.orch.agent;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.common.config.FileshareAuthConfig;
import eu.agno3.fileshare.orch.common.config.FileshareCIFSPassthroughGroup;
import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareContentConfig;
import eu.agno3.fileshare.orch.common.config.FileshareContentPreviewConfig;
import eu.agno3.fileshare.orch.common.config.FileshareContentScanConfig;
import eu.agno3.fileshare.orch.common.config.FileshareContentSearchConfig;
import eu.agno3.fileshare.orch.common.config.FileshareLoggerConfig;
import eu.agno3.fileshare.orch.common.config.FileshareNotificationConfig;
import eu.agno3.fileshare.orch.common.config.FilesharePassthroughGroup;
import eu.agno3.fileshare.orch.common.config.FileshareQuotaRule;
import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicy;
import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicyConfig;
import eu.agno3.fileshare.orch.common.config.FileshareUserConfig;
import eu.agno3.fileshare.orch.common.config.FileshareUserLabelRule;
import eu.agno3.fileshare.orch.common.config.FileshareUserQuotaConfig;
import eu.agno3.fileshare.orch.common.config.FileshareUserSelfServiceConfig;
import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevel;
import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevelConfig;
import eu.agno3.fileshare.orch.common.config.FileshareWebConfig;
import eu.agno3.fileshare.orch.common.config.GrantType;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.orch.common.jobs.FileshareConfigurationJob;
import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureGeneratedKey;
import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureKeystore;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptions;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.agent.api.SMTPConfigurator;
import eu.agno3.orchestrator.config.hostconfig.agent.api.ServiceStorageUtil;
import eu.agno3.orchestrator.config.hostconfig.agent.api.StorageContext;
import eu.agno3.orchestrator.config.logger.IPLogAnonymizationType;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.terms.TermsApplyType;
import eu.agno3.orchestrator.config.terms.TermsConfiguration;
import eu.agno3.orchestrator.config.terms.TermsDefinition;
import eu.agno3.orchestrator.config.web.ICAPConfiguration;
import eu.agno3.orchestrator.config.web.LDAPServerType;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.config.web.agent.ICAPConfigUtil;
import eu.agno3.orchestrator.config.web.agent.LDAPConfigUtil;
import eu.agno3.orchestrator.config.web.agent.RuntimeConfigUtil;
import eu.agno3.orchestrator.config.web.agent.WebConfigUtil;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.monitor.units.ServiceCheck;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.units.ClearConfigFiles;
import eu.agno3.orchestrator.jobs.agent.service.units.ReloadServiceConfig;
import eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer;
import eu.agno3.orchestrator.jobs.agent.system.ConfigJobBuilder;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.GroupJobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.service.EnableService;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    FileshareConfigJobBuilder.class, JobRunnableFactory.class, ConfigJobBuilder.class
}, property = "jobType=eu.agno3.fileshare.orch.common.jobs.FileshareConfigurationJob" )
@JobType ( FileshareConfigurationJob.class )
public class FileshareConfigJobBuilder extends AbstractRuntimeConfigJobBuilder<FileshareConfiguration, FileshareConfigurationJob> {

    private static final String FILESHARE = "fileshare"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileshareConfigJobBuilder.class);

    private AuthenticatorConfigurator authenticatorConfigurator;

    private SMTPConfigurator smtpConfigurator;

    private ServiceStorageUtil serviceStorage;

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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder#setConfigFilesManagerFactory(eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory)
     */
    @Override
    @Reference
    protected synchronized void setConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        super.setConfigFilesManagerFactory(cfmf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder#unsetConfigFilesManagerFactory(eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory)
     */
    @Override
    protected synchronized void unsetConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        super.unsetConfigFilesManagerFactory(cfmf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder#setServiceManager(eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
    @Override
    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        super.setServiceManager(sm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractRuntimeConfigJobBuilder#unsetServiceManager(eu.agno3.orchestrator.jobs.agent.service.ServiceManager)
     */
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


    @Reference
    protected synchronized void setSMTPConfigurator ( SMTPConfigurator sc ) {
        this.smtpConfigurator = sc;
    }


    protected synchronized void unsetSMTPConfigurator ( SMTPConfigurator sc ) {
        if ( this.smtpConfigurator == sc ) {
            this.smtpConfigurator = null;
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


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigClass()
     */
    @Override
    protected @NonNull Class<FileshareConfiguration> getConfigClass () {
        return FileshareConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnitInitializationFailedException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getConfigFromJob(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected @NonNull FileshareConfiguration getConfigFromJob ( @NonNull FileshareConfigurationJob j ) throws UnitInitializationFailedException {
        FileshareConfiguration fileshareConfig = j.getFileshareConfig();
        if ( fileshareConfig == null ) {
            throw new UnitInitializationFailedException("Fileshare config is null"); //$NON-NLS-1$
        }
        return fileshareConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getServiceType()
     */
    @Override
    protected @NonNull String getServiceType () {
        return FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#getJobName(eu.agno3.orchestrator.config.model.base.config.jobs.ConfigurationJob)
     */
    @Override
    protected String getJobName ( FileshareConfigurationJob j ) {
        return "Reconfiguring fileshare server"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobBuilderException
     * 
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractConfigJobBuilder#buildConfigJob(eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext)
     */
    @Override
    protected void buildConfigJob ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws Exception {

        @NonNull
        GroupJobBuilder cfgGroup = b.beginGroup();
        @NonNull
        RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> gctx = ctx.withBuilder(cfgGroup);

        ensureKeystores(cfgGroup, gctx);
        HostConfiguration hc = ctx.octx().sctx().getContextService(HostConfiguration.class);
        StorageContext localStorage = getStorage(
            cfgGroup,
            gctx,
            hc,
            ctx.octx().cfg().getStorageConfiguration().getLocalStorage(),
            !ctx.octx().cur().isPresent() ? null : ctx.octx().cur().get().getStorageConfiguration().getLocalStorage());
        StorageContext filesStorage = getStorage(
            cfgGroup,
            gctx,
            hc,
            ctx.octx().cfg().getStorageConfiguration().getFileStorage(),
            !ctx.octx().cur().isPresent() ? null : ctx.octx().cur().get().getStorageConfiguration().getFileStorage());
        setupStorage(cfgGroup, gctx, localStorage, filesStorage);
        setupPassthroughGroups(cfgGroup, gctx);
        setupWebFrontend(cfgGroup, gctx, hc, localStorage, filesStorage);
        setupContent(cfgGroup, gctx);
        setupUser(cfgGroup, gctx);
        setupTerms(cfgGroup, gctx);
        setupAuth(cfgGroup, gctx);
        setupNotification(cfgGroup, gctx);
        setupSecurityPolicies(cfgGroup, gctx);
        setupEventLogConfig(cfgGroup, gctx, localStorage);

        this.kerberosConfigUtil.writeKerberosConfig(gctx);

        boolean needRestart = false;
        needRestart |= setupRuntimeConfig(cfgGroup, gctx);

        Set<String> modifiedPids = gctx.complete();

        if ( ctx.octx().preventRestart() ) {
            log.info("Not restarting service"); //$NON-NLS-1$
            cfgGroup.after(EnableService.class).service(ctx.getServiceManager().getSystemServiceName());
        }
        else if ( needRestart ) {
            log.info("Restarting service"); //$NON-NLS-1$
        }
        else {
            log.info("Reload " + modifiedPids); //$NON-NLS-1$
        }

        cfgGroup.after(ReloadServiceConfig.class).service(ctx.octx().job().getService()).modified(modifiedPids)
                .forceRestart(!ctx.octx().preventRestart() && ( needRestart || ctx.octx().job().getApplyInfo().isForce() ))
                .runIf(!ctx.octx().preventRestart() && ( !modifiedPids.isEmpty() || needRestart ));
        cfgGroup.after(ServiceCheck.class).service(StructuralObjectReferenceImpl.fromObject(ctx.octx().job().getService()))
                .timeout(Duration.standardMinutes(10)).skipIf(ctx.octx().preventRestart());

    }


    /**
     * @param cfgGroup
     * @param gctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    @SuppressWarnings ( "nls" )
    private void setupPassthroughGroups ( @NonNull GroupJobBuilder cfgGroup,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> gctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {

        Set<FilesharePassthroughGroup> passthroughGroups = gctx.octx().cfg().getStorageConfiguration().getPassthroughGroups();
        for ( FilesharePassthroughGroup g : passthroughGroups ) {
            if ( g instanceof FileshareCIFSPassthroughGroup ) {
                FileshareCIFSPassthroughGroup cvg = (FileshareCIFSPassthroughGroup) g;

                PropertyConfigBuilder cfg = PropertyConfigBuilder.get();
                try {
                    cfg.p("root", uncToURI(cvg.getUncPath()));
                }
                catch ( URISyntaxException e ) {
                    throw new InvalidParameterException("Invalid CIFS URL", e);
                }
                cfg.p("securityLabel", cvg.getSecurityPolicy());

                switch ( cvg.getAuthType() ) {
                case KERBEROS:
                    setupCIFSKerberosAuth(cfgGroup, gctx, cfg, cvg);
                    break;
                case NTLMSSP:
                case NTLM:
                case NTLMv2:
                    cfg.p("user", cvg.getUsername());
                    cfg.p("domain", cvg.getDomain());
                    cfg.p("password", cvg.getPassword());
                case GUEST:
                    break;
                }

                cfg.p("sharable", g.getAllowSharing());
                cfg.p("enforceSignatures", cvg.getEnableSigning());
                cfg.p("allowSMB1", cvg.getAllowSMB1());
                cfg.p("disableSMB2", cvg.getDisableSMB2());
                gctx.factory("vfs.smb", cvg.getGroupName(), cfg);
            }
        }
    }


    /**
     * @param gctx
     * @param cfg
     * @param cvg
     * @throws JobBuilderException
     */
    @SuppressWarnings ( "nls" )
    private void setupCIFSKerberosAuth ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> gctx,
            PropertyConfigBuilder cfg, FileshareCIFSPassthroughGroup cvg ) throws JobBuilderException {

        String princ;
        try {
            princ = this.kerberosConfigUtil
                    .ensureInitiatorCredentials(b, gctx, this.kerberosConfigUtil.getRealmManager(cvg.getAuthRealm()), cvg.getAuthKeytab(), null);
        }
        catch (
            UnitInitializationFailedException |
            ServiceManagementException |
            JobBuilderException |
            ADException |
            KerberosException |
            UnixAccountException e ) {
            throw new JobBuilderException("Failed to configure virtual group kerberos", e);
        }

        cfg.p("useRealm", true);
        cfg.p("Realm.target", FilterBuilder.get().eq("instanceId", cvg.getAuthRealm()).toString());
        cfg.p("useKerberos", true);
        cfg.p("keytab", cvg.getAuthKeytab());
        cfg.p("user", princ);
    }


    /**
     * @param uncPath
     * @return
     * @throws InvalidParameterException
     * @throws URISyntaxException
     */
    private static URI uncToURI ( String uncPath ) throws InvalidParameterException, URISyntaxException {

        if ( !uncPath.startsWith("\\") ) { //$NON-NLS-1$
            throw new InvalidParameterException("UNC path must start with \\"); //$NON-NLS-1$
        }

        int nextSep = uncPath.indexOf('\\', 2);

        if ( nextSep < 0 ) {
            throw new InvalidParameterException("UNC path must contain a share specification"); //$NON-NLS-1$
        }

        String hostSpec = uncPath.substring(2, nextSep);
        String pathSpec = '/' + uncPath.substring(nextSep + 1);
        pathSpec = pathSpec.replace('\\', '/');

        // TODO: check whether we need to strip [] for v6
        // port specifications are not really allowed by the spec
        int port = -1;
        String host = hostSpec;
        return new URI("smb", null, host, port, pathSpec, null, null); //$NON-NLS-1$
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
    private boolean setupRuntimeConfig ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws MatcherException, UnitInitializationFailedException, ServiceManagementException, InvalidParameterException,
            SystemInformationException {

        RuntimeConfiguration runtimeConfig = ctx.octx().cfg().getAdvancedConfiguration().getRuntimeConfiguration();
        this.runtimeConfigBuilder.configureDebugging(ctx, runtimeConfig);

        ctx.instance("license", PropertyConfigBuilder.get()); //$NON-NLS-1$
        if ( ctx.octx().changed(ctx.octx().match().getAdvancedConfiguration().getRuntimeConfiguration().getMemoryLimit()) ) {
            return this.runtimeConfigBuilder.setupRuntimeConfig(
                b,
                ctx.octx(),
                runtimeConfig,
                384,
                -1,
                0.6f,
                FileSystems.getDefault().getPath("/opt/agno3/fileshare/fileshare.config.local"), //$NON-NLS-1$
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
     */
    @SuppressWarnings ( "nls" )
    private static void setupEventLogConfig ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, StorageContext localStorage )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        String[] userIpFields = new String[] {
            "field.remoteAddr", "field.properties.remoteAddr"
        };

        FileshareLoggerConfig lc = ctx.octx().cfg().getLoggerConfiguration();

        int defaultLogRetention = lc.getDefaultLoggerConfig().getRetentionDays();
        int preauthLogRetention = lc.getUnauthLoggerConfig().getRetentionDays();
        IPLogAnonymizationType defaultIpAnon = lc.getDefaultLoggerConfig().getIpAnonymizationType();
        IPLogAnonymizationType preauthIpAnon = lc.getUnauthLoggerConfig().getIpAnonymizationType();

        PropertyConfigBuilder defaultAnon = PropertyConfigBuilder.get();
        setupIpRedact(defaultAnon, defaultIpAnon, userIpFields);
        ctx.factory("event.anonymize", "default", defaultAnon);

        String fileLogPath = localStorage.getLocalStorage().resolve("audit/").toString();

        PropertyConfigBuilder defaultLog = PropertyConfigBuilder.get();
        defaultLog.p("logPath", fileLogPath);
        defaultLog.p("logName", "eventlog");
        defaultLog.p("excludeStreams", "preauth");
        defaultLog.p("retainDays", defaultLogRetention);

        ctx.factory("event.filelog", "default", defaultLog);

        PropertyConfigBuilder preauthAnon = PropertyConfigBuilder.get();
        preauthAnon.p("matchStream", "preauth");
        setupIpRedact(preauthAnon, preauthIpAnon, userIpFields);
        ctx.factory("event.anonymize", "preauth", preauthAnon);

        PropertyConfigBuilder preauthLog = PropertyConfigBuilder.get();
        preauthLog.p("logPath", fileLogPath);
        preauthLog.p("logName", "preauth");
        preauthLog.p("includeStreams", "preauth");
        preauthLog.p("retainDays", preauthLogRetention);

        ctx.factory("event.filelog", "preauth", preauthLog);

        PropertyConfigBuilder esLog = PropertyConfigBuilder.get();
        esLog.p("excludeStreams", "preauth");
        esLog.p("retainDays", 90);
        ctx.factory("event.elastic", "default", esLog);
    }


    /**
     * @param cfg
     * @param field
     * @param anonType
     */
    private static void setupIpRedact ( PropertyConfigBuilder cfg, IPLogAnonymizationType anonType, String... fields ) {
        for ( String field : fields ) {
            switch ( anonType ) {
            case MASK:
                cfg.p(field, "ip"); //$NON-NLS-1$
                break;
            case NONE:
                break;
            default:
            case REDACT:
                cfg.p(field, "redact"); //$NON-NLS-1$
                break;
            }
        }
    }


    /**
     * @param cfgGroup
     * @param gctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private static void setupTerms ( @NonNull GroupJobBuilder cfgGroup,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        TermsConfiguration tc = ctx.octx().cfg().getUserConfiguration().getTermsConfig();

        if ( !tc.getTerms().isEmpty() ) {
            ctx.factory("auth.terms", "TERMS", PropertyConfigBuilder.get().p("after", "*"));

            for ( TermsDefinition td : tc.getTerms() ) {
                PropertyConfigBuilder p = configureTermsDefinition(td);
                ctx.factory("terms", td.getTermsId(), p);
            }

        }
    }


    /**
     * @param td
     * @return
     */
    @SuppressWarnings ( "nls" )
    static PropertyConfigBuilder configureTermsDefinition ( TermsDefinition td ) {
        PropertyConfigBuilder p = PropertyConfigBuilder.get();

        p.p("priority", td.getPriority());
        p.p("persistAcceptance", td.getPersistAcceptance());

        TermsApplyType at = td.getApplyType();

        switch ( at ) {
        case EXCLUDE:
            p.p("excludeUnauth", true);
            break;
        case ONLY:
            p.p("onlyUnauth", true);
            break;
        case ALL:
        default:
            break;
        }

        if ( at == TermsApplyType.ALL || at == TermsApplyType.EXCLUDE ) {
            p.p("excludeRoles", td.getExcludeRoles());
            p.p("includeRoles", td.getIncludeRoles());
        }

        for ( Entry<Locale, String> e : td.getTitles().entrySet() ) {
            if ( Locale.ROOT.equals(e.getKey()) ) {
                p.p("label", e.getValue());
            }
            else {
                p.p("label_" + e.getKey().toLanguageTag(), e.getValue());
            }
        }

        for ( Entry<Locale, String> e : td.getDescriptions().entrySet() ) {
            if ( Locale.ROOT.equals(e.getKey()) ) {
                p.p("description", e.getValue());
            }
            else {
                p.p("description_" + e.getKey().toLanguageTag(), e.getValue());
            }
        }
        if ( td.getUpdated() != null ) {
            p.p("lastModified", td.getUpdated().getMillis());
        }
        return p;
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
    private StorageContext getStorage ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx,
            HostConfiguration hc, String storageAlias, String oldAlias )
            throws UnitInitializationFailedException, InvalidUnitConfigurationException, JobBuilderException, ServiceManagementException {

        UserPrincipal servicePrincipal = ctx.getServiceManager().getServicePrincipal();
        Path overridePath = ctx.getServiceManager().getOverrideStoragePath(storageAlias);
        Path oldOverridePath = !StringUtils.isBlank(oldAlias) ? ctx.getServiceManager().getOverrideStoragePath(oldAlias) : null;
        String userName = servicePrincipal != null ? servicePrincipal.getName() : null;
        if ( this.serviceStorage.checkMigrationNeeded(storageAlias, oldAlias, overridePath, oldOverridePath) ) {
            return this.serviceStorage
                    .migrateStorage(b, ctx.octx(), hc.getStorageConfiguration(), storageAlias, oldAlias, overridePath, oldOverridePath, userName);
        }

        StorageContext storageCtx = this.serviceStorage
                .ensureStorageAccess(b, ctx.octx(), hc.getStorageConfiguration(), storageAlias, overridePath, userName);
        if ( storageCtx == null ) {
            if ( overridePath != null ) {
                return new StorageContext(null, overridePath, overridePath, overridePath);
            }
        }

        return storageCtx;
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
    private static void setupStorage ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx,
            StorageContext localStorage, StorageContext filesStorage )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {

        ctx.instance("db.server.derby", PropertyConfigBuilder.get().p("systemHome", localStorage.getLocalStorage().resolve("db/").toString()));

        String agentId;
        try {
            agentId = ctx.octx().platformInfo().getAgentId().toString();
        }
        catch ( SystemInformationException e ) {
            throw new JobBuilderException("Failed to get agent id", e);
        }
        ctx.instance(
            "txservice",
            PropertyConfigBuilder.get().p("logBaseDir", localStorage.getLocalStorage().resolve("tx/").toString())
                    .p("uniqueName", "fileshare-" + agentId).p("maxActive", 2048));

        ctx.instance(
            "es.node",
            PropertyConfigBuilder.get().p("es.path.data", localStorage.getLocalStorage().resolve("es/").toString()).p("es.node.name", agentId)
                    .p("es.cluster.name", "fileshare-eventlog"));

        ctx.instance("db.pool.plain", PropertyConfigBuilder.get().p("poolSize", 2048));

        PropertyConfigBuilder p = PropertyConfigBuilder.get().p("storagePath", filesStorage.getSharedStorage().resolve("files/").toString())
                .p("tmpPath", filesStorage.getSharedStorage().resolve("tmpfiles/").toString());
        if ( filesStorage.getStorageGroup() != null ) {
            p.p("storageGroup", filesStorage.getStorageGroup());
        }
        ctx.instance("blockstore", p);

    }


    /**
     * @param ctx
     * @param wc
     * @param filesStorage
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    static void configureChunkService ( RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareWebConfig wc,
            StorageContext filesStorage ) throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder p = PropertyConfigBuilder.get().p("chunksPath", filesStorage.getSharedStorage().resolve("tmp-chunks/").toString());
        if ( filesStorage.getStorageGroup() != null ) {
            p.p("storageGroup", filesStorage.getStorageGroup());
        }
        ctx.instance("chunks.file", p);

        p = PropertyConfigBuilder.get();
        if ( wc.getDefaultUploadChunkSize() != null ) {
            p.p("defaultChunkSize", wc.getDefaultUploadChunkSize());
        }
        if ( wc.getMaximumUploadChunkSize() != null ) {
            p.p("maxChunkSize", wc.getMaximumUploadChunkSize());
        }
        if ( wc.getOptimalUploadChunkCount() != null ) {
            p.p("optimalChunkCount", wc.getOptimalUploadChunkCount());
        }

        ctx.instance("chunks", p);
    }


    /**
     * @param b
     * @param ctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private static void setupSecurityPolicies ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        FileshareSecurityPolicyConfig pols = ctx.octx().cfg().getSecurityPolicyConfiguration();
        Map<String, String> userLabelRules = new LinkedHashMap<>();
        for ( FileshareUserLabelRule ulr : pols.getUserLabelRules() ) {
            userLabelRules.put(ulr.getMatchRole(), ulr.getAssignLabel());
        }
        ctx.instance(
            "policies",
            PropertyConfigBuilder.get().p("defaultPolicy", pols.getDefaultEntityLabel()).p("defaultRootContainerLabel", pols.getDefaultRootLabel())
                    .p("sharePasswordsBits", pols.getDefaultSharePasswordBits()).p("defaultUserLabelRules", userLabelRules));

        for ( FileshareSecurityPolicy pol : pols.getPolicies() ) {
            setupSecurityPolicy(b, ctx, pol);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param pol
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private static void setupSecurityPolicy ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareSecurityPolicy pol )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        // make sure these are sorted
        Set<String> shareTypes = new TreeSet<>();
        for ( GrantType t : pol.getAllowedShareTypes() ) {
            shareTypes.add(t.name());
        }

        p.p("label", pol.getLabel());
        p.p("sortOrder", (float) pol.getSortPriority());
        p.p("allowedShareTypes", shareTypes);

        p.p("defaultExpiration", pol.getEnableDefaultExpiration() ? pol.getDefaultExpirationDuration() : null);
        p.p("maximumExpiration", pol.getRestrictExpirationDuration() ? pol.getMaximumExpirationDuration() : null);

        p.p("defaultShareLifetime", pol.getEnableShareExpiration() ? pol.getDefaultShareLifetime() : null);
        p.p("maximumShareLifetime", pol.getRestrictShareLifetime() ? pol.getMaximumShareLifetime() : null);

        p.p("afterShareGraceTime", pol.getAfterShareGracePeriod());

        p.p("transportRequireEncryption", pol.getTransportRequireEncryption());
        p.p("transportRequirePFS", pol.getTransportRequirePFS());
        p.p("transportMinKeySize", pol.getTransportMinKeySize());
        p.p("transportMinHashBlockSize", pol.getTransportMinHashBlockSize());

        p.p("requireAnyRole", pol.getRequireAnyRole());
        p.p("disallowRoles", pol.getDisallowRoles());

        p.p("minTokenPasswordEntropy", pol.getMinTokenPasswordEntropy());
        p.p("requireTokenPassword", pol.getRequireTokenPassword());
        p.p("noUserTokenPasswords", pol.getNoUserTokenPasswords());

        p.p("disallowWebDAVAccess", pol.getDisallowWebDAVAccess());

        ctx.factory("policy", pol.getLabel().toLowerCase(), p);
    }


    /**
     * @param b
     * @param ctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private void setupNotification ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        FileshareNotificationConfig nc = ctx.octx().cfg().getNotificationConfiguration();
        HostConfiguration hc = ctx.octx().sctx().getContextService(HostConfiguration.class);

        PropertyConfigBuilder p = PropertyConfigBuilder.get();

        if ( StringUtils.isBlank(nc.getTemplateLibrary()) ) {
            // clear template library
            b.add(ClearConfigFiles.class).service(ctx.octx().job().getService()).root("mail");
        }

        if ( hc.getMailingConfiguration().getMailingEnabled() && !nc.getNotificationDisabled() ) {
            p.p("disabled", false);
            p.p("defaultSenderAddress", nc.getDefaultSenderAddress());
            p.p("defaultSenderName", nc.getDefaultSenderName());
            p.p("adminContact", nc.getAdminContactAddress());
            p.p("mailFooterText", nc.getFooter());
            p.p("expirationNotificationPeriod", nc.getExpirationNotificationPeriod());

            if ( !nc.getSendAsUserNotificationDomains().isEmpty() ) {
                p.p("sendAsUsers", true);
                p.p("asUserNotificationDomains", nc.getSendAsUserNotificationDomains());
            }
            else {
                p.p("sendAsUsers", false);
            }
            p.p("defaultLocale", nc.getDefaultNotificationLocale() != null ? nc.getDefaultNotificationLocale().toLanguageTag() : null);

            this.smtpConfigurator.setupSMTPClient(b, ctx, hc.getMailingConfiguration());
        }
        else {
            p.p("disabled", true);

        }

        ctx.instance("notify", p);

        // this is always through the local relay anyways, so we don't use TLS here
        ctx.factory("tls.mapping", "smtp", PropertyConfigBuilder.get().p("subsystem", "smtp").p("truststore", "allInvalid"));
    }


    /**
     * @param b
     * @param ctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    private void setupAuth ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {

        FileshareAuthConfig ac = ctx.octx().cfg().getAuthConfiguration();
        this.authenticatorConfigurator.setupAuthenticators(b, ctx, ac.getAuthenticators());
        this.authenticatorConfigurator.setupRoles(b, ctx, ac.getRoleConfig());

        FileshareConfigurationJob job = ctx.octx().job();
        if ( job.getCreateInitialUser() ) {
            ctx.instance(
                "user.init", //$NON-NLS-1$
                PropertyConfigBuilder.get().p("staticUser", job.getCreateInitialUserName()) //$NON-NLS-1$
                        .p("staticUserPasswordHash", job.getCreateInitialUserPasswordHash()) //$NON-NLS-1$
                        .p("staticUserRoles", job.getCreateInitialUserRoles())); //$NON-NLS-1$
        }

        setupSynchronization(b, ctx, ac);
    }


    /**
     * @param b
     * @param ctx
     * @param ac
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    private static void setupSynchronization ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareAuthConfig authc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        for ( AuthenticatorConfig ac : authc.getAuthenticators().getAuthenticators() ) {

            if ( ac instanceof ADAuthenticatorConfig ) {
                if ( ( (ADAuthenticatorConfig) ac ).getEnableSynchronization() ) {
                    setupADSynchronizationRealm(b, ctx, (ADAuthenticatorConfig) ac, authc.getNoSynchronizationRoles());
                }
            }
            else if ( ac instanceof LDAPAuthenticatorConfig ) {
                if ( ( (LDAPAuthenticatorConfig) ac ).getEnableSynchronization() ) {
                    setupLDAPSynchronizationRealm(b, ctx, (LDAPAuthenticatorConfig) ac, authc.getNoSynchronizationRoles());
                }
            }
        }

    }


    /**
     * @param b
     * @param ctx
     * @param ac
     * @param noSyncRoles
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private static void setupADSynchronizationRealm ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, ADAuthenticatorConfig ac, Set<String> noSyncRoles )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder cfg = PropertyConfigBuilder.get();

        addSyncOptions(cfg, ac.getSyncOptions());

        cfg.p("schemaStyle", LDAPServerType.AD.name()); //$NON-NLS-1$
        cfg.p("LdapClientFactory.target", FilterBuilder.get().eq("instanceId", ac.getDomain()).toString());

        cfg.p("alwaysAddRoles", filterNoSyncRoles(noSyncRoles, ac.getAlwaysAddRoles()));

        cfg.p("useForwardGroups", true);

        cfg.p("user.baseDN", ac.getUserSyncBase());
        cfg.p("user.filter", ac.getUserSyncFilter());
        cfg.p("group.baseDN", ac.getGroupSyncBase());
        cfg.p("group.filter", ac.getGroupSyncFilter());

        Map<String, Set<String>> sidMap = new HashMap<>();
        for ( StaticRoleMapEntry e : ac.getSidRoles() ) {
            Set<String> filterNoSyncRoles = filterNoSyncRoles(noSyncRoles, e.getAddRoles());
            if ( !filterNoSyncRoles.isEmpty() ) {
                sidMap.put(e.getInstance(), filterNoSyncRoles);
            }
        }

        if ( !sidMap.isEmpty() ) {
            cfg.p("roleMapAttributes", "objectSID=SID,primaryGroupID=RID");
            cfg.pmultiValueMap("attributeRoleMappings", sidMap);

        }

        ctx.factory("ldap.sync", ac.getRealm(), cfg); //$NON-NLS-1$
    }


    /**
     * @param cfg
     * @param syncOptions
     */
    @SuppressWarnings ( "nls" )
    private static void addSyncOptions ( PropertyConfigBuilder cfg, LDAPSyncOptions syncOptions ) {
        cfg.p("syncInterval", syncOptions.getSyncInterval());
        cfg.p("pageSize", syncOptions.getPageSize());
        cfg.p("removeMissing", syncOptions.getSynchronizeRemovals());
        cfg.p("removeUseUUIDs", syncOptions.getRemoveUsingUUID());
    }


    /**
     * @param b
     * @param ctx
     * @param ac
     * @param noSyncRoles
     * @param schemaConfig
     * @param syncOptions
     * @param c
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    private static void setupLDAPSynchronizationRealm ( @NonNull JobBuilder b,
            @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, LDAPAuthenticatorConfig ac,
            Set<String> noSyncRoles ) throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        PropertyConfigBuilder cfg = PropertyConfigBuilder.get();
        LDAPConfigUtil.makeLDAPAuthSchemaConfig(b, ctx, ac.getSchemaConfig(), cfg);

        addSyncOptions(cfg, ac.getSyncOptions());
        String ldapConnName = "auth-" + ac.getRealm();
        cfg.p("LdapClientFactory.target", FilterBuilder.get().eq("instanceId", ldapConnName).toString());

        cfg.p("addGroupNameAsRole", ac.getAddGroupNameAsRole());
        cfg.p("alwaysAddRoles", filterNoSyncRoles(noSyncRoles, ac.getAlwaysAddRoles()));

        Map<String, Set<String>> staticMap = new HashMap<>();
        for ( StaticRoleMapEntry me : ac.getStaticRoleMappings() ) {
            staticMap.put(me.getInstance(), filterNoSyncRoles(noSyncRoles, me.getAddRoles()));
        }
        cfg.pmultiValueMap("staticRoleMappings", staticMap);

        Map<String, Set<String>> patternMap = new HashMap<>();
        for ( PatternRoleMapEntry me : ac.getPatternRoleMappings() ) {
            patternMap.put(me.getPattern(), filterNoSyncRoles(noSyncRoles, me.getAddRoles()));
        }
        cfg.pmultiValueMap("patternRoleMappings", patternMap);

        ctx.factory("ldap.sync", ac.getRealm(), cfg); //$NON-NLS-1$
    }


    /**
     * @param noSyncRoles
     * @param alwaysAddRoles
     * @return
     */
    private static Set<String> filterNoSyncRoles ( Set<String> noSyncRoles, Set<String> alwaysAddRoles ) {
        Set<String> staticRoles = new HashSet<>(alwaysAddRoles);
        staticRoles.removeAll(noSyncRoles);
        return staticRoles;
    }


    /**
     * @param b
     * @param ctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */

    private static void setupUser ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        FileshareUserConfig uc = ctx.octx().cfg().getUserConfiguration();
        setupQuotaConfig(ctx, uc);
        setupUserConfig(ctx, uc);
        setupTrustLevels(ctx, uc);
    }


    /**
     * @param ctx
     * @param uc
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    private static void setupQuotaConfig ( RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareUserConfig uc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        FileshareUserQuotaConfig qc = uc.getQuotaConfig();

        Map<String, String> quotaRules = new LinkedHashMap<>();
        for ( FileshareQuotaRule qr : qc.getDefaultQuotaRules() ) {
            quotaRules.put(qr.getMatchRole(), qr.getQuota() != null ? qr.getQuota().toString() : null);
        }

        ctx.instance(
            "quotas",
            PropertyConfigBuilder.get().p("trackCombinedSizesWithoutQuota", !qc.getDisableSizeTrackingWithoutQuota())
                    .p("defaultQuota", qc.getEnableDefaultQuota() ? qc.getGlobalDefaultQuota() : null).p("defaultQuotaRules", quotaRules));
    }


    /**
     * @param ctx
     * @param uc
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    private static void setupUserConfig ( RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareUserConfig uc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        FileshareUserSelfServiceConfig ssc = uc.getSelfServiceConfig();
        FileshareNotificationConfig ntc = ctx.octx().cfg().getNotificationConfiguration();
        FileshareAuthConfig authc = ctx.octx().cfg().getAuthConfiguration();

        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        if ( !ntc.getNotificationDisabled() && authc.getAuthenticators().getEnableLocalAuth() ) {
            p.p("localPasswordRecoveryEnabled", ssc.getLocalPasswordRecoveryEnabled());
            p.p("passwordRecoveryTokenLifetime", ssc.getPasswordRecoveryTokenLifetime());

            p.p("invitationEnabled", ssc.getInvitationEnabled());
            p.p("invitationTokenLifetime", ssc.getInvitationTokenLifetime());
            p.p("invitationUserRoles", ssc.getInvitationUserRoles());
            p.p("invitationUserExpiration", ssc.getInvitationUserExpires() ? ssc.getInvitationUserExpiration() : null);
            p.p("trustInvitedUserNames", ssc.getTrustInvitedUserNames());
            p.p("allowInvitingUserExtension", ssc.getAllowInvitingUserExtension());

            p.p("registrationEnabled", ssc.getRegistrationEnabled());
            p.p("registrationTokenLifetime", ssc.getRegistrationTokenLifetime());
            p.p("registrationUserRoles", ssc.getRegistrationUserRoles());
            p.p("registartionUserExpiration", ssc.getRegistrationUserExpires() ? ssc.getRegistrationUserExpiration() : null);

        }
        else {
            p.p("localPasswordRecoveryEnabled", false);
            p.p("registrationEnabled", false);
            p.p("invitationEnabled", false);
        }

        Set<RoleConfig> roles = authc.getRoleConfig().getRoles();

        Set<String> staticSyncRoles = new HashSet<>();
        for ( RoleConfig role : roles ) {
            if ( !authc.getNoSynchronizationRoles().contains(role.getRoleId()) ) {
                staticSyncRoles.add(role.getRoleId());
            }
        }

        p.p("staticSynchronizationRoles", staticSyncRoles);
        p.p("noSubjectRootRoles", uc.getNoSubjectRootRoles());
        p.p("defaultRoles", uc.getDefaultRoles());

        ctx.instance("users", p);
    }


    /**
     * @param ctx
     * @param uc
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    private static void setupTrustLevels ( RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx, FileshareUserConfig uc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        FileshareUserTrustLevelConfig utlc = uc.getUserTrustLevelConfig();

        for ( FileshareUserTrustLevel fileshareUserTrustLevel : utlc.getTrustLevels() ) {
            setupTrustLevel(ctx, utlc, fileshareUserTrustLevel);
        }
    }


    /**
     * @param ctx
     * @param utlc
     * @param utl
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    private static void setupTrustLevel ( RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx,
            FileshareUserTrustLevelConfig utlc, FileshareUserTrustLevel utl )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder tl = PropertyConfigBuilder.get();

        tl.p("matchRoles", utl.getMatchRoles());
        tl.p("color", "#" + utl.getColor());

        if ( utl.getTrustLevelId().equals(utlc.getGroupTrustLevel()) ) {
            tl.p("matchGroups", true);
        }

        if ( utl.getTrustLevelId().equals(utlc.getMailTrustLevel()) ) {
            tl.p("matchAnyMail", true);
        }

        if ( utl.getTrustLevelId().equals(utlc.getLinkTrustLevel()) ) {
            tl.p("matchLinks", true);
        }

        tl.p("title", utl.getTitle());

        for ( Entry<Locale, String> entry : utl.getMessages().entrySet() ) {
            if ( Locale.ROOT.equals(entry.getKey()) ) {
                tl.p("msg", entry.getValue());
            }
            else {
                tl.p("msg_" + entry.getKey().toLanguageTag(), entry.getValue());
            }
        }
        ctx.factory("trustLevel", utl.getTrustLevelId(), tl);
    }


    /**
     * @param b
     * @param ctx
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    @SuppressWarnings ( "nls" )
    private static void setupContent ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {
        FileshareContentConfig cc = ctx.octx().cfg().getContentConfiguration();
        ctx.instance(
            "mimeTypes",
            PropertyConfigBuilder.get().p("allowMimeTypeChanges", cc.getAllowMimeTypeChanges()).p("fallbackMimeType", cc.getFallbackMimeType())
                    .p("useUserSuppliedTypes", cc.getUseUserSuppliedTypeInfo()).p("allowedMimeTypes", cc.getWhitelistMimeTypes())
                    .p("blacklistedMimeTypes", cc.getBlacklistMimeTypes()));

        FileshareContentPreviewConfig pc = cc.getPreviewConfig();
        ctx.instance(
            "viewPolicy",
            PropertyConfigBuilder.get().p("viewableMimeTypes", pc.getPreviewMimeTypes()).p("safeMimeTypes", pc.getPreviewSafeMimeTypes())
                    .p("noSandboxMimeTypes", pc.getPreviewNoSandboxMimeTypes()).p("relaxedCSPMimeTypes", pc.getPreviewRelaxedCSPMimeTypes())
                    .p("maxPreviewFileSize", pc.getLimitPreviewFileSize() ? pc.getMaxPreviewFileSize() : null));

        FileshareContentSearchConfig sc = cc.getSearchConfig();
        ctx.instance(
            "search",
            PropertyConfigBuilder.get().p("searchDisabled", sc.getSearchDisabled()).p("allowPaging", sc.getSearchAllowPaging())
                    .p("pageSize", sc.getSearchPageSize()));

        Set<String> requiredFilters = new HashSet<>();
        requiredFilters.add("mime");

        FileshareContentScanConfig scanConfig = cc.getScanConfig();
        if ( scanConfig.getEnableICAP() ) {
            requiredFilters.add("antivirus");
            String icapInstance = "content-av";
            ctx.instance(
                "antivirus",
                PropertyConfigBuilder.get().p("ICAPConnectionPool.target", FilterBuilder.get().eq("instanceId", icapInstance).toString()));
            ICAPConfiguration ic = scanConfig.getIcapConfig();
            ICAPConfigUtil.makeICAPConfiguration(b, ctx, icapInstance, ic);
        }
        ctx.instance("upload", PropertyConfigBuilder.get().p("requiredFilters", requiredFilters));
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
    private static void setupWebFrontend ( JobBuilder b, RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx,
            HostConfiguration hc, StorageContext localStoragePath, StorageContext filesStoragePath ) throws UnitInitializationFailedException,
            ServiceManagementException, InvalidParameterException, SystemInformationException, JobBuilderException {

        FileshareWebConfig wc = ctx.octx().cfg().getWebConfiguration();
        String defaultContextPath = "/";
        URI overrideBase = getOverrideBase(wc, defaultContextPath);
        PropertyConfigBuilder webConfig = PropertyConfigBuilder.get().p("overrideWebBaseUri", overrideBase).p("intentTimeout", wc.getIntentTimeout())
                .p("allowUserModificationTimes", wc.getWebDAVAllowSetModificationTime())
                .p("sessionIncompleteExpireDuration", wc.getSessionIncompleteExpireDuration())
                .p("userIncompleteExpireDuration", wc.getUserIncompleteExpireDuration())
                .p("defaultTimeZone", hc.getDateTimeConfiguration().getTimezone().getID());
        if ( wc.getPerSessionIncompleteSizeLimitEnabled() == null || wc.getPerSessionIncompleteSizeLimitEnabled() ) {
            webConfig.p("perSessionIncompleteSizeLimit", wc.getPerSessionIncompleteSizeLimit());
        }
        if ( wc.getPerUserIncompleteSizeLimitEnabled() == null || wc.getPerUserIncompleteSizeLimitEnabled() ) {
            webConfig.p("perUserIncompleteSizeLimit", wc.getPerUserIncompleteSizeLimit());
        }

        ctx.instance("frontend", webConfig);

        configureChunkService(ctx, wc, filesStoragePath);

        ctx.instance("webdav", PropertyConfigBuilder.get().p("enabled", wc.getEnableWebDAV()));

        if ( StringUtils.isBlank(wc.getThemeLibrary()) ) {
            // clear resource library
            b.add(ClearConfigFiles.class).service(ctx.octx().job().getService()).root("web");
        }

        WebConfigUtil.setupSessionManager(b, ctx, wc.getWebEndpointConfig(), localStoragePath.getLocalStorage().resolve("session/"));

        WebConfigUtil.makeWebEndpointConfig(b, ctx, wc.getWebEndpointConfig(), "web", hc.getNetworkConfiguration().getIpv6Enabled());
        WebConfigUtil.makeSecurityHeaderConfig(b, ctx, wc.getWebEndpointConfig());

        Map<String, String> webappProps = new HashMap<>();
        webappProps.put("depends", "fileshare");

        WebConfigUtil.makeWebappConfig(
            b,
            ctx,
            "fileshare",
            "eu.agno3.fileshare.webgui",
            defaultContextPath,
            false,
            wc.getWebEndpointConfig(),
            filesStoragePath.getLocalStorage().resolve("tmp-files/"),
            webappProps,
            "web");
    }


    /**
     * @param wc
     * @param defaultContextPath
     * @return
     * @throws JobBuilderException
     */
    private static URI getOverrideBase ( FileshareWebConfig wc, String defaultContextPath ) throws JobBuilderException {
        URI overrideBase = wc.getOverrideBaseURI();
        if ( overrideBase == null && wc.getWebEndpointConfig().getBehindReverseProxy() ) {
            URI proxyOverride = wc.getWebEndpointConfig().getReverseProxyConfig().getOverrideURI();
            // if a proxy override is set and no override base, set to proxy override
            if ( proxyOverride != null && !StringUtils.isBlank(proxyOverride.getScheme()) && !StringUtils.isBlank(proxyOverride.getHost()) ) {
                String contextPath = !StringUtils.isBlank(wc.getWebEndpointConfig().getContextPath()) ? wc.getWebEndpointConfig().getContextPath()
                        : defaultContextPath;
                try {
                    overrideBase = new URI(
                        proxyOverride.getScheme(),
                        null,
                        proxyOverride.getHost(),
                        proxyOverride.getPort(),
                        contextPath,
                        null,
                        null);
                }
                catch ( URISyntaxException e ) {
                    throw new JobBuilderException(e);
                }
            }
        }
        return overrideBase;
    }


    /**
     * @param b
     * @param ctx
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     */
    private static void ensureKeystores ( JobBuilder b, @NonNull RuntimeConfigContext<FileshareConfiguration, FileshareConfigurationJob> ctx )
            throws UnitInitializationFailedException, ServiceManagementException, InvalidParameterException {
        b.add(EnsureKeystore.class).keystore(FILESHARE).user(ctx.getServiceManager().getServicePrincipal()).internal();
        b.add(EnsureGeneratedKey.class).keystore(FILESHARE).alias("ssh"); //$NON-NLS-1$
        ctx.factory("keystore", FILESHARE, PropertyConfigBuilder.get()); //$NON-NLS-1$
    }

}

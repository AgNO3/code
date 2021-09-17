/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider;
import eu.agno3.orchestrator.agent.bootstrap.BootstrapRunnerLock;
import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.agent.crypto.InternalCAConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.bootstrap.msg.BootstrapRequestMessage;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.hostconfig.agent.HostConfigJobBuilder;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.agent.OrchestratorConfigJobBuilder;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.jobs.OrchestratorConfigurationJob;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.coord.ExecutorFactory;
import eu.agno3.orchestrator.jobs.coord.LoggingJobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.dirconfig.util.DirectoryConfigUtil;
import eu.agno3.orchestrator.system.dirconfig.util.DirectoryWriter;
import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.configloader.contribs.DirectoryConfigContribution;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.update.PlatformStateManager;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.xml.XmlFormattingWriter;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = BootstrapRunnerImpl.class, immediate = true )
public class BootstrapRunnerImpl implements Runnable, UncaughtExceptionHandler {

    private static final String KEYSTORE_CFG_PID = "keystore"; //$NON-NLS-1$
    private static final String TRUSTSTORE_CFG_PID = "truststore"; //$NON-NLS-1$
    private static final String TLS_MAPPING_PID = "tls.mapping"; //$NON-NLS-1$
    private static final String BOOTSTRAP_PID = "bootstrap"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BootstrapRunnerImpl.class);

    private BootstrapConfigurationProvider bcProv;
    private ServiceSystem serviceSystem;
    private ServerConnectorConfiguration agentConnectorConfig;
    private AgentServerConnector agentConnector;
    private Semaphore agentConnectorSemapore = new Semaphore(0);
    private Thread bootstrapThread;
    private MessagingClient<AgentMessageSource> msgClient;
    private ConfigLoader configLoader;
    private BootstrapCryptoRunnerImpl bootstrapCrypto;

    private PlatformStateManager platformState;

    private ComponentContext componentContext;

    private ConfigurationAdmin configAdmin;
    private OrchestratorConfigJobBuilder serverConfigBuilder;
    private HostConfigJobBuilder hostConfigBuilder;

    private ExecutorFactory executorFactory;
    private ObjectTypeRegistry objectTypeRegistry;
    private ConfigRepository configRepo;
    private XmlMarshallingService marshallingService;


    @Reference
    protected synchronized void setBootstrapConfigProvider ( BootstrapConfigurationProvider bcp ) {
        this.bcProv = bcp;
    }


    protected synchronized void unsetBootstrapConfigProvider ( BootstrapConfigurationProvider bcp ) {
        if ( this.bcProv == bcp ) {
            this.bcProv = null;
        }
    }


    @Reference
    protected synchronized void setConfigurationAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigurationAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    @Reference
    protected synchronized void setServiceSystem ( ServiceSystem ss ) {
        this.serviceSystem = ss;
    }


    protected synchronized void unsetServiceSystem ( ServiceSystem ss ) {
        if ( this.serviceSystem == ss ) {
            this.serviceSystem = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnectorConfig ( ServerConnectorConfiguration config ) {
        this.agentConnectorConfig = config;
    }


    protected synchronized void unsetAgentConnectorConfig ( ServerConnectorConfiguration config ) {
        if ( this.agentConnectorConfig == config ) {
            this.agentConnectorConfig = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setAgentServerConnector ( AgentServerConnector conn ) {
        this.agentConnector = conn;
        this.agentConnectorSemapore.release();
    }


    protected synchronized void unsetAgentServerConnector ( AgentServerConnector conn ) {
        if ( this.agentConnector == conn ) {
            this.agentConnector = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setMessagingClient ( MessagingClient<AgentMessageSource> msgCl ) {
        this.msgClient = msgCl;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<AgentMessageSource> msgCl ) {
        if ( this.msgClient == msgCl ) {
            this.msgClient = null;
        }
    }


    @Reference
    protected synchronized void setBootstrapCrypto ( BootstrapCryptoRunnerImpl bootc ) {
        this.bootstrapCrypto = bootc;
    }


    protected synchronized void unsetBootstrapCrypto ( BootstrapCryptoRunnerImpl bootc ) {
        if ( this.bootstrapCrypto == bootc ) {
            this.bootstrapCrypto = null;
        }
    }


    @Reference
    protected synchronized void setConfigLoader ( ConfigLoader cf ) {
        this.configLoader = cf;
    }


    protected synchronized void unsetConfigLoader ( ConfigLoader cf ) {
        if ( this.configLoader == cf ) {
            this.configLoader = null;
        }
    }


    @Reference
    protected synchronized void setServerConfigJobBuilder ( OrchestratorConfigJobBuilder cb ) {
        this.serverConfigBuilder = cb;
    }


    protected synchronized void unsetServerConfigJobBuilder ( OrchestratorConfigJobBuilder cb ) {
        if ( this.serverConfigBuilder == cb ) {
            this.serverConfigBuilder = null;
        }
    }


    @Reference
    protected synchronized void setHostConfigJobBuilder ( HostConfigJobBuilder cb ) {
        this.hostConfigBuilder = cb;
    }


    protected synchronized void unsetHostConfigJobBuilder ( HostConfigJobBuilder cb ) {
        if ( this.hostConfigBuilder == cb ) {
            this.hostConfigBuilder = null;
        }
    }


    @Reference
    protected synchronized void setExecutorFactory ( ExecutorFactory ef ) {
        this.executorFactory = ef;
    }


    protected synchronized void unsetExecutorFactory ( ExecutorFactory ef ) {
        if ( this.executorFactory == ef ) {
            this.executorFactory = null;
        }
    }


    @Reference
    protected synchronized void setObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        this.objectTypeRegistry = otr;
    }


    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        if ( this.objectTypeRegistry == otr ) {
            this.objectTypeRegistry = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepo = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepo == cr ) {
            this.configRepo = null;
        }
    }


    @Reference
    protected synchronized void setXmlMarshaller ( XmlMarshallingService xms ) {
        this.marshallingService = xms;
    }


    protected synchronized void unsetXmlMarshaller ( XmlMarshallingService xms ) {
        if ( this.marshallingService == xms ) {
            this.marshallingService = null;
        }
    }


    @Reference
    protected synchronized void setPlatformStateManager ( PlatformStateManager psm ) {
        this.platformState = psm;
    }


    protected synchronized void unsetPlatformStateManager ( PlatformStateManager psm ) {
        if ( this.platformState == psm ) {
            this.platformState = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.bootstrapThread = new Thread(this, "Bootstrap"); //$NON-NLS-1$
        this.bootstrapThread.setUncaughtExceptionHandler(this);
        this.bootstrapThread.start();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException ( Thread t, Throwable e ) {
        log.error("Uncaught exception in bootstrap thread", e); //$NON-NLS-1$
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.bootstrapThread.interrupt();
        try {
            this.bootstrapThread.join();
        }
        catch ( InterruptedException e ) {
            log.warn("Failed to join bootstrap thread", e); //$NON-NLS-1$
        }
        this.componentContext = null;
    }


    /**
     * 
     */
    @Override
    public void run () {

        if ( this.bcProv == null ) {
            log.error("No bootstrap configuration available"); //$NON-NLS-1$
            return;
        }

        if ( this.bcProv.isDeveloperMode() && BootstrapRunnerLock.hasRun() ) {
            return;
        }

        log.info("Bootstrap configuration available, running bootstrap process"); //$NON-NLS-1$

        DirectoryWriter agentConfigWriter = getAgentConfigWriter();

        try {
            configureLocalAgent(agentConfigWriter);
        }
        catch (
            NoSuchAlgorithmException |
            IOException e ) {
            log.error("Failed to configure local agent", e); //$NON-NLS-1$
            return;
        }

        localAgentConfigured(agentConfigWriter);
    }


    /**
     * @param agentConfigWriter
     * @param caCert
     */
    private void localAgentConfigured ( DirectoryWriter agentConfigWriter ) {
        ServiceBootstrapResult res;
        // first bootstrap local server
        if ( this.bcProv.isLocalServer() ) {
            res = this.bootstrapLocalServer(agentConfigWriter);
        }
        else {
            // establish trust with remote server and connect
            // TODO: implement
            log.error("Remote servers not currently supported"); //$NON-NLS-1$
            return;
        }

        serverConfigured(agentConfigWriter, res);
    }


    /**
     * @param agentConfigWriter
     * @param caCert
     */
    private void serverConfigured ( DirectoryWriter agentConfigWriter, ServiceBootstrapResult res ) {
        try {
            if ( !this.agentConnectorSemapore.tryAcquire(120, TimeUnit.SECONDS) ) {
                log.error("Agent connector failed to start"); //$NON-NLS-1$
                return;
            }
        }
        catch ( InterruptedException e ) {
            log.error("Failed to acquire semaphore", e); //$NON-NLS-1$
            return;
        }

        // wait for server to be available
        if ( this.bcProv.isLocalServer() && !waitForServer() ) {
            return;
        }

        serverAvailable(agentConfigWriter, res);
    }


    /**
     * @param agentConfigWriter
     * @param caCert
     */
    private void serverAvailable ( DirectoryWriter agentConfigWriter, ServiceBootstrapResult res ) {
        try {
            // submit bootstrap configuration to server
            MessagingClient<AgentMessageSource> cl = this.msgClient;

            if ( cl == null ) {
                log.error("Failed to obtain messaging client"); //$NON-NLS-1$
                return;
            }

            BootstrapRequestMessage req = makeBootstrapRequest(res);
            try {
                cl.sendMessage(req);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.error("Failed to send bootstrap request", e); //$NON-NLS-1$
                return;
            }

            // user may now access the web interface and complete the configuration
            // remove bootstrap configuration, so that this process will not be triggered again
            removeBootstrapConfig(agentConfigWriter);
        }
        finally {
            this.agentConnectorSemapore.release();
        }
    }


    /**
     * @return
     */
    private @NonNull BootstrapRequestMessage makeBootstrapRequest ( ServiceBootstrapResult res ) {
        BootstrapRequestMessage req = new BootstrapRequestMessage((AgentMessageSource) this.agentConnector.getMessageSource());
        req.setAdminPassword(this.bcProv.getAdminPassword());
        req.setAutoRun(this.bcProv.isAutoRun());

        req.setImageType(this.bcProv.getImageType());
        req.setCaCertificate(res.getCryptoResult().getCaCert());
        req.setWebCertificate(res.getCryptoResult().getWebCert());

        req.setHostConfigService(res.getHostService());
        req.setBootstrapHostConfig(this.bcProv.getBootstrapHostConfig());

        if ( this.bcProv.isLocalServer() ) {
            req.setServerService(res.getServerService());
            req.setBootstrapServerConfig(this.bcProv.getServerConfiguration());
        }

        return req;
    }


    /**
     * @param agentConfigWriter
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void configureLocalAgent ( DirectoryWriter agentConfigWriter ) throws IOException, NoSuchAlgorithmException {
        Properties txProps = new Properties();
        txProps.put(
            "uniqueName", //$NON-NLS-1$
            String.format("tx-agent-%s", this.agentConnectorConfig.getComponentId())); //$NON-NLS-1$
        agentConfigWriter.createConfig("txservice", txProps); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private DirectoryWriter getAgentConfigWriter () {
        Collection<ConfigContribution> contribs = this.configLoader.getSourcesForPid(BootstrapConfigurationProviderImpl.PID);

        if ( contribs.size() != 1 ) {
            log.error("Bootstrap configuration is provided by multiple sources, cannot remove"); //$NON-NLS-1$
            return null;
        }

        ConfigContribution contrib = contribs.iterator().next();

        if ( ! ( contrib instanceof DirectoryConfigContribution ) ) {
            log.error("Bootstrap configuration is not provided by a configuration directory, cannot remove"); //$NON-NLS-1$
            return null;
        }

        File baseDir = ( (DirectoryConfigContribution) contrib ).getBaseDir();

        return DirectoryConfigUtil.getWriter(baseDir.toPath());
    }


    /**
     * @param wr
     * 
     */
    private void removeBootstrapConfig ( DirectoryWriter wr ) {

        if ( !wr.exists(BOOTSTRAP_PID) ) {
            log.error("Bootstrap configuration file does not exist"); //$NON-NLS-1$
            return;
        }

        if ( !this.bcProv.isDeveloperMode() ) {
            log.info("Bootstrap complete, removing configuration"); //$NON-NLS-1$

            try {
                wr.removeConfig(BOOTSTRAP_PID);
            }
            catch ( IOException e ) {
                log.warn("Failed to remove bootstrap configuration", e); //$NON-NLS-1$
            }
        }
        else {
            log.warn("In dev mode, not removing bootstrap configuration, disabling runner"); //$NON-NLS-1$
            this.componentContext.disableComponent(BootstrapRunnerImpl.class.getName());
            BootstrapRunnerLock.setRun();
        }

        try {
            Configuration configuration = this.configAdmin.getConfiguration(BootstrapConfigurationProviderImpl.PID);
            configuration.delete();
        }
        catch ( IOException e ) {
            log.warn("Failed to remove bootstrap configuration instance", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    boolean waitForServer () {
        // wait for the server to complete
        log.info("Waiting for server to boot..."); //$NON-NLS-1$
        if ( !waitForConnector() ) {
            return false;
        }

        int tries = 1;
        while ( !tryConnect() ) {
            log.info(String.format("Still waiting for server to boot... (try %d)", tries)); //$NON-NLS-1$
            tries++;
        }

        if ( this.agentConnector == null ) {
            log.error("Failed to get agent connector"); //$NON-NLS-1$
            return false;
        }

        if ( this.agentConnector.getState() != ServerConnectorState.CONNECTED ) {
            log.error("Giving up connection to local server, state is " + this.agentConnector.getState()); //$NON-NLS-1$
            return false;
        }

        log.info("Connected to server " + this.agentConnector.getServerAddress()); //$NON-NLS-1$
        return true;
    }


    /**
     * @return whether the connection succeeded
     * 
     */
    private boolean tryConnect () {
        int tookSecs = 0;
        while ( this.agentConnector != null && this.agentConnector.getState() != ServerConnectorState.CONNECTED && tookSecs < 60 ) {
            log.debug("Trying to connect to server..."); //$NON-NLS-1$
            try {
                if ( this.agentConnector.tryConnect() ) {
                    log.debug("Try connect succeeded"); //$NON-NLS-1$
                    Thread.sleep(100);
                    continue;
                }

                if ( this.agentConnector.getState() == ServerConnectorState.CONNECTING ) {
                    this.platformState.refreshAppState();
                }

                log.trace("Try connect failed, waiting"); //$NON-NLS-1$
                tookSecs++;
                Thread.sleep(1000);
            }
            catch ( InterruptedException e ) {
                log.debug("Inter-connect sleep interrupted", e); //$NON-NLS-1$
                return true;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("State is " + ( this.agentConnector != null ? this.agentConnector.getState() //$NON-NLS-1$
                    : "NULL" )); //$NON-NLS-1$
        }
        return this.agentConnector != null && this.agentConnector.getState() == ServerConnectorState.CONNECTED;
    }


    /**
     * @return
     * 
     */
    private boolean waitForConnector () {
        try {
            AgentServerConnector connector = this.agentConnector;
            if ( connector != null && connector.getState() == ServerConnectorState.CONNECTING ) {
                this.platformState.refreshAppState();
                Thread.sleep(5 * 1000);
            }
            if ( connector == null || connector.getState() != ServerConnectorState.CONNECTED ) {
                Thread.sleep(60 * 1000);
            }
            return true;
        }
        catch ( InterruptedException e ) {
            log.debug("Waiting for server interrupted", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param agentConfigWriter
     * 
     */
    protected ServiceBootstrapResult bootstrapLocalServer ( DirectoryWriter agentConfigWriter ) {

        log.info("Bootstrapping local orchestrator server"); //$NON-NLS-1$

        UserPrincipal[] serverUser = getServerUsers();
        DirectoryWriter serverConfigWriter;
        try {
            serverConfigWriter = getServerConfigWriter(serverUser);
        }
        catch ( UnixAccountException e ) {
            log.error("Failed to get server config writer", e); //$NON-NLS-1$
            return null;
        }

        UUID serverId;
        try {
            serverId = getOrCreateServerId(serverConfigWriter);
        }
        catch (
            NoSuchAlgorithmException |
            IOException e ) {
            log.error("Failed to determine server id", e); //$NON-NLS-1$
            return null;
        }

        String bootstrapHostname = buildHostname();

        if ( log.isDebugEnabled() ) {
            log.debug("Hostname is " + bootstrapHostname); //$NON-NLS-1$
        }

        log.info("Setting up trust"); //$NON-NLS-1$

        CryptoBootstrapResult crypto = this.bootstrapCrypto
                .setupLocalServerCrypto(this.agentConnectorConfig.getComponentId(), bootstrapHostname, serverId, serverUser, agentConfigWriter);
        if ( crypto == null ) {
            return null;
        }

        ServiceStructuralObject hostService;
        ServiceStructuralObject serverService;
        try {
            hostService = configureHost();
            serverService = configureServer();
        }
        catch ( Exception e ) {
            log.error("Failed to configure server", e); //$NON-NLS-1$
            return null;
        }

        if ( !configureCrypto(agentConfigWriter) ) {
            return null;
        }

        restartServer();

        return new ServiceBootstrapResult(crypto, hostService, serverService);
    }


    /**
     * @throws Exception
     * 
     */
    private ServiceStructuralObject configureHost () throws Exception {
        log.info("Configuring host"); //$NON-NLS-1$
        HostConfigurationJob j = new HostConfigurationJob();
        j.getApplyInfo().setForce(true);
        j.setBootstrapping(true);

        ServiceStructuralObject service = getBootstrapService(HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE);
        j.setService(service);
        @NonNull
        HostConfiguration proxyCfg = BootstrapDefaultsInvocationHandler.makeProxy(this.bcProv.getBootstrapHostConfig(), this.objectTypeRegistry);
        j.setHostConfig(proxyCfg);

        if ( log.isDebugEnabled() ) {
            dumpConfig(proxyCfg);
        }

        JobRunnable runnable = this.hostConfigBuilder.getRunnableForJob(j);
        runnable.run(new LoggingJobOutputHandler(log, true));
        return service;
    }


    /**
     * @param proxyCfg
     * @throws FactoryConfigurationError
     */
    private void dumpConfig ( ConfigurationObject proxyCfg ) throws FactoryConfigurationError {
        try ( LogWriter stream = new LogWriter(log, Level.DEBUG) ) {
            XMLStreamWriter st = new XmlFormattingWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(stream));
            this.marshallingService.marshall(proxyCfg, st);
        }
        catch ( Exception e ) {
            log.warn("Failed to get debug config output", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws ConfigRepositoryException
     */
    private ServiceStructuralObject getBootstrapService ( String serviceType ) throws ConfigRepositoryException {
        try {
            ServiceStructuralObject s = this.configRepo.getSingletonServiceByType(serviceType);
            if ( s != null ) {
                log.info("Found existing service of type " + serviceType); //$NON-NLS-1$
                return s;
            }
        }
        catch ( ConfigRepositoryException e ) {
            log.debug("Service does not exist", e); //$NON-NLS-1$
        }

        ServiceStructuralObjectImpl service = new ServiceStructuralObjectImpl();
        service.setId(UUID.randomUUID());
        service.setServiceType(serviceType);
        this.configRepo.ensureServiceRegistered(service);
        return service;
    }


    /**
     * @throws Exception
     * 
     */
    protected ServiceStructuralObject configureServer () throws Exception {
        log.info("Configuring server"); //$NON-NLS-1$
        OrchestratorConfigurationJob j = new OrchestratorConfigurationJob();
        j.getApplyInfo().setForce(true);
        j.setBootstrapping(true);

        ServiceStructuralObject service = getBootstrapService(OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE);
        j.setService(service);
        j.setBootstrapHostConfig(this.bcProv.getBootstrapHostConfig());
        @NonNull
        OrchestratorConfiguration cfg = BootstrapDefaultsInvocationHandler.makeProxy(this.bcProv.getServerConfiguration(), this.objectTypeRegistry);
        j.setOrchestratorConfig(cfg);

        if ( log.isDebugEnabled() ) {
            dumpConfig(cfg);
        }

        JobRunnable runnable = this.serverConfigBuilder.getRunnableForJob(j);
        runnable.run(new LoggingJobOutputHandler(log, true));
        return service;
    }


    /**
     * @return
     */
    protected String buildHostname () {
        @Nullable
        HostIdentification hostIdentification = this.bcProv.getBootstrapHostConfig().getHostIdentification();

        if ( hostIdentification == null ) {
            log.error("No host identification present"); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
        return String.format(
            "%s.%s", //$NON-NLS-1$
            hostIdentification.getHostName(),
            hostIdentification.getDomainName());
    }


    /**
     * @param agentConfigWriter
     * @return success
     */
    protected boolean configureCrypto ( DirectoryWriter agentConfigWriter ) {
        try {
            enableTrustStores(agentConfigWriter);
            enableAgentKeyStores(agentConfigWriter);
            enableAgentCA(agentConfigWriter);
            this.configLoader.reload(KEYSTORE_CFG_PID);
            this.configLoader.reload(TLS_MAPPING_PID);
            this.configLoader.reload(InternalCAConfig.PID);
        }
        catch (
            IOException |
            NoSuchAlgorithmException e ) {
            log.error("Failed to enable key/trust stores", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param serverConfigWriter
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static UUID getOrCreateServerId ( DirectoryWriter serverConfigWriter ) throws NoSuchAlgorithmException, IOException {
        final String serverConfigPid = "server"; //$NON-NLS-1$
        final String idProp = "id"; //$NON-NLS-1$
        if ( serverConfigWriter.exists(serverConfigPid) ) {
            Properties serverProps = serverConfigWriter.readConfig(serverConfigPid);
            return UUID.fromString(serverProps.getProperty(idProp));
        }

        UUID serverId = UUID.randomUUID();
        Properties serverProps = new Properties();
        serverProps.put(idProp, serverId.toString());
        serverConfigWriter.createConfig(serverConfigPid, serverProps);
        return serverId;
    }


    /**
     * @param agentConfigWriter
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    protected void enableAgentKeyStores ( DirectoryWriter agentConfigWriter ) throws NoSuchAlgorithmException, IOException {
        log.info("Enabling key stores"); //$NON-NLS-1$
        setupKeyStoreConfig(BootstrapConstants.AGENT_KEYSTORE, agentConfigWriter);
        setupTlsConfig(
            agentConfigWriter,
            "agent-client", //$NON-NLS-1$
            "agent/client", //$NON-NLS-1$
            1000,
            BootstrapConstants.AGENT_KEYSTORE,
            BootstrapConstants.AGENT_KEY_ALIAS,
            BootstrapConstants.INTERNAL_TRUSTSTORE,
            null);
        this.configLoader.reload("keystore@" + BootstrapConstants.AGENT_KEYSTORE); //$NON-NLS-1$
    }


    /**
     * @param agentConfigWriter
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static void enableAgentCA ( DirectoryWriter agentConfigWriter ) throws NoSuchAlgorithmException, IOException {
        log.info("Enabling internal CA"); //$NON-NLS-1$

        Properties props = new Properties();

        props.setProperty("keystore", BootstrapConstants.INTERNAL_CA_KEYSTORE); //$NON-NLS-1$
        props.setProperty("keyAlias", BootstrapConstants.CA_KEY_ALIAS); //$NON-NLS-1$

        agentConfigWriter.createConfig(InternalCAConfig.PID, props);
    }


    /**
     * @param serverConfigWriter
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void enableTrustStores ( DirectoryWriter configWriter ) throws NoSuchAlgorithmException, IOException {
        setupTruststore(configWriter, BootstrapConstants.INTERNAL_TRUSTSTORE); // $NON-NLS-1$
        this.configLoader.forceReload("truststore@" + BootstrapConstants.INTERNAL_TRUSTSTORE); //$NON-NLS-1$
    }


    /**
     * @param configWriter
     * @param string
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static void setupTruststore ( DirectoryWriter configWriter, String instance ) throws NoSuchAlgorithmException, IOException {
        configWriter.createConfig(TRUSTSTORE_CFG_PID, instance, new Properties());
    }


    /**
     * 
     * @param configWriter
     * @param instanceId
     * @param subsystem
     * @param prio
     * @param keystore
     * @param keyAlias
     * @param trustStore
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static void setupTlsConfig ( DirectoryWriter configWriter, String instanceId, String subsystem, int prio, String keystore,
            String keyAlias, String trustStore, String hostnameVerifier ) throws NoSuchAlgorithmException, IOException {

        Properties props = new Properties();
        props.put("subsystem", subsystem); //$NON-NLS-1$
        props.put("priority", String.valueOf(prio)); //$NON-NLS-1$
        if ( keystore != null ) {
            props.put("keyStore", keystore); //$NON-NLS-1$
            props.put("keyAlias", keyAlias); //$NON-NLS-1$
        }
        if ( trustStore != null ) {
            props.put("trustStore", trustStore); //$NON-NLS-1$
        }

        if ( hostnameVerifier != null ) {
            props.put("hostnameVerifier", hostnameVerifier); //$NON-NLS-1$
        }

        configWriter.createConfig(TLS_MAPPING_PID, instanceId, props);
    }


    /**
     * @param agentKeystore
     * @param agentConfigWriter
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static void setupKeyStoreConfig ( String ksId, DirectoryWriter confWriter ) throws NoSuchAlgorithmException, IOException {
        confWriter.createConfig(KEYSTORE_CFG_PID, ksId, new Properties());
    }


    /**
     * @param serverUser
     * @return
     * @throws UnixAccountException
     */
    protected DirectoryWriter getServerConfigWriter ( UserPrincipal[] serverUser ) throws UnixAccountException {
        Path serverConfigDir = this.bcProv.getLocalServerConfigDirectory().toPath();
        if ( serverUser != null && serverUser.length > 0 ) {
            UserPrincipal root = UnixAccountUtil.resolveUser("root");//$NON-NLS-1$
            // server group name is the same as server user
            GroupPrincipal serverGroup = UnixAccountUtil.resolveGroup(serverUser[ 0 ].getName());
            return DirectoryConfigUtil.getWriter(serverConfigDir, root, serverGroup, false);
        }

        // running as a unprivileged user both agent and server run as the same user
        return DirectoryConfigUtil.getWriter(serverConfigDir);
    }


    /**
     * @return
     */
    protected UserPrincipal[] getServerUsers () {
        UserPrincipal[] serverPrincipals = new UserPrincipal[] {};
        if ( this.bcProv.isDeveloperMode() ) {
            return serverPrincipals;
        }
        try {
            UserPrincipal serverPrincipal = UnixAccountUtil.resolveUser(BootstrapConstants.ORCHSERVER_USER);
            serverPrincipals = new UserPrincipal[] {
                serverPrincipal
            };
        }
        catch ( UnixAccountException e ) {
            log.warn("Failed to get server user", e); //$NON-NLS-1$
        }
        return serverPrincipals;
    }


    /**
     * 
     */
    protected void restartServer () {
        if ( this.bcProv.isDeveloperMode() ) {
            log.warn("In dev mode, you need to manually start the orchestrator server"); //$NON-NLS-1$
            return;
        }
        try {
            Service orchserver = this.serviceSystem.getService("orchserver"); //$NON-NLS-1$
            orchserver.enableOnBoot();
            log.info("Restarting orchestrator server"); //$NON-NLS-1$
            orchserver.stop();
            orchserver.start();
        }
        catch ( ServiceException e ) {
            log.error("Failed to obtain orchestrator server service", e); //$NON-NLS-1$
        }
    }

}

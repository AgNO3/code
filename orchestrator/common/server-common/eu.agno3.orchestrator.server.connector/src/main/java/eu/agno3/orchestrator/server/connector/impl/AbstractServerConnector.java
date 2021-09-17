/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentIllegalConnStateException;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigRequest;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigResponse;
import eu.agno3.orchestrator.server.component.msg.ComponentPingRequest;
import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.messaging.CallErrorException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.client.MessagingClientFactory;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.scheduler.JobProperties;
import eu.agno3.runtime.scheduler.TriggeredJob;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 * @param <TSource>
 * @param <TConfig>
 */
public abstract class AbstractServerConnector <@NonNull TSource extends MessageSource, TConfig extends ComponentConfig>
        implements ServerConnector<TConfig> {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
    private static final String CONNECTION_ESTABLISH_FAILED = "Failed to establish server connection:"; //$NON-NLS-1$
    private static final String CONNECTION_CLEAN_UP_FAILED = "Connection clean up failed:"; //$NON-NLS-1$
    private static final String ILLEGAL_SERVER_ADDRESS = "Illegal server address:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(AbstractServerConnector.class);

    private final Object connectionLock = new Object();
    private ComponentContext componentContext;
    private ServerConnectorState state = ServerConnectorState.DISCONNECTED;
    private ServerConnectorConfiguration connectorConfig;
    private Optional<@NonNull ActiveMQConnection> systemConnection = Optional.empty();
    private ServiceRegistration<XAConnectionFactory> connectionFactoryRegistration;
    private MessagingClientFactory messagingClientFactory;
    private Optional<MessagingClient<TSource>> systemClient = Optional.empty();
    private ServiceRegistration<TriggeredJob> pingerRegistration;
    private ServiceRegistration<TConfig> configRegistration;
    private ServiceRegistration<MessageSource> messageSourceRegistration;
    private TLSContext tlsContext;

    private final ServerTransportListener serverTransportListener = new ServerTransportListener(this);

    private Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread ( Runnable r ) {
            Thread t = new Thread(r, "Connector-Executor"); //$NON-NLS-1$
            return t;
        }
    });


    /**
     * 
     * @return the local message source
     */
    @Override
    public abstract @NonNull TSource getMessageSource ();


    protected abstract @NonNull ComponentConfigRequest<TSource, TConfig, ? extends ComponentConfigResponse<TConfig>, ? extends ErrorResponseMessage<@NonNull ?>> makeConfigRequest ();


    protected abstract @NonNull ComponentPingRequest<TSource> makePingEvent ();


    protected abstract @NonNull EventMessage<TSource> makeDisconnectingEvent ( TSource ms );


    protected abstract @NonNull EventMessage<TSource> createConnectingEvent ();


    protected abstract @NonNull EventMessage<TSource> createConnectedEvent ();


    protected abstract @NonNull Class<TConfig> getConfigClass ();


    protected abstract String buildUserName ( UUID id );


    /**
     * @return the log
     */
    protected static Logger getLog () {
        return log;
    }


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        log.debug("Activating ServerConnector"); //$NON-NLS-1$
        this.componentContext = context;
        this.executor.execute(new Runnable() {

            @Override
            public void run () {
                tryConnect();
            }
        });
    }


    @Reference
    protected synchronized void setMessagingClientFactory ( MessagingClientFactory mcf ) {
        this.messagingClientFactory = mcf;
    }


    protected synchronized void unsetMessagingClientFactory ( MessagingClientFactory mcf ) {
        if ( this.messagingClientFactory == mcf ) {
            this.messagingClientFactory = null;
        }
    }


    @Reference ( target = "(subsystem=jms/client)" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Reference ( updated = "updatedServerConnectorConfig" )
    protected synchronized void setServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        this.connectorConfig = scc;
    }


    protected synchronized void updatedServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        @NonNull
        TSource ms = getMessageSource();
        this.connectorConfig = scc;
        if ( this.state == ServerConnectorState.CONNECTED || this.state == ServerConnectorState.ERROR ) {
            // reconnect required
            this.executor.execute(new Runnable() {

                @Override
                public void run () {
                    try {
                        disconnect(ms, false);
                        tryConnect();
                    }
                    catch ( ServerConnectorException e ) {
                        getLog().error("Modified configuration invalid:", e); //$NON-NLS-1$
                    }
                }
            });
        }

    }


    protected synchronized void unsetServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        if ( this.connectorConfig == scc ) {
            this.connectorConfig = null;
        }
    }


    /**
     * 
     */
    public AbstractServerConnector () {
        super();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        if ( this.state == ServerConnectorState.CONNECTED ) {
            @NonNull
            TSource ms = getMessageSource();
            this.executor.execute(new Runnable() {

                @Override
                public void run () {
                    try {
                        disconnect(ms, false);
                    }
                    catch ( ServerConnectorException e ) {
                        getLog().error("Failed to disconnect on shutdown", e); //$NON-NLS-1$
                    }
                }
            });
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#tryConnect()
     */
    @Override
    public boolean tryConnect () {

        try {
            this.connectInternal(false);
            return true;
        }
        catch (
            ServerConnectorException |
            InterruptedException e ) {
            log.trace("Connection failed:", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#connect()
     */
    @Override
    public void connect () throws ServerConnectorException, InterruptedException {
        this.connectInternal(true);
    }


    /**
     * 
     * @param log
     * @throws ServerConnectorException
     * @throws InterruptedException
     */
    private void connectInternal ( boolean doLogConnError ) throws ServerConnectorException, InterruptedException {
        synchronized ( this.connectionLock ) {

            ServerConnectorState oldState = this.state;
            if ( oldState == ServerConnectorState.CONNECTED || oldState == ServerConnectorState.CONNECTING ) {
                return;
            }

            if ( oldState != ServerConnectorState.DISCONNECTED && oldState != ServerConnectorState.ERROR ) {
                throw new ServerConnectorException("Cannot connect to server in state " + oldState); //$NON-NLS-1$
            }

            this.state = ServerConnectorState.CONNECTING;

            String brokerURL;
            try {
                brokerURL = String.format(
                    "failover:(ssl://%s:%d)?%s", //$NON-NLS-1$
                    this.connectorConfig.getServerAddress().getHostName(),
                    this.connectorConfig.getServerPort(),
                    makeOptions());
            }
            catch ( UnsupportedEncodingException e ) {
                throw new ServerConnectorException(ILLEGAL_SERVER_ADDRESS, e);
            }
            String userName = this.buildUserName(this.connectorConfig.getComponentId());

            if ( log.isDebugEnabled() ) {
                log.debug(
                    String.format("Connecting to server %s using component ID %s", brokerURL.toString(), this.connectorConfig.getComponentId())); //$NON-NLS-1$
            }

            try {
                ActiveMQXAConnectionFactory connFactory = openSystemConnection(doLogConnError, brokerURL, userName, oldState);
                log.debug("JMS connection is up"); //$NON-NLS-1$
                openSystemClient();
                greetServer(brokerURL, userName, connFactory);
            }
            catch ( ServerConnectorException e ) {
                if ( doLogConnError ) {
                    log.warn(String.format(
                        "Connection to server %s:%d failed", //$NON-NLS-1$
                        this.connectorConfig.getServerAddress().getHostName(),
                        this.connectorConfig.getServerPort()));
                    log.debug("Server connection failed:", e); //$NON-NLS-1$
                }
                throw e;
            }
        }
    }


    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeOptions () throws UnsupportedEncodingException {
        Map<String, String> optHash = new HashMap<>();
        optHash.put("timeout", String.valueOf(1000)); //$NON-NLS-1$
        optHash.put("startupMaxReconnectAttempts", String.valueOf(3)); //$NON-NLS-1$
        optHash.put("maxReconnectAttempts", String.valueOf(3)); //$NON-NLS-1$
        optHash.put("maxReconnectDelay", String.valueOf(2000)); //$NON-NLS-1$
        List<String> opts = new ArrayList<>();
        for ( Entry<String, String> o : optHash.entrySet() ) {
            opts.add(String.format("%s=%s", URLEncoder.encode(o.getKey(), UTF_8), URLEncoder.encode(o.getValue(), UTF_8))); //$NON-NLS-1$
        }
        return StringUtils.join(opts, "&"); //$NON-NLS-1$
    }


    /**
     * @param brokerURL
     * @param userName
     * @param connFactory
     * @throws ServerConnectorException
     * @throws InterruptedException
     */
    private void greetServer ( String brokerURL, String userName, ActiveMQXAConnectionFactory connFactory )
            throws ServerConnectorException, InterruptedException {
        try {
            messageClientAvailable(brokerURL, userName, connFactory);
        }
        catch (
            MessagingException |
            ServerConnectorException t ) {
            log.debug(CONNECTION_ESTABLISH_FAILED, t);
            onException();
            throw new ServerConnectorException(CONNECTION_ESTABLISH_FAILED, t);
        }
    }


    /**
     * @param t
     * @throws ServerConnectorException
     */
    private void onException () throws ServerConnectorException {
        this.state = ServerConnectorState.ERROR;
        try {
            closeSystemClient();
        }
        catch ( MessagingException e ) {
            log.warn(CONNECTION_CLEAN_UP_FAILED, e);
        }

        try {
            closeSystemConnection();
        }
        catch ( JMSException e ) {
            log.warn(CONNECTION_CLEAN_UP_FAILED, e);
        }
    }


    /**
     * @throws JMSException
     */
    private void closeSystemConnection () throws JMSException {
        if ( this.systemConnection.isPresent() ) {
            try {
                @NonNull
                ActiveMQConnection connection = this.systemConnection.get();
                connection.removeTransportListener(this.serverTransportListener);
                try {
                    connection.close();
                }
                finally {
                    connection.cleanup();
                }
            }
            finally {
                this.systemConnection = Optional.empty();
                System.gc();
                System.runFinalization();
            }
        }
    }


    /**
     * @throws MessagingException
     */
    private void closeSystemClient () throws MessagingException {
        if ( this.systemClient.isPresent() ) {
            synchronized ( this ) {
                try {
                    MessagingClient<@NonNull TSource> client = this.systemClient.get();
                    client.close();
                }
                finally {
                    this.systemClient = Optional.empty();
                }
            }
        }
    }


    /**
     * @throws ServerConnectorException
     */
    private void openSystemClient () throws ServerConnectorException {

        MessagingClient<@NonNull TSource> sysClient = this.messagingClientFactory
                .createClient(this.getMessageSource(), new SystemConnectionFactory(this), getSystemEventRouter());

        try {
            sysClient.open();
            if ( this.systemClient.isPresent() ) {
                log.warn("System client not properly shutdown, this will leak memory"); //$NON-NLS-1$
                try {
                    this.systemClient.get().close();
                }
                catch ( Exception e ) {
                    log.warn("Failure trying to close unclosed system client", e); //$NON-NLS-1$
                }
            }
            this.systemClient = Optional.of(sysClient);
        }
        catch ( MessagingException e ) {
            log.error("Failed to open messaging client:", e); //$NON-NLS-1$
            onException();
            throw new ServerConnectorException("Failed to setup system client:", e); //$NON-NLS-1$
        }
    }


    protected @NonNull SystemEventRouterManager getSystemEventRouter () {
        return new SystemEventRouterManager(this.getSystemEventTopic());
    }


    /**
     * @return
     */
    protected abstract String getSystemEventTopic ();


    /**
     * @param doLogConnError
     * @param brokerURL
     * @param userName
     * @return
     * @throws ServerConnectorException
     */
    private ActiveMQXAConnectionFactory openSystemConnection ( boolean doLogConnError, String brokerURL, String userName,
            ServerConnectorState oldState ) throws ServerConnectorException {
        ActiveMQXAConnectionFactory connFactory = createConnectionFactory(brokerURL, userName);

        ActiveMQConnection conn = null;
        try {
            conn = (ActiveMQConnection) connFactory.createConnection();
            conn.addTransportListener(this.serverTransportListener);
            conn.start();
            if ( this.systemConnection.isPresent() ) {
                log.warn("System connection not properly shutdown, this might leak memory " + oldState); //$NON-NLS-1$
            }
            this.systemConnection = Optional.of(conn);
        }
        catch ( JMSException e ) {
            if ( doLogConnError ) {
                log.debug("Failed to connect to server:", e); //$NON-NLS-1$
            }
            this.state = ServerConnectorState.ERROR;

            try {
                if ( conn != null ) {
                    conn.close();
                }
            }
            catch ( JMSException ex ) {
                log.debug("Error closing system connection", ex); //$NON-NLS-1$
            }

            throw new ServerConnectorException("Failed to setup system connection:", e); //$NON-NLS-1$
        }
        return connFactory;
    }


    /**
     * @param brokerURL
     * @param userName
     * @return
     */
    protected ActiveMQXAConnectionFactory createConnectionFactory ( String brokerURL, String userName ) {
        ActiveMQXAConnectionFactory factory = new ActiveMQXASSLConnectionFactory(this.tlsContext, userName, null, brokerURL);
        ServerConnectorExceptionListener exceptionListener = new ServerConnectorExceptionListener();
        factory.setExceptionListener(exceptionListener);
        factory.setClientInternalExceptionListener(exceptionListener);
        factory.setAlwaysSyncSend(true);
        factory.setClientIDPrefix(this.buildUserName(this.getComponentId()));
        return factory;
    }


    /**
     * @param config
     * @throws ServerConnectorException
     */
    @SuppressWarnings ( "null" )
    @Override
    public void updateConfig ( TConfig config ) throws ServerConnectorException {
        if ( !config.getId().equals(this.connectorConfig.getComponentId()) ) {
            throw new ServerConnectorException("Server returned mismatching component id"); //$NON-NLS-1$
        }

        if ( this.configRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.configRegistration);
        }

        this.configRegistration = DsUtil.registerSafe(this.componentContext, getConfigClass(), config, null);

        if ( this.messageSourceRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.messageSourceRegistration);
        }

        this.messageSourceRegistration = DsUtil.registerSafe(this.componentContext, MessageSource.class, this.getMessageSource(), null);

        log.debug("Component configuration available"); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("WS base address: %s", config.getWebServiceBaseAddress())); //$NON-NLS-1$
            log.debug(String.format("Event out queue: %s", config.getEventOutQueue())); //$NON-NLS-1$
            log.debug(String.format("Event in topic: %s", config.getEventTopic())); //$NON-NLS-1$
        }

    }


    /**
     * @param brokerURL
     * @param userName
     * @param connFactory
     * @throws MessagingException
     * @throws ServerConnectorException
     * @throws InterruptedException
     * @throws Throwable
     * 
     */
    protected void messageClientAvailable ( String brokerURL, String userName, XAConnectionFactory connFactory )
            throws MessagingException, ServerConnectorException, InterruptedException {
        log.debug("Messaging client is ready"); //$NON-NLS-1$

        this.systemClient.get().publishEvent(this.createConnectingEvent());

        ComponentConfigResponse<TConfig> configResponse = this.systemClient.get().sendMessage(makeConfigRequest());

        if ( configResponse == null ) {
            throw new ServerConnectorException("No configuration response"); //$NON-NLS-1$
        }

        TConfig config = configResponse.getConfiguration();

        this.updateConfig(config);

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("url", brokerURL); //$NON-NLS-1$
        properties.put("user", userName); //$NON-NLS-1$
        properties.put("poolSize", 20); //$NON-NLS-1$
        properties.put("borrowTimeout", 10); //$NON-NLS-1$
        properties.put(
            "resourceName", //$NON-NLS-1$
            "sconn-" + this.getComponentId()); //$NON-NLS-1$
        this.connectionFactoryRegistration = DsUtil.registerSafe(this.componentContext, XAConnectionFactory.class, connFactory, properties);

        // delay connected event until the application is up

        ComponentPinger pinger = new ComponentPinger(this, config.getPingTimeout() / 2);
        Dictionary<String, Object> pingerProperties = new Hashtable<>();
        pingerProperties.put(JobProperties.JOB_TYPE, ComponentPinger.class.getName());
        this.pingerRegistration = DsUtil.registerSafe(this.componentContext, TriggeredJob.class, pinger, pingerProperties);
    }


    /**
     * @throws MessagingException
     * @throws InterruptedException
     */
    void connected () throws MessagingException, InterruptedException {
        this.state = ServerConnectorState.CONNECTED;
        @NonNull
        EventMessage<@NonNull TSource> connectedEvent = this.createConnectedEvent();
        this.systemClient.get().publishEvent(connectedEvent);
    }


    protected void onConnectionError () {
        this.state = ServerConnectorState.ERROR;
        this.executor.execute(new Runnable() {

            @Override
            public void run () {
                doCloseOnError();
            }
        });

    }


    protected synchronized void doCloseOnError () {
        if ( this.state != ServerConnectorState.ERROR ) {
            // something already changed the state before we got executed
            return;
        }

        if ( this.connectionFactoryRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.connectionFactoryRegistration);
            this.connectionFactoryRegistration = null;
        }

        if ( this.configRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.configRegistration);
            this.configRegistration = null;
        }

        if ( this.pingerRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.pingerRegistration);
            this.pingerRegistration = null;
        }

        try {
            closeSystemClient();

        }
        catch ( MessagingException e ) {
            log.debug(CONNECTION_CLEAN_UP_FAILED, e);
            log.warn("Connection closed with error"); //$NON-NLS-1$
        }

        try {
            closeSystemConnection();
        }
        catch ( JMSException e ) {
            log.debug(CONNECTION_CLEAN_UP_FAILED, e);
            log.warn("Connection closed with error"); //$NON-NLS-1$
        }

    }


    /**
     * 
     */
    public void interrupted () {
        onConnectionError();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#disconnect()
     */
    @Override
    public void disconnect () throws ServerConnectorException {
        disconnect(getMessageSource(), false);
    }


    /**
     * @throws ServerConnectorException
     * 
     */
    @Override
    public void serverShutdown () throws ServerConnectorException {
        disconnect(getMessageSource(), true);
    }


    /**
     * 
     * @param ms
     * @throws ServerConnectorException
     */
    void disconnect ( TSource ms, boolean hard ) throws ServerConnectorException {
        synchronized ( this.connectionLock ) {
            this.state = ServerConnectorState.DISCONNECTING;

            if ( this.connectionFactoryRegistration != null ) {
                DsUtil.unregisterSafe(this.componentContext, this.connectionFactoryRegistration);
                this.connectionFactoryRegistration = null;
            }

            if ( this.pingerRegistration != null ) {
                DsUtil.unregisterSafe(this.componentContext, this.pingerRegistration);
                this.pingerRegistration = null;
            }

            if ( !hard && this.systemClient.isPresent() && this.systemConnection.isPresent() ) {
                try {
                    this.systemClient.get().publishEvent(makeDisconnectingEvent(ms));
                }
                catch (
                    MessagingException |
                    InterruptedException e ) {
                    log.debug("Failed to publish disconnect event:", e); //$NON-NLS-1$
                }
            }

            try {
                this.closeSystemClient();
            }
            catch ( MessagingException e ) {
                log.warn("Failed to close system messaging client:", e); //$NON-NLS-1$
            }

            try {
                this.closeSystemConnection();
            }
            catch ( JMSException e ) {
                log.warn("Failed to close system server connection:", e); //$NON-NLS-1$
            }

            log.info("Disconnected"); //$NON-NLS-1$
            if ( hard ) {
                this.state = ServerConnectorState.ERROR;
            }
            else {
                this.state = ServerConnectorState.DISCONNECTED;
            }
        }
    }


    @Override
    public void reconnect () throws ServerConnectorException, InterruptedException {
        this.disconnect();
        this.connect();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#getState()
     */
    @Override
    public ServerConnectorState getState () {
        return this.state;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#getComponentId()
     */
    @Override
    @NonNull
    public UUID getComponentId () {
        return this.connectorConfig.getComponentId();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#getServerAddress()
     */
    @Override
    public String getServerAddress () {
        return String.format("%s:%d", this.connectorConfig.getServerAddress(), this.connectorConfig.getServerPort()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#sendPing()
     */
    @Override
    public void sendPing () {
        if ( this.systemClient != null ) {
            try {
                log.trace("Sending ping"); //$NON-NLS-1$
                this.systemClient.get().sendMessage(this.makePingEvent());
            }
            catch ( NoSuchElementException e ) {
                log.debug("No client available", e); //$NON-NLS-1$
            }
            catch ( CallErrorException e ) {
                if ( e.getCause() instanceof ComponentIllegalConnStateException ) {
                    log.trace("Illegal conn state", e); //$NON-NLS-1$
                    log.info("Recieved illegal conn state, reconnecting"); //$NON-NLS-1$
                    try {
                        this.reconnect();
                    }
                    catch (
                        ServerConnectorException |
                        InterruptedException e1 ) {
                        log.warn("Failed to reconnect after illegal conn state", e1); //$NON-NLS-1$
                    }
                    return;
                }

                log.warn("Unknown call error", e.getCause()); //$NON-NLS-1$
            }
            catch ( MessagingException e ) {
                onPingFailure(e);
            }
            catch ( InterruptedException e ) {
                log.debug("Ping interrupted", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param e
     */
    private void onPingFailure ( MessagingException e ) {

        if ( this.state == ServerConnectorState.DISCONNECTED || this.state == ServerConnectorState.DISCONNECTING ) {
            return;
        }

        log.trace("Failed to ping", e); //$NON-NLS-1$
        log.info("Could not ping server, reconnecting"); //$NON-NLS-1$
        try {
            this.reconnect();
        }
        catch (
            ServerConnectorException |
            InterruptedException e1 ) {
            log.debug("Failed to disconnect after ping failure", e1); //$NON-NLS-1$
            try {
                onException();
            }
            catch ( ServerConnectorException e2 ) {
                log.warn("Failed to clean up connection", e2); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return the system connection
     */
    Connection getSystemConnection () {
        return this.systemConnection.get();
    }

}

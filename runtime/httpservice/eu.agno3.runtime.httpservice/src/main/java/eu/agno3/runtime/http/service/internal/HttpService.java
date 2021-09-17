/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2013 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.http.service.ActiveHandler;
import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.http.service.HttpServiceInfo;
import eu.agno3.runtime.http.service.ProxiableConnectorFactory;
import eu.agno3.runtime.http.service.ReverseProxyConfig;
import eu.agno3.runtime.http.service.connector.ConnectorFactory;
import eu.agno3.runtime.http.service.handler.ExtendedHandler;
import eu.agno3.runtime.http.service.handler.JettyLoggerBridge;
import eu.agno3.runtime.util.net.LocalHostUtil;
import eu.agno3.runtime.util.osgi.DsUtil;
import eu.agno3.runtime.util.threads.NamedThreadFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    HttpServiceInfo.class
}, configurationPid = HttpServiceConfig.PID, property = {
    HttpServiceConfig.CONNECTORS + "=" + HttpServiceConfig.CONNECTOR_DEFAULT
}, immediate = true )
public class HttpService implements HttpServiceInfo, ServiceTrackerCustomizer<Handler, HandlerRegistrationHolder> {

    private static final Logger log = Logger.getLogger(HttpService.class);

    private Server server;

    private boolean active = false;

    private Map<String, ConnectorFactory> connectorFactories = new HashMap<>();
    private Map<String, List<ServerConnector>> connectorSet = new HashMap<>();
    private Map<String, Boolean> modifiedConnectors = new HashMap<>();
    private GuardedHandlerCollection contextHandlers = new GuardedHandlerCollection();

    private String serverName;

    private ServiceTracker<Handler, HandlerRegistrationHolder> handlerTracker;

    private ComponentContext componentContext;

    private ExecutorService initExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("HttpService-Init")); //$NON-NLS-1$

    static {
        Log.setLog(new JettyLoggerBridge(Logger.getLogger(HttpService.class)));
    }


    /**
     * 
     */
    public HttpService () {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName(this.getClass().getSimpleName());
        this.server = new Server(threadPool);

        MBeanContainer mbc = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        this.server.addEventListener(mbc);
        this.server.addBean(mbc);

        this.server.setHandler(this.contextHandlers);
        this.server.insertHandler(new StatisticsHandler());
        this.contextHandlers.setHandlers(new Handler[] {
            new ExtendedDefaultHandler()
        });
    }


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        this.active = true;
        this.componentContext = context;
        this.handlerTracker = new ServiceTracker<>(context.getBundleContext(), Handler.class, this);
        this.handlerTracker.open();

        String hostnameOverride = (String) context.getProperties().get("serverName"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(hostnameOverride) ) {
            this.serverName = hostnameOverride.trim();
        }
        else {
            this.serverName = LocalHostUtil.guessPrimaryHostName();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Starting HttpService, found serverName " + this.serverName); //$NON-NLS-1$
        }
        setupConnectors(false);

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        log.info("Shutting down HttpService"); //$NON-NLS-1$

        if ( this.handlerTracker != null ) {
            this.handlerTracker.close();
        }

        this.active = false;
        this.connectorFactories.clear();

        this.server.setStopTimeout(1000);
        this.setupConnectors(true);
        try {
            this.server.stop();
            this.server.join();
        }
        catch ( Exception e ) {
            log.error("Failed to shutdown HttpService:", e); //$NON-NLS-1$
        }

        this.server = null;
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setGuardHandler ( GuardHandlerImpl gh ) {
        try {
            gh.setServer(this.server);
            gh.start();
            this.contextHandlers.setGuardHandler(gh);
        }
        catch ( Exception e ) {
            log.warn("Failed to start guard handler", e); //$NON-NLS-1$
        }
    }


    protected synchronized void unsetGuardHandler ( GuardHandlerImpl gh ) {
        if ( this.contextHandlers.getGuardHandler() == gh ) {
            this.contextHandlers.setGuardHandler(null);
            try {
                gh.stop();
                gh.setServer(null);
            }
            catch ( Exception e ) {
                log.warn("Failed to stop guard handler", e); //$NON-NLS-1$
            }
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void bindMBeanServer ( MBeanServer mbs ) {

    }


    protected synchronized void unbindMBeanServer ( MBeanServer mbs ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public HandlerRegistrationHolder addingService ( ServiceReference<Handler> ref ) {
        Handler handler = this.componentContext.getBundleContext().getService(ref);
        if ( handler instanceof ExtendedHandler ) {
            ExtendedHandler eh = (ExtendedHandler) handler;

            logHandlerInfo(handler, eh);

            if ( ! ( handler instanceof ContextHandler ) ) {
                ContextHandler ctxHandler = new ContextHandler( ( (ExtendedHandler) handler ).getContextPath());
                ctxHandler.setHandler(handler);
                return addAndStartHandler(handler, ref);
            }
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding native handler: %s", handler.getClass().getName())); //$NON-NLS-1$
            }
        }

        return addAndStartHandler(handler, ref);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<Handler> ref, HandlerRegistrationHolder reg ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<Handler> ref, HandlerRegistrationHolder holder ) {
        ServiceRegistration<ActiveHandler> reg = holder != null ? holder.fetch() : null;
        if ( reg != null ) {
            DsUtil.unregisterSafe(this.componentContext, reg);
            Handler service = this.componentContext.getBundleContext().getService(ref);
            try {
                unbindHandler(service);
            }
            finally {
                this.componentContext.getBundleContext().ungetService(ref);
            }
        }
    }


    private HandlerRegistrationHolder addAndStartHandler ( Handler handler, ServiceReference<Handler> ref ) {
        HandlerRegistrationHolder holder = new HandlerRegistrationHolder();
        this.initExecutor.execute(new Runnable() {

            @Override
            public void run () {
                doStartHandler(handler, ref, holder);
            }

        });
        return holder;
    }


    /**
     * @param handler
     * @param ref
     * @param holder
     */
    void doStartHandler ( Handler handler, ServiceReference<Handler> ref, HandlerRegistrationHolder holder ) {
        try {
            handler.setServer(this.server);
            ClassLoader otccl = Thread.currentThread().getContextClassLoader();
            try {
                Handler h = handler;

                while ( h instanceof HandlerWrapper && ! ( h instanceof ContextHandler ) ) {
                    Handler wrapped = ( (HandlerWrapper) h ).getHandler();

                    if ( wrapped != null ) {
                        h = wrapped;
                    }
                    else {
                        break;
                    }
                }

                if ( h instanceof ContextHandler ) {
                    ClassLoader wcl = ( (ContextHandler) h ).getClassLoader();
                    Thread.currentThread().setContextClassLoader(wcl);
                }
                handler.start();

                this.contextHandlers.addHandler(handler);

                Dictionary<String, Object> handlerProps = new Hashtable<>();
                String[] copyProps = new String[] {
                    "handler.id" //$NON-NLS-1$
                };
                for ( String copy : copyProps ) {
                    Object property = ref.getProperty(copy);
                    if ( property != null ) {
                        handlerProps.put(copy, property);
                    }
                }
                holder.set(DsUtil.registerSafe(this.componentContext, ActiveHandler.class, new ActiveHandlerImpl(), handlerProps));
            }
            finally {
                Thread.currentThread().setContextClassLoader(otccl);
            }
        }
        catch ( Exception e ) {
            log.error("Failed to start context handler", e); //$NON-NLS-1$
            this.contextHandlers.removeHandler(handler);
        }
    }


    /**
     * @param handler
     * @param eh
     */
    private static void logHandlerInfo ( Handler handler, ExtendedHandler eh ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Adding extended handler for scope %s: %s", eh.getContextPath(), handler.getClass().getName())); //$NON-NLS-1$
            if ( eh.getVirtualHosts() != null ) {
                for ( String virtualHost : eh.getVirtualHosts() ) {
                    log.debug(String.format(" bound to virtual host " + virtualHost)); //$NON-NLS-1$
                }
            }
        }
    }


    protected synchronized void unbindHandler ( Handler handler ) {
        if ( handler instanceof ExtendedHandler ) {
            ExtendedHandler eh = (ExtendedHandler) handler;
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Removing context handler for scope %s: %s", eh.getContextPath(), handler.getClass().getName())); //$NON-NLS-1$
            }
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Removing native handler: %s", handler.getClass().getName())); //$NON-NLS-1$
            }
        }

        this.contextHandlers.removeHandler(handler);

        try {
            handler.stop();
            handler.destroy();
        }
        catch ( Exception e ) {
            log.error("Failed to stop context handler", e); //$NON-NLS-1$
        }
    }


    @Override
    public List<ContextHandler> getContextHandlers () {
        Set<ContextHandler> res = new HashSet<>();
        Handler[] handlerArray = this.contextHandlers.getHandlers();
        if ( handlerArray == null || handlerArray.length == 0 ) {
            return new ArrayList<>();
        }

        List<Handler> handlers = Arrays.asList(handlerArray);

        for ( Handler handler : handlers ) {
            if ( ! ( handler instanceof ContextHandler ) ) {
                continue;
            }

            res.add((ContextHandler) handler);
        }

        return new ArrayList<>(res);
    }


    /**
     * 
     */
    private boolean setupConnectors ( boolean shutdown ) {
        boolean modified = false;

        modified |= removeConnectors();

        if ( !shutdown ) {
            modified |= addConnectors();
            modified |= reconfigureConnectors();
        }

        if ( modified ) {
            List<ServerConnector> all = new ArrayList<>();
            for ( List<ServerConnector> connectors : this.connectorSet.values() ) {
                all.addAll(connectors);
            }
            this.server.setConnectors(all.toArray(new Connector[] {}));

        }

        if ( !shutdown ) {
            startServer();
        }

        return modified;
    }


    /**
     * 
     */
    private void startServer () {
        if ( !this.connectorFactories.isEmpty() ) {
            try {
                if ( !this.server.isStarted() ) {
                    this.server.start();
                }
            }
            catch ( Exception e ) {
                log.error("Failed to activate HttpService:", e); //$NON-NLS-1$
            }
        }
        else {
            log.debug("No connectorFactories available"); //$NON-NLS-1$
        }

        for ( Connector c : this.server.getConnectors() ) {
            if ( c.getTransport() == null || !c.isRunning() ) {
                try {
                    c.start();
                }
                catch ( Exception e ) {
                    log.error("Failed to start connector on second attempt:", e); //$NON-NLS-1$
                }
                log.debug("Connector did not start the first time"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param modified
     * @return
     */
    private boolean reconfigureConnectors () {
        boolean modified = false;
        for ( Entry<String, Boolean> entry : this.modifiedConnectors.entrySet() ) {

            if ( entry.getValue() ) {
                if ( log.isInfoEnabled() ) {
                    log.info("Restarting connector " + entry.getKey()); //$NON-NLS-1$
                }

                List<ServerConnector> connectors = this.connectorSet.get(entry.getKey());
                for ( ServerConnector oldConnector : connectors ) {
                    try {
                        shutdownConnector(oldConnector);
                    }
                    finally {
                        oldConnector.close();
                    }
                }

                modified |= this.addConnector(entry.getKey(), this.connectorFactories.get(entry.getKey()));
                this.modifiedConnectors.put(entry.getKey(), false);
            }
        }
        return modified;
    }


    /**
     * @param modified
     * @return
     */
    private boolean addConnectors () {
        boolean modified = false;
        for ( Entry<String, ConnectorFactory> entry : this.connectorFactories.entrySet() ) {
            if ( this.connectorSet.containsKey(entry.getKey()) ) {
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding connector for " + entry.getKey()); //$NON-NLS-1$
            }
            modified |= addConnector(entry.getKey(), entry.getValue());
        }
        return modified;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.HttpServiceInfo#getActiveConnectors(java.lang.String)
     */
    @Override
    public List<ServerConnector> getActiveConnectors ( String key ) {
        return this.connectorSet.get(key);
    }


    /**
     * @param modified
     * @param entry
     * @return
     */
    private boolean addConnector ( String key, ConnectorFactory connectorFactory ) {
        List<ServerConnector> connectors = connectorFactory.createConnectors(this.server);
        try {
            for ( Connector connector : connectors ) {
                ( (ServerConnector) connector ).open();
                if ( log.isDebugEnabled() ) {
                    log.debug("Started connector for " + key); //$NON-NLS-1$
                }
            }
        }
        catch ( Exception e ) {
            log.error("Failed to start connector:", e); //$NON-NLS-1$
            for ( Connector connector : connectors ) {
                ( (ServerConnector) connector ).close();
            }
            return false;
        }

        this.connectorSet.put(key, connectors);
        return true;
    }


    /**
     * @param modified
     * @return
     */
    private boolean removeConnectors () {
        boolean modified = false;
        List<String> toRemove = new ArrayList<>();
        for ( Entry<String, List<ServerConnector>> entry : this.connectorSet.entrySet() ) {
            if ( this.connectorFactories.containsKey(entry.getKey()) ) {
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Removing connector for " + entry.getKey()); //$NON-NLS-1$
            }

            for ( ServerConnector conn : entry.getValue() ) {
                try {
                    shutdownConnector(conn);
                }
                catch ( Exception e ) {
                    log.warn("Failed to close connector", e); //$NON-NLS-1$
                    continue;
                }
            }

            toRemove.add(entry.getKey());
        }
        for ( String remove : toRemove ) {
            this.connectorSet.remove(remove);
        }
        return modified;
    }


    /**
     * @param connector
     */
    private static void shutdownConnector ( ServerConnector connector ) {
        if ( connector != null ) {
            try {
                connector.shutdown().get();
                connector.getSelectorManager().stop();
            }
            catch ( Exception e ) {
                log.warn("Connector shutdown interrupted:", e); //$NON-NLS-1$
            }
            connector.destroy();
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, updated = "updatedConnectorFactory" )
    protected synchronized void bindConnectorFactory ( ConnectorFactory cf ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting connector factory for " + cf.getConnectorName()); //$NON-NLS-1$
        }
        if ( this.connectorFactories.put(cf.getConnectorName(), cf) != null ) {
            log.error("Multiple connectors with name " + cf.getConnectorName()); //$NON-NLS-1$
            return;
        }

        if ( this.active ) {
            this.setupConnectors(false);
        }
    }


    protected synchronized void unbindConnectorFactory ( ConnectorFactory cf ) {
        ConnectorFactory stored = this.connectorFactories.get(cf.getConnectorName());
        if ( cf == stored ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing connector factory " + cf.getConnectorName()); //$NON-NLS-1$
            }
            this.connectorFactories.remove(cf.getConnectorName());
        }
        else if ( stored != null ) {
            log.warn("Trying to unbind connector with same name, but not same instance"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Stored is " + stored); //$NON-NLS-1$
                log.debug("Given is " + cf); //$NON-NLS-1$
            }
        }

        if ( this.active ) {
            this.setupConnectors(false);
        }
    }


    protected synchronized void updatedConnectorFactory ( ConnectorFactory cf ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Connector factory update for " + cf.getConnectorName()); //$NON-NLS-1$
        }
        this.modifiedConnectors.put(cf.getConnectorName(), true);

        if ( this.active ) {
            this.setupConnectors(false);
        }
    }


    @Override
    public List<ConnectorFactory> getConnectorFactories () {
        return new ArrayList<>(this.connectorFactories.values());

    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServletException
     *
     * @see eu.agno3.runtime.http.service.HttpServiceInfo#getContextBaseUrl(java.lang.String,
     *      javax.servlet.ServletContext, java.lang.String)
     */

    @Override
    public URL getContextBaseUrl ( String key, ServletContext ctx, String overrideHostname ) throws ServletException {
        return getContextBaseUrl(key, ctx.getContextPath(), overrideHostname);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.HttpServiceInfo#getContextBaseUrl(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public URL getContextBaseUrl ( String key, String contextPath, String overrideHostname ) throws ServletException {
        List<ServerConnector> activeConnector = this.getActiveConnectors(key);
        ConnectorFactory cf = this.connectorFactories.get(key);

        if ( activeConnector == null || activeConnector.isEmpty() || cf == null ) {
            throw new ServletException("Failed to find connector for the given context " + key); //$NON-NLS-1$
        }

        ReverseProxyConfig proxyConfig = null;
        if ( cf instanceof ProxiableConnectorFactory ) {
            proxyConfig = ( (ProxiableConnectorFactory) cf ).getReverseProxyConfig();
        }

        try {
            // the connectors all have the same configuration, so we just use the first one
            URL u = makeBaseUrl(contextPath, activeConnector.get(0), overrideHostname, proxyConfig);
            if ( log.isTraceEnabled() ) {
                log.trace("Base address is " + u); //$NON-NLS-1$
            }
            return u;
        }
        catch ( MalformedURLException e ) {
            throw new ServletException("Failed to build context base URL", e); //$NON-NLS-1$
        }
    }


    /**
     * @param contextPath
     * @param sc
     * @param overrideHostname
     * @param proxyConfig
     * @return
     * @throws MalformedURLException
     * @throws ServletException
     */
    protected URL makeBaseUrl ( String contextPath, ServerConnector sc, String overrideHostname, ReverseProxyConfig proxyConfig )
            throws MalformedURLException, ServletException {
        return new URL(getScheme(sc, proxyConfig), getHostName(sc, proxyConfig, overrideHostname), getPort(sc.getPort(), proxyConfig), contextPath);
    }


    /**
     * @param port
     * @param proxyConfig
     * @return
     */
    private static int getPort ( int port, ReverseProxyConfig proxyConfig ) {
        if ( proxyConfig != null && proxyConfig.getOverridePort() != null ) {
            return proxyConfig.getOverridePort();
        }
        return port;
    }


    /**
     * @param sc
     * @param proxyConfig
     * @param overrideHostname
     * @return
     * @throws ServletException
     */
    protected String getHostName ( ServerConnector sc, ReverseProxyConfig proxyConfig, String overrideHostname ) throws ServletException {
        if ( overrideHostname != null ) {
            return overrideHostname;
        }

        if ( proxyConfig != null && !StringUtils.isBlank(proxyConfig.getOverrideHost()) ) {
            return proxyConfig.getOverrideHost();
        }

        if ( isLocalHost(sc.getHost()) ) {
            return "localhost"; //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(this.serverName) ) {
            throw new ServletException("Failed to determine server name"); //$NON-NLS-1$
        }

        return this.serverName;
    }


    /**
     * @param host
     * @return
     */
    private static boolean isLocalHost ( String host ) {
        if ( "localhost".equals(host) ) { //$NON-NLS-1$
            return true;
        }
        else if ( "127.0.0.1".equals(host) ) { //$NON-NLS-1$
            return true;
        }
        return false;
    }


    /**
     * @param sc
     * @param proxyConfig
     * @return
     */
    protected String getScheme ( ServerConnector sc, ReverseProxyConfig proxyConfig ) {

        if ( proxyConfig != null && proxyConfig.getOverrideScheme() != null ) {
            return proxyConfig.getOverrideScheme();
        }

        if ( "SSL".equalsIgnoreCase(sc.getDefaultProtocol()) ) { //$NON-NLS-1$
            return "https"; //$NON-NLS-1$
        }
        return "http"; //$NON-NLS-1$
    }
}

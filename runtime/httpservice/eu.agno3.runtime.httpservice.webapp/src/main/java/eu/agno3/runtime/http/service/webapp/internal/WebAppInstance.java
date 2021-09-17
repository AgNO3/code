/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.osgi.boot.OSGiWebInfConfiguration;
import org.eclipse.jetty.osgi.boot.OSGiWebappConstants;
import org.eclipse.jetty.osgi.boot.utils.BundleFileLocatorHelperFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.FragmentDescriptor;
import org.eclipse.jetty.webapp.StandardDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.http.service.HttpConfigurationException;
import eu.agno3.runtime.http.service.handler.ContextHandlerConfig;
import eu.agno3.runtime.http.service.logging.LoggingRequestLog;
import eu.agno3.runtime.http.service.session.SessionManagerFactory;
import eu.agno3.runtime.http.service.webapp.WebAppConfiguration;
import eu.agno3.runtime.http.service.webapp.WebAppDependencies;
import eu.agno3.runtime.util.osgi.BundleUtil;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component (
    service = WebAppInstance.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    configurationPid = WebAppInstance.PID )
public class WebAppInstance implements BundleTrackerCustomizer<Object> {

    private static final Logger log = Logger.getLogger(WebAppInstance.class);

    /**
     * 
     */
    public static final String PID = "httpservice.webapp.instance"; //$NON-NLS-1$

    private static final String WEB_FRAGMENT_XML = "web-fragment.xml"; //$NON-NLS-1$
    private static final String META_INF = "/META-INF/"; //$NON-NLS-1$

    private WebAppConfiguration config;
    private SessionManagerFactory sessionManagerFactory;
    private ComponentContext componentContext;
    private BundleTracker<Object> webappBundleTracker;
    private Map<Bundle, ServiceRegistration<Handler>> handlers = new HashMap<>();


    @Reference ( updated = "updatedWebAppConfiguration" )
    protected synchronized void setWebAppConfiguration ( WebAppConfiguration cfg ) {
        this.config = cfg;
    }


    protected synchronized void updatedWebAppConfiguration ( WebAppConfiguration cfg ) {
        log.debug("Modified WebAppConfiguration"); //$NON-NLS-1$
        for ( ServiceRegistration<Handler> reg : this.handlers.values() ) {
            Handler service = unwrapHandler(this.componentContext.getBundleContext().getService(reg.getReference()));
            if ( service instanceof WebAppContext ) {
                WebAppContext wctx = (WebAppContext) service;
                wctx.getServletContext().setAttribute("refresh", System.currentTimeMillis()); //$NON-NLS-1$
            }
            else {
                log.warn("Handler is not WebApp " + //$NON-NLS-1$
                        ( service != null ? service.getClass().getName() : StringUtils.EMPTY ));
            }
        }
    }


    /**
     * @param service
     * @return
     */
    private static Handler unwrapHandler ( Handler service ) {
        Handler cur = service;
        while ( cur instanceof HandlerWrapper && ! ( cur instanceof WebAppContext ) ) {
            cur = ( (HandlerWrapper) cur ).getHandler();
        }
        return cur;
    }


    protected synchronized void unsetWebAppConfiguration ( WebAppConfiguration cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    // dependency only
    @Reference
    protected synchronized void setWebAppDependencies ( WebAppDependencies deps ) {}


    protected synchronized void unsetWebAppDependencies ( WebAppDependencies deps ) {

    }


    @Reference
    protected synchronized void setSessionManagerFactory ( SessionManagerFactory smf ) {
        this.sessionManagerFactory = smf;
    }


    protected synchronized void unsetSessionManagerFactory ( SessionManagerFactory smf ) {
        if ( this.sessionManagerFactory == smf ) {
            this.sessionManagerFactory = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.componentContext = context;
        this.webappBundleTracker = new BundleTracker<>(context.getBundleContext(), Bundle.ACTIVE, this);
        this.webappBundleTracker.open();
    }


    @Modified
    protected void modified ( ComponentContext context ) {
        log.debug("Modified WebAppInstance"); //$NON-NLS-1$
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( this.webappBundleTracker != null ) {
            this.webappBundleTracker.close();
            this.webappBundleTracker = null;
        }

        for ( Bundle b : this.handlers.keySet() ) {
            this.deactivateBundle(b);
        }

        this.componentContext = null;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param bundle
     */
    private synchronized void deactivateBundle ( Bundle bundle ) {

        ServiceRegistration<Handler> bundleHandler = this.handlers.remove(bundle);

        if ( bundleHandler == null ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Deactivating webapp bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        Handler h = this.componentContext.getBundleContext().getService(bundleHandler.getReference());

        if ( h instanceof GzipHandler ) {
            h = ( (GzipHandler) h ).getHandler();
        }

        SessionHandler sh = null;
        if ( ( h instanceof WebAppContext ) ) {
            sh = ( (WebAppContext) h ).getSessionHandler();
        }

        DsUtil.unregisterSafe(this.componentContext, bundleHandler);

        try {
            h.stop();
        }
        catch ( Exception e ) {
            log.warn("Failed to stop context:", e); //$NON-NLS-1$
        }

        if ( ( h instanceof WebAppContext ) ) {
            saveSessions((WebAppContext) h, sh);
        }
    }


    /**
     * @param h
     * @param sm
     */
    protected void saveSessions ( WebAppContext h, SessionHandler sm ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Saving sessions..."); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(h.getClassLoader());
            sm.stop();
            sm.destroy();
        }
        catch ( Exception e ) {
            log.warn("Failed to save sessions", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @param bundle
     * @throws HttpConfigurationException
     */
    private synchronized void activateBundle ( Bundle bundle ) {

        String contextPath = bundle.getHeaders().get("Webapp-Context"); //$NON-NLS-1$

        if ( contextPath == null ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Activating webapp bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        try {
            WebAppContext contextHandler = createContextHandler(bundle, contextPath);
            overrideInitConfig(bundle, contextHandler);
            Dictionary<String, Object> chProperties = new Hashtable<>();
            chProperties.put("handler.id", this.config.getBundleSymbolicName()); //$NON-NLS-1$
            chProperties.put("webapp.bundle", this.config.getBundleSymbolicName()); //$NON-NLS-1$
            Handler wrap = wrap(contextHandler);
            this.handlers.put(bundle, DsUtil.registerSafe(this.componentContext, Handler.class, wrap, chProperties));
        }
        catch ( HttpConfigurationException e ) {
            log.error("Failed to setup context handler:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param contextHandler
     * @return
     */
    private static Handler wrap ( WebAppContext contextHandler ) {
        Handler h = contextHandler;
        String compressResources = contextHandler.getServletContext().getInitParameter("compress-resources"); //$NON-NLS-1$
        if ( compressResources != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Enabling GZIP on " + compressResources); //$NON-NLS-1$
            }
            String compressTypes = contextHandler.getServletContext().getInitParameter("compress-resource-types"); //$NON-NLS-1$
            String[] resources = StringUtils.split(compressResources, ',');

            GzipHandler gh = new GzipHandler();
            if ( compressTypes != null ) {
                String[] mimeTypes = StringUtils.split(compressTypes, ',');
                gh.setIncludedMimeTypes(mimeTypes);
            }
            gh.setCompressionLevel(6);
            gh.setMinGzipSize(4096);
            gh.setIncludedMethods("GET"); //$NON-NLS-1$
            gh.setIncludedPaths(resources);
            gh.insertHandler(contextHandler);
            h = gh;
        }

        return h;
    }


    /**
     * @param bundle
     * @param contextHandler
     */
    private void overrideInitConfig ( Bundle bundle, WebAppContext contextHandler ) {
        if ( this.config != null ) {
            for ( Entry<String, String> entry : this.config.getProperties().entrySet() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Setting init Parameter %s = %s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
                }
                contextHandler.setInitParameter(entry.getKey(), entry.getValue());
            }

            String contextPath = this.config.getProperties().get(ContextHandlerConfig.CONTEXT_PATH_ATTR);
            if ( contextPath != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Setting context path " + contextPath); //$NON-NLS-1$
                }
                contextHandler.setContextPath(contextPath);
            }

            String virtualHostsSpec = this.config.getProperties().get(ContextHandlerConfig.VIRTUAL_HOSTS_ATTR);
            if ( virtualHostsSpec != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Setting virtual hosts " + virtualHostsSpec); //$NON-NLS-1$
                }
                contextHandler.setVirtualHosts(virtualHostsSpec.split(Pattern.quote(","))); //$NON-NLS-1$
            }

            String displayName = this.config.getProperties().get(ContextHandlerConfig.DISPLAY_NAME_ATTR);
            if ( displayName != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Setting display name " + displayName); //$NON-NLS-1$
                }
                contextHandler.setDisplayName(displayName);
            }

            String tempDir = this.config.getProperties().get(ContextHandlerConfig.TEMP_DIR);
            if ( !StringUtils.isBlank(tempDir) ) {
                Path path = Paths.get(tempDir);
                if ( !Files.exists(path) ) {
                    try {
                        Files.createDirectories(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"))); //$NON-NLS-1$
                    }
                    catch ( IOException e ) {
                        log.error("Failed to create temporary directory for jetty", e); //$NON-NLS-1$
                    }
                }
                contextHandler.setAttribute("javax.servlet.context.tempdir", tempDir); //$NON-NLS-1$
            }
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("No configuration found for " + bundle.getSymbolicName()); //$NON-NLS-1$
        }
    }


    /**
     * @param bundle
     * @param contextPath
     * @return
     * @throws HttpConfigurationException
     */
    @SuppressWarnings ( "resource" )
    private WebAppContext createContextHandler ( Bundle bundle, String contextPath ) throws HttpConfigurationException {
        OSGiWebInfConfiguration cfg = new OSGiWebInfConfiguration();
        WebAppContext contextHandler = new ExtendedWebAppContext(bundle.getSymbolicName(), contextPath);
        contextHandler.getServletContext().setAttribute("context.bundle", bundle); //$NON-NLS-1$
        contextHandler.setAttribute(OSGiWebappConstants.JETTY_OSGI_BUNDLE, bundle);
        eu.agno3.runtime.http.service.webapp.internal.OSGiClassLoader classLoader = null;

        // Otherwise resources from symlinked directories cannot be loaded
        contextHandler.addAliasCheck(new AllowSymLinkAliasChecker());

        try {
            createRootResource(bundle, contextHandler);

            classLoader = createClassLoader(bundle, contextHandler);

            addBundleFragmentFragments(bundle, contextHandler);
            addRequiredBundleFragments(bundle, contextHandler);
            contextHandler.getMetaData().setWebXml(contextHandler.getWebInf().addPath("web.xml")); //$NON-NLS-1$
            contextHandler.getMetaData().orderFragments();

            cfg.configure(contextHandler);
            contextHandler.setDefaultsDescriptor(null);

            StandardDescriptorProcessor standardDescriptorProcessor = new StandardDescriptorProcessor();
            for ( FragmentDescriptor descriptor : contextHandler.getMetaData().getFragments() ) {
                setupInitParams(contextHandler, standardDescriptorProcessor, descriptor);
            }
            setupInitParams(contextHandler, standardDescriptorProcessor, contextHandler.getMetaData().getWebXml());

            setupProtectedTargets(contextHandler);

        }
        catch ( Exception e ) {
            if ( classLoader != null ) {
                try {
                    classLoader.close();
                }
                catch ( IOException e1 ) {
                    log.error("Failed to close webapp classloader:", e1); //$NON-NLS-1$
                }
            }
            throw new HttpConfigurationException("Failed to setup webapp context handler:", e); //$NON-NLS-1$
        }

        RequestLogHandler reqLog = new RequestLogHandler();
        reqLog.setRequestLog(new LoggingRequestLog());
        contextHandler.insertHandler(reqLog);

        if ( contextHandler.getErrorHandler() instanceof ErrorPageErrorHandler ) {
            contextHandler.setErrorHandler(new BypassableErrorHandler(new ErrorHandler()));
        }
        setupSessionHandler(bundle, contextHandler);
        return contextHandler;
    }


    void setupInitParams ( WebAppContext contextHandler, StandardDescriptorProcessor standardDescriptorProcessor, Descriptor descriptor ) {
        if ( descriptor == null ) {
            return;
        }
        XmlParser.Node root = descriptor.getRoot();
        Iterator<?> iter = root.iterator();
        XmlParser.Node node = null;
        while ( iter.hasNext() ) {
            Object o = iter.next();
            if ( ! ( o instanceof XmlParser.Node ) )
                continue;
            node = (XmlParser.Node) o;
            if ( "context-param".equals(node.getTag()) ) { //$NON-NLS-1$
                standardDescriptorProcessor.visitContextParam(contextHandler, descriptor, node);
            }
        }
    }


    private static void addBundleFragmentFragments ( Bundle bundle, WebAppContext contextHandler ) {
        Enumeration<URL> fragments = bundle.findEntries(META_INF, WEB_FRAGMENT_XML, false);

        if ( fragments == null || !fragments.hasMoreElements() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("No fragment found in " + bundle.getSymbolicName()); //$NON-NLS-1$
            }
            return;
        }

        try {
            @SuppressWarnings ( "resource" )
            Resource bundleFile = Resource
                    .newResource(BundleFileLocatorHelperFactory.getFactory().getHelper().getBundleInstallLocation(bundle).getCanonicalFile());

            if ( !contextHandler.getMetaData().getWebInfJars().contains(bundleFile) ) {
                contextHandler.getMetaData().addWebInfJar(bundleFile);

                while ( fragments.hasMoreElements() ) {
                    contextHandler.getMetaData().addFragment(bundleFile, Resource.newResource(fragments.nextElement()));
                }
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to add web fragment:", e); //$NON-NLS-1$
        }
    }


    private static void addRequiredBundleFragments ( Bundle bundle, WebAppContext contextHandler ) {
        List<Bundle> reqBundles = BundleUtil.getRequiredBundles(bundle);
        for ( Bundle b : reqBundles ) {
            Enumeration<URL> fragments = b.findEntries(META_INF, WEB_FRAGMENT_XML, false);

            if ( fragments == null || !fragments.hasMoreElements() ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("No fragment found in " + b.getSymbolicName()); //$NON-NLS-1$
                }
                continue;
            }

            try {
                @SuppressWarnings ( "resource" )
                Resource bundleFile = Resource
                        .newResource(BundleFileLocatorHelperFactory.getFactory().getHelper().getBundleInstallLocation(b).getCanonicalPath());
                if ( !contextHandler.getMetaData().getWebInfJars().contains(bundleFile) ) {
                    contextHandler.getMetaData().addWebInfJar(bundleFile);

                    while ( fragments.hasMoreElements() ) {
                        contextHandler.getMetaData().addFragment(bundleFile, Resource.newResource(fragments.nextElement()));
                    }
                }
            }
            catch ( Exception e ) {
                log.warn("Failed to add web fragment:", e); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param bundle
     * @param contextHandler
     * @param classLoader
     */
    private void setupSessionHandler ( Bundle bundle, WebAppContext contextHandler ) {
        contextHandler.setSessionHandler(this.sessionManagerFactory.createSessionHandler(bundle.getSymbolicName()));
    }


    /**
     * @param contextHandler
     */
    private static void setupProtectedTargets ( WebAppContext contextHandler ) {
        // make sure we protect also the osgi dirs specified by OSGi Enterprise spec
        String[] targets = contextHandler.getProtectedTargets();
        int length = targets == null ? 0 : targets.length;

        String[] updatedTargets = null;
        if ( targets != null ) {
            updatedTargets = new String[length + OSGiWebappConstants.DEFAULT_PROTECTED_OSGI_TARGETS.length];
            System.arraycopy(targets, 0, updatedTargets, 0, length);

        }
        else {
            updatedTargets = new String[OSGiWebappConstants.DEFAULT_PROTECTED_OSGI_TARGETS.length];
        }
        System.arraycopy(
            OSGiWebappConstants.DEFAULT_PROTECTED_OSGI_TARGETS,
            0,
            updatedTargets,
            length,
            OSGiWebappConstants.DEFAULT_PROTECTED_OSGI_TARGETS.length);
        contextHandler.setProtectedTargets(updatedTargets);
    }


    /**
     * @param bundle
     * @param contextHandler
     * @return
     * @throws HttpConfigurationException
     */
    private static OSGiClassLoader createClassLoader ( final Bundle bundle, WebAppContext contextHandler ) throws HttpConfigurationException {
        eu.agno3.runtime.http.service.webapp.internal.OSGiClassLoader classLoader;
        // Use a classloader that knows about the common jetty parent loader, and also the bundle
        classLoader = AccessController.doPrivileged(new PrivilegedAction<OSGiClassLoader>() {

            @Override
            public OSGiClassLoader run () {
                try {
                    return new OSGiClassLoader(WebappTracker.class.getClassLoader(), contextHandler, bundle);
                }
                catch ( IOException e ) {
                    getLog().error("Failed to create classloader", e); //$NON-NLS-1$
                    return null;
                }
            }

        });

        if ( classLoader == null ) {
            throw new HttpConfigurationException("Failed to create classloader"); //$NON-NLS-1$
        }

        contextHandler.setClassLoader(classLoader);
        return classLoader;
    }


    /**
     * @param bundle
     * @param contextHandler
     * @throws HttpConfigurationException
     * @throws Exception
     */
    @SuppressWarnings ( "resource" )
    private static void createRootResource ( Bundle bundle, WebAppContext contextHandler ) throws HttpConfigurationException {
        Resource rootResource;
        Resource jarResource;

        try {
            // Location on filesystem of bundle or the bundle override location
            File root = BundleFileLocatorHelperFactory.getFactory().getHelper().getBundleInstallLocation(bundle);
            rootResource = Resource
                    .newResource(BundleFileLocatorHelperFactory.getFactory().getHelper().getLocalURL(root.getCanonicalFile().toURI().toURL()));

            // try and make sure the rootResource is useable - if its a jar then make it a jar file url
            if ( rootResource.exists() && !rootResource.isDirectory() && !rootResource.toString().startsWith("jar:") ) { //$NON-NLS-1$
                jarResource = JarResource.newJarResource(rootResource);
                if ( jarResource.exists() && jarResource.isDirectory() ) {
                    rootResource.close();
                    rootResource = jarResource;
                }
            }
        }
        catch ( Exception e ) {
            throw new HttpConfigurationException("Cannot setup root resource:", e); //$NON-NLS-1$
        }

        // Set the base resource of the ContextHandler, if not already set, can also be overridden by the context
        // xml file
        if ( contextHandler.getBaseResource() == null ) {
            contextHandler.setBaseResource(rootResource); // .getResource("/webapp/")
        }
    }


    @Override
    public Object addingBundle ( Bundle bundle, BundleEvent event ) {
        if ( !bundle.getSymbolicName().equals(this.config.getBundleSymbolicName()) ) {
            return null;
        }
        if ( event == null || event.getType() == BundleEvent.STARTED ) {
            this.activateBundle(bundle);
        }
        else if ( event.getType() == BundleEvent.STOPPING ) {
            this.deactivateBundle(bundle);
        }
        else {
            log.error("uncaught event " + event.getType()); //$NON-NLS-1$
        }

        return bundle;
    }


    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, Object object ) {
        if ( !bundle.getSymbolicName().equals(this.config.getBundleSymbolicName()) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Removed bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        this.deactivateBundle(bundle);

    }


    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, Object object ) {

        if ( !bundle.getSymbolicName().equals(this.config.getBundleSymbolicName()) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Modified bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        if ( event != null && event.getType() == BundleEvent.STOPPING ) {
            this.deactivateBundle(bundle);
        }
    }
}

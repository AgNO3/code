/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.eclipse.persistence.jaxb.compiler.XMLProcessor;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.eclipse.persistence.jaxb.xmlmodel.XmlSchema;
import org.eclipse.persistence.oxm.MediaType;
import org.eclipse.persistence.oxm.XMLLogin;
import org.eclipse.persistence.sessions.SessionEventListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.util.classloading.BundleDelegatingClassLoader;
import eu.agno3.runtime.util.classloading.CompositeClassLoader;
import eu.agno3.runtime.util.osgi.DsUtil;
import eu.agno3.runtime.util.osgi.ResourceUtil;
import eu.agno3.runtime.xml.binding.JAXBContextProvider;
import eu.agno3.runtime.xml.schema.SchemaRegistration;
import eu.agno3.runtime.xml.schema.SchemaService;
import eu.agno3.runtime.xml.schema.SchemaValidationConfig;
import eu.agno3.runtime.xml.schema.SchemaValidationLevel;


/**
 * @author mbechler
 * 
 */
@Component ( service = JAXBContextProvider.class )
public class BindingBundleTracker implements JAXBContextProvider, BundleTrackerCustomizer<BundleBindingDescriptors> {

    private static final String JAXB_CONFIG_BASE = "/jaxb/"; //$NON-NLS-1$
    private static final String JAXB_CONFIG_PATTERN = "*.xml"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BindingBundleTracker.class);

    private static final String XML_BINDINGS_PACKAGE = "org.eclipse.persistence.jaxb.xmlmodel"; //$NON-NLS-1$
    private JAXBContext bindingsContext;
    private BundleTracker<BundleBindingDescriptors> tracker;

    private SchemaService schemaService;

    private SchemaValidationConfig validationConfig;

    private MOXYJAXBContext globalContext;
    private boolean globalContextNeedsUpdate = true;

    private Map<String, ServiceRegistration<SchemaRegistration>> schemaRegistrations = new HashMap<>();

    private Map<Bundle, BundleBindingDescriptors> registeredBindings = new HashMap<>();

    private Set<SessionEventListener> sessionEventListeners = new HashSet<>();

    private ComponentContext componentContext;
    private ClassLoader globalContextClassLoader;
    @SuppressWarnings ( "unused" )
    private Set<ClassLoader> globalContextClassLoaders;

    private boolean isRefreshingGlobal;
    private EventAdmin eventAdmin;


    @Activate
    protected synchronized void activate ( ComponentContext context ) throws JAXBException {
        this.componentContext = context;
        this.bindingsContext = JAXBContext.newInstance(XML_BINDINGS_PACKAGE, this.getClass().getClassLoader());
        this.tracker = new BundleTracker<>(context.getBundleContext(), Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, this);
        this.tracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.globalContextClassLoader = null;
        this.globalContextClassLoaders = null;
        this.bindingsContext = null;
        this.componentContext = null;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( SessionEventListener listener ) {
        this.sessionEventListeners.add(listener);
    }


    protected synchronized void unbindListener ( SessionEventListener listener ) {
        this.sessionEventListeners.remove(listener);
    }


    @Reference
    protected synchronized void setEventAdmin ( EventAdmin ea ) {
        this.eventAdmin = ea;
    }


    protected synchronized void unsetEventAdmin ( EventAdmin ea ) {
        if ( this.eventAdmin == ea ) {
            this.eventAdmin = null;
        }
    }


    @Reference
    protected synchronized void setValidationConfig ( SchemaValidationConfig vc ) {
        this.validationConfig = vc;
    }


    protected synchronized void unsetValidationConfig ( SchemaValidationConfig vc ) {
        if ( this.validationConfig == vc ) {
            this.validationConfig = null;
        }
    }


    @Reference
    protected synchronized void setSchemaService ( SchemaService ss ) {
        this.schemaService = ss;
    }


    protected synchronized void unsetSchemaService ( SchemaService ss ) {
        if ( this.schemaService == ss ) {
            this.schemaService = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public BundleBindingDescriptors addingBundle ( Bundle bundle, BundleEvent event ) {

        if ( event == null || event.getType() == BundleEvent.RESOLVED ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Bundle %s resolved: %s", bundle.getSymbolicName(), event)); //$NON-NLS-1$
            }

            List<URL> bindingFiles = ResourceUtil.safeFindPattern(bundle, JAXB_CONFIG_BASE, JAXB_CONFIG_PATTERN, true);

            if ( bindingFiles.isEmpty() ) {
                return null;
            }

            registerBundleBindings(bundle, bindingFiles);

        }

        return null;
    }


    /**
     * @param bundle
     * @param bindingFiles
     */
    private void registerBundleBindings ( Bundle bundle, List<URL> bindingFiles ) {
        MultiValuedMap<String, XmlBindings> bundleBindings = new ArrayListValuedHashMap<>();
        synchronized ( this.registeredBindings ) {

            for ( URL bindingFile : bindingFiles ) {
                if ( bindingFile.getPath().endsWith("/") ) { //$NON-NLS-1$
                    continue;
                }

                this.addBindingFile(bindingFile, bundleBindings);
            }

            Map<String, XmlBindings> merged = new HashMap<>();
            for ( String pkg : bundleBindings.keySet() ) {
                merged.put(pkg, XMLProcessor.mergeXmlBindings(new LinkedList<>(bundleBindings.get(pkg))));
            }
            this.registeredBindings.put(bundle, new BundleBindingDescriptors(bundle, merged));
        }
    }


    private void addBindingFile ( URL bindingFile, MultiValuedMap<String, XmlBindings> bundleBindings ) {
        if ( log.isDebugEnabled() ) {
            log.trace("Adding binding definition " + bindingFile); //$NON-NLS-1$
        }

        File f = new File(bindingFile.getFile());
        String bindingPackage = f.getParentFile().getName();

        try {
            Unmarshaller u = getBindingUnmarshaller();
            XmlBindings bindings = (XmlBindings) u.unmarshal(bindingFile);

            if ( bindings.getPackageName() != null ) {
                this.updatedPackage(bindings.getPackageName());
            }

            XmlSchema xmlSchema = bindings.getXmlSchema();
            if ( xmlSchema != null ) {
                this.updatedNamespace(xmlSchema.getNamespace());
            }

            this.globalContextNeedsUpdate = true;
            bundleBindings.put(bindingPackage, bindings);
        }
        catch ( Exception e ) {
            log.error("Error reading bindings " + bindingFile, e); //$NON-NLS-1$ #
        }
    }


    /**
     * @param packageName
     */
    private void updatedPackage ( String packageName ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Updated packages " + packageName); //$NON-NLS-1$
        }

        Map<String, Object> props = new HashMap<>();
        props.put("package", packageName); //$NON-NLS-1$
        this.eventAdmin.postEvent(new Event("eu/agno3/runtime/xml/binding/PACKAGE_UPDATED", props)); //$NON-NLS-1$
    }


    /**
     * @param namespace
     */
    private void updatedNamespace ( String namespace ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Updated namespace " + namespace); //$NON-NLS-1$
        }
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace); //$NON-NLS-1$
        this.eventAdmin.postEvent(new Event("eu/agno3/runtime/xml/binding/NAMESPACE_UPDATED", props)); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, BundleBindingDescriptors object ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, BundleBindingDescriptors object ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Bundle removing " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        synchronized ( this.registeredBindings ) {
            this.registeredBindings.remove(bundle);
            this.globalContextNeedsUpdate = true;
        }

    }


    protected synchronized void refreshGlobalContext () throws JAXBException {
        if ( !this.globalContextNeedsUpdate ) {
            return;
        }

        if ( this.isRefreshingGlobal ) {
            log.warn("Recursion during JAXB context refresh"); //$NON-NLS-1$
        }
        try {
            this.isRefreshingGlobal = true;
            if ( log.isDebugEnabled() ) {
                try {
                    throw new IllegalStateException();
                }
                catch ( IllegalStateException e ) {
                    log.debug("Refreshing global JAXB context", e); //$NON-NLS-1$
                }
            }
            Set<ClassLoader> classLoaders = new HashSet<>();
            ClassLoader defaultContextClassLoader = setupGlobalClassLoader(classLoaders);

            MOXYJAXBContext defaultContext = this.createJAXBContext(null, defaultContextClassLoader);
            long start = System.currentTimeMillis();

            if ( this.validationConfig.getLevel() != SchemaValidationLevel.OFF ) {
                final Set<String> foundNamespaces = new HashSet<>();
                ByteArraySchemaOutputResolver resolver = new ByteArraySchemaOutputResolver();
                defaultContext.generateSchema(resolver);
                updateSchemas(foundNamespaces, resolver.getResults());
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Schema generation took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0)); //$NON-NLS-1$
            }

            this.globalContext = defaultContext;
            this.globalContextClassLoader = defaultContextClassLoader;
            this.globalContextClassLoaders = classLoaders;
            this.globalContextNeedsUpdate = false;
        }
        finally {
            this.isRefreshingGlobal = false;
        }
    }


    /**
     * @param foundNamespaces
     * @param results
     */
    private void updateSchemas ( final Set<String> foundNamespaces, final Map<String, ByteArrayOutputStream> results ) {
        Dictionary<String, Object> regProperties = new Hashtable<>();
        regProperties.put("autoGenerated", true); //$NON-NLS-1$
        for ( Entry<String, ByteArrayOutputStream> r : results.entrySet() ) {
            String ns = r.getKey();

            if ( log.isDebugEnabled() ) {
                log.debug("Generated schema for " + ns); //$NON-NLS-1$
            }

            if ( ns != null ) {
                ns = ns.trim();

                if ( ns.isEmpty() ) {
                    continue;
                }

                if ( this.schemaRegistrations.containsKey(ns) ) {
                    DsUtil.unregisterSafe(this.componentContext, this.schemaRegistrations.get(ns));
                }

                if ( !this.schemaService.hasSchemaFor(ns) ) {
                    publishAutoGeneratedSchema(foundNamespaces, regProperties, r, ns);
                }
            }
        }

        unregisterRemovedSchemata(foundNamespaces);
    }


    /**
     * @param foundNamespaces
     * @param regProperties
     * @param r
     * @param ns
     */
    private synchronized void publishAutoGeneratedSchema ( final Set<String> foundNamespaces, Dictionary<String, Object> regProperties,
            Entry<String, ByteArrayOutputStream> r, String ns ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Publish autogenerated schema for " + ns); //$NON-NLS-1$
        }
        byte[] encoded = r.getValue().toByteArray();
        AutoGeneratedSchemaRegistration schemaReg = new AutoGeneratedSchemaRegistration(ns, encoded);
        regProperties.put("targetNamespace", ns); //$NON-NLS-1$

        ServiceRegistration<SchemaRegistration> serviceReg = DsUtil
                .registerSafe(this.componentContext, SchemaRegistration.class, schemaReg, regProperties);
        this.schemaRegistrations.put(ns, serviceReg);

        foundNamespaces.add(ns);
    }


    /**
     * @param foundNamespaces
     */
    private synchronized void unregisterRemovedSchemata ( final Set<String> foundNamespaces ) {
        for ( Entry<String, ServiceRegistration<SchemaRegistration>> namespace : this.schemaRegistrations.entrySet() ) {
            if ( !foundNamespaces.contains(namespace.getKey()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Unregister removed schema " + namespace.getKey()); //$NON-NLS-1$
                }
                DsUtil.unregisterSafe(this.componentContext, this.schemaRegistrations.remove(namespace.getKey()));
            }
        }
    }


    @Override
    public JAXBContext getContext ( Class<?>... classes ) throws JAXBException {
        synchronized ( this.registeredBindings ) {
            this.refreshGlobalContext();

            if ( classes == null || classes.length == 0 ) {
                // no extra classes, global context can be used
                return this.globalContext;
            }

            log.debug("Creating custom JAXB context"); //$NON-NLS-1$
            return createJAXBContext(this.globalContext, null, classes);
        }
    }


    /**
     * @param classes
     * @return a JAXB context including both the globally registered descriptors and mapping for the specified classes
     * @throws JAXBException
     */
    protected MOXYJAXBContext createJAXBContext ( MOXYJAXBContext delegate, ClassLoader overrideClassLoader, Class<?>... classes )
            throws JAXBException {
        long start = System.currentTimeMillis();
        Set<Class<?>> realClasses = new HashSet<>();

        if ( classes != null ) {
            for ( Class<?> clazz : classes ) {
                if ( clazz.getPackage() == null || !this.registeredBindings.containsKey(clazz.getPackage().getName()) ) {
                    realClasses.add(clazz);
                }
            }
        }

        // need to keep classloader references alive until makeJAXBContext creates proper references
        Set<ClassLoader> classLoaders = new HashSet<>();
        ClassLoader classLoader;
        if ( overrideClassLoader != null ) {
            classLoader = overrideClassLoader;
        }
        else {
            classLoader = setupCustomClassLoader(classLoaders, classes);
        }

        Map<String, XmlBindings> bindings = Collections.EMPTY_MAP;
        if ( delegate == null ) {
            bindings = mergeBindings(getPackageBindings());
        }
        MOXYJAXBContext context = makeJAXBContext(delegate, classLoader, realClasses, bindings);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Context initialization took %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
        }

        return context;
    }


    /**
     * @param bindings
     * @return
     * @throws JAXBException
     */
    private static Map<String, XmlBindings> mergeBindings ( Map<String, List<XmlBindings>> bindings ) throws JAXBException {
        Map<String, XmlBindings> res = new HashMap<>();
        for ( Entry<String, List<XmlBindings>> entry : bindings.entrySet() ) {
            if ( entry.getValue().size() > 1 ) {
                res.put(entry.getKey(), XMLProcessor.mergeXmlBindings(entry.getValue()));
            }
            else {
                res.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return res;
    }


    /**
     * @return
     * @throws JAXBException
     * @throws PropertyException
     */
    private Unmarshaller getBindingUnmarshaller () throws JAXBException, PropertyException {
        Unmarshaller unmarshaller = this.bindingsContext.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_XML);
        unmarshaller.setProperty(UnmarshallerProperties.AUTO_DETECT_MEDIA_TYPE, true);
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        unmarshaller.setEventHandler(new DefaultValidationEventHandler());
        return unmarshaller;
    }


    /**
     * @param properties
     * @param packageNames
     * @param classLoader
     * @param classes
     * @param bindings
     * @return
     * @throws JAXBException
     */
    private MOXYJAXBContext makeJAXBContext ( MOXYJAXBContext delegate, ClassLoader classLoader, Set<Class<?>> classes,
            Map<String, XmlBindings> bindings ) throws JAXBException {
        MOXYJAXBContext ctx = new MOXYJAXBContext(
            new MOXYJAXBContextInput(classes, delegate, bindings, new HashMap<>(), classLoader, this.sessionEventListeners));
        XMLLogin datasourceLogin = (XMLLogin) ctx.getXMLContext().getSession().getDatasourceLogin();
        datasourceLogin.setEqualNamespaceResolvers(false);
        return ctx;
    }


    /**
     * @return the globalContextClassLoader
     */
    ClassLoader getGlobalContextClassLoader () {
        return this.globalContextClassLoader;
    }


    /**
     * @param classLoaders
     * @return
     */
    private ClassLoader setupGlobalClassLoader ( Set<ClassLoader> classLoaders ) {
        classLoaders.add(this.getClass().getClassLoader());
        classLoaders.add(JAXBContextFactory.class.getClassLoader());
        for ( Bundle b : this.registeredBindings.keySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("adding classloader for bundle " + b); //$NON-NLS-1$
            }
            classLoaders.add(new BundleDelegatingClassLoader(b));
        }
        return new CompositeClassLoader(classLoaders);
    }


    /**
     * @param packageNames
     * @param classes
     * @param classLoaders
     * @return
     */
    private ClassLoader setupCustomClassLoader ( Set<ClassLoader> classLoaders, Class<?>... classes ) {
        classLoaders.add(this.globalContextClassLoader);
        for ( Class<?> cl : classes ) {
            Bundle b = FrameworkUtil.getBundle(cl);
            if ( b != null && !this.registeredBindings.containsKey(b) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("adding classloader for bundle " + b); //$NON-NLS-1$
                }
                classLoaders.add(cl.getClassLoader());
            }
            else if ( b == null ) {
                if ( classLoaders.add(cl.getClassLoader()) && log.isDebugEnabled() ) {
                    log.debug("adding classloader " + cl); //$NON-NLS-1$
                }
            }
        }

        return new JAXBBindingClassLoader(this, classLoaders);
    }


    /**
     * @return
     */
    private Map<String, List<XmlBindings>> getPackageBindings () {
        Map<String, List<XmlBindings>> bindings = new HashMap<>();
        for ( Entry<Bundle, BundleBindingDescriptors> e : this.registeredBindings.entrySet() ) {
            if ( e.getValue() != null ) {
                collectBindings(bindings, e);
            }
        }
        return bindings;
    }


    /**
     * @param bindings
     * @param e
     */
    private static void collectBindings ( Map<String, List<XmlBindings>> bindings, Entry<Bundle, BundleBindingDescriptors> e ) {
        for ( String pack : e.getValue().getBindings().keySet() ) {
            if ( !bindings.containsKey(pack) ) {
                bindings.put(pack, new ArrayList<XmlBindings>());
            }
            bindings.get(pack).add(e.getValue().getBindings().get(pack));
        }
    }

}

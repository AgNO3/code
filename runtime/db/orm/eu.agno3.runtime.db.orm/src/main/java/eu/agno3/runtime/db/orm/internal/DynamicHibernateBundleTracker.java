/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Namespace;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationException;


/**
 * @author mbechler
 * 
 */
class DynamicHibernateBundleTracker implements BundleTrackerCustomizer<Object> {

    public static final String HBM_CONFIG_PATH = "/orm/"; //$NON-NLS-1$
    public static final String DYNAMIC_HIBERNATE_HEADER = "Hibernate-Contribution"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DynamicHibernateBundleTracker.class);

    private Map<Long, DynamicHibernateBundleInfo> bundles = new HashMap<>();

    private DynamicHibernatePersistenceProvider persistenceProvider;
    private BundleTracker<Object> bundleTracker;


    /**
     * @param provider
     * 
     */
    DynamicHibernateBundleTracker ( DynamicHibernatePersistenceProvider provider ) {
        this.persistenceProvider = provider;
    }


    void start ( BundleContext context ) {
        this.bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        this.bundleTracker.open();
    }


    void stop () {
        this.bundleTracker.close();
        this.bundleTracker = null;
    }


    void booted () {}


    @Override
    public Object addingBundle ( Bundle bundle, BundleEvent event ) {
        if ( event == null || event.getType() == BundleEvent.STARTED ) {
            this.processBundleStart(bundle, false); //
        }
        else if ( event.getType() == BundleEvent.STOPPING ) {
            this.processBundleStop(bundle);
        }
        else {
            log.error("uncaught event " + event.getType()); //$NON-NLS-1$
        }

        return bundle;
    }


    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, Object object ) {
        if ( event != null && event.getType() == BundleEvent.STOPPING ) {
            this.processBundleStop(bundle);
        }

    }


    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, Object object ) {
        if ( event != null && event.getType() == BundleEvent.STOPPING ) {
            this.processBundleStop(bundle);
        }
    }


    synchronized void processBundleStop ( Bundle bundle ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Processing dynamic hibernate data of stopping bundle %s-%s", bundle.getSymbolicName(), bundle.getVersion())); //$NON-NLS-1$
        }

        synchronized ( this.bundles ) {
            if ( this.bundles.containsKey(bundle.getBundleId()) ) {
                for ( String pu : this.bundles.get(bundle.getBundleId()).getClassRegistrations().keySet() ) {
                    this.persistenceProvider.removeBundleContributions(pu, this.bundles.get(bundle.getBundleId()));
                }

                this.bundles.remove(bundle.getBundleId());
            }
            else {
                log.trace("No dynamic hibernate entity classes registered"); //$NON-NLS-1$
            }
        }
    }


    synchronized void processBundleStart ( Bundle bundle, boolean booting ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Processing dynamic hibernate data of started bundle %s-%s", bundle.getSymbolicName(), bundle.getVersion())); //$NON-NLS-1$
        }

        if ( this.bundles.containsKey(bundle.getBundleId()) ) {
            log.error("Bundle is already registered in dynamic hibernate"); //$NON-NLS-1$
            return;
        }

        String header = HibernateBundleScanner.getBundleHibernateHeader(bundle);

        checkBundleContribution(bundle);

        Map<String, List<URL>> bundleMappingFiles = HibernateBundleScanner.getBundleMappingFiles(bundle);
        Map<String, List<String>> entries;
        try {
            entries = HibernateBundleScanner.parseBundleHeader(bundle, header);
        }
        catch ( HibernateConfigurationException e ) {
            log.error("Failed to parse bundle header:", e); //$NON-NLS-1$
            return;
        }

        try {
            this.persistenceProvider.validateEntries(entries);
        }
        catch ( DatabaseConfigurationException e1 ) {
            log.error("Persistence unit validation failed:", e1); //$NON-NLS-1$
            return;
        }

        synchronized ( this.bundles ) {
            DynamicHibernateBundleInfo bundleInfo = new DynamicHibernateBundleInfo(bundle);
            this.bundles.put(bundle.getBundleId(), bundleInfo);
            bundleInfo.getMappingFiles().putAll(bundleMappingFiles);

            setupBundleEntities(entries, bundleInfo, booting);

        }

    }


    /**
     * @param bundle
     */
    private static void checkBundleContribution ( Bundle bundle ) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleRequirement> requirements = wiring.getRequirements(BundleRevision.PACKAGE_NAMESPACE);
        for ( BundleRequirement req : requirements ) {
            String filter = req.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
            // TODO: check required package imports (javax.persistence, org.hibernate.proxy, javassist.util.proxy)
            if ( log.isTraceEnabled() ) {
                log.trace("Found filter " + filter); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param entries
     * @param bundleInfo
     * @param booting
     */
    private void setupBundleEntities ( Map<String, List<String>> entries, DynamicHibernateBundleInfo bundleInfo, boolean noRefresh ) {
        for ( Entry<String, List<String>> entry : entries.entrySet() ) {
            String pu = entry.getKey();
            List<String> cs = entry.getValue();

            if ( log.isDebugEnabled() ) {
                log.debug("Adding classes to PU " + pu); //$NON-NLS-1$
            }
            List<Class<? extends Object>> realClasses = new ArrayList<>();

            for ( String clazz : cs ) {
                setupClass(bundleInfo, realClasses, clazz);
            }

            bundleInfo.getClassRegistrations().put(pu, realClasses);

            this.persistenceProvider.refreshProxies(bundleInfo, pu, noRefresh);
        }
    }


    /**
     * @param bundleInfo
     * @param realClasses
     * @param clazz
     */
    private static void setupClass ( DynamicHibernateBundleInfo bundleInfo, List<Class<? extends Object>> realClasses, String clazz ) {
        try {
            Class<? extends Object> realClass = bundleInfo.getBundle().loadClass(clazz);
            realClasses.add(realClass);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Class %s added", clazz)); //$NON-NLS-1$
            }
        }
        catch ( ClassNotFoundException e ) {
            log.error(String.format("Cannot find class %s in bundle:", clazz), e); //$NON-NLS-1$
        }
    }


    Collection<DynamicHibernateBundleInfo> getBundleContributions () {
        return this.bundles.values();
    }

}

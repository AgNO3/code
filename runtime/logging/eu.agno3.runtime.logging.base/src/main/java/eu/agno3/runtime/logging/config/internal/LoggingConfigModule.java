/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.logging.DynamicVerbosityLayout;
import eu.agno3.runtime.logging.LogConfigurationService;
import eu.agno3.runtime.logging.config.LoggerConfigObserverProxy;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;
import eu.agno3.runtime.logging.config.PrioritizedLoggerConfigurationSource;


/**
 * @author mbechler
 * 
 */
public class LoggingConfigModule implements BundleActivator,
        ServiceTrackerCustomizer<PrioritizedLoggerConfigurationSource, PrioritizedLoggerConfigurationSource> {

    private static final String DEFAULT_CONFIG_FILE = "/default-log4j.properties"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LoggingConfigModule.class);

    private LoggerConfigAdminTracker configAdminTracker;
    private BundleTracker<BundleConfigurationSource> bundleTracker;

    private DynamicVerbosityLayout consoleLayout;

    private DelegatingLoggerConfigurationSource source;

    private BundleContext bundleContext;

    private ServiceTracker<PrioritizedLoggerConfigurationSource, PrioritizedLoggerConfigurationSource> sourceTracker;


    /**
     * @param consoleLayout
     */
    public LoggingConfigModule ( DynamicVerbosityLayout consoleLayout ) {
        this.consoleLayout = consoleLayout;
    }


    Logger getLog () {
        return LoggingConfigModule.log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( BundleContext context ) {

        try {
            getLog().debug("Setting up PAX Logging configuration"); //$NON-NLS-1$
            this.bundleContext = context;

            this.source = new DelegatingLoggerConfigurationSource();
            DynamicLoggerConfigurationSource dynamicSource = new DynamicLoggerConfigurationSource(100);
            this.source.addSource(new BundleConfigurationSource(context.getBundle(), DEFAULT_CONFIG_FILE, -100));
            this.source.addSource(dynamicSource);

            this.configAdminTracker = new LoggerConfigAdminTracker(this.source, context);
            this.source.addObserver(new LoggerConfigObserverProxy(this.configAdminTracker));
            this.configAdminTracker.open();

            if ( this.configAdminTracker.getService() == null ) {
                log.debug("ConfigAdmin service not yet available"); //$NON-NLS-1$
            }

            this.bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE | Bundle.STARTING | Bundle.RESOLVED, new BundleConfigurationTracker(
                this.source));
            this.bundleTracker.open();

            LogConfigurationService logConfigService = new LogConfigurationServiceImpl(dynamicSource, this.consoleLayout);

            context.registerService(LogConfigurationService.class, logConfigService, null);

            this.sourceTracker = new ServiceTracker<>(context, PrioritizedLoggerConfigurationSource.class, this);
            this.sourceTracker.open();
        }
        catch ( LoggerConfigurationException e ) {
            log.error("Failed to set up logging configuration:", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( BundleContext context ) {

        if ( this.sourceTracker != null ) {
            this.sourceTracker.close();
            this.sourceTracker = null;
        }

        if ( this.configAdminTracker != null ) {
            this.configAdminTracker.close();
        }

        if ( this.bundleTracker != null ) {
            this.bundleTracker.close();
        }

        this.source = null;
        this.bundleContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public PrioritizedLoggerConfigurationSource addingService ( ServiceReference<PrioritizedLoggerConfigurationSource> ref ) {
        PrioritizedLoggerConfigurationSource s = this.bundleContext.getService(ref);
        if ( s != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Binding logger configuration source " + s); //$NON-NLS-1$
            }
            this.source.addSource(s);
        }

        return s;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<PrioritizedLoggerConfigurationSource> ref, PrioritizedLoggerConfigurationSource obj ) {
        // unused
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<PrioritizedLoggerConfigurationSource> ref, PrioritizedLoggerConfigurationSource obj ) {
        if ( obj != null ) {

            if ( log.isDebugEnabled() ) {
                log.debug("Unbinding logger configuration source " + obj); //$NON-NLS-1$
            }
            this.source.removeSource(obj);
        }
    }

}

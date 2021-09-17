/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import eu.agno3.runtime.logging.config.LoggerConfigApplier;
import eu.agno3.runtime.logging.config.LoggerConfigObserver;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;
import eu.agno3.runtime.logging.config.LoggerConfigurationSource;


class LoggerConfigAdminTracker extends ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> implements LoggerConfigApplier, LoggerConfigObserver {

    private static final String PAX_LOGGING_PID = "org.ops4j.pax.logging"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LoggerConfigAdminTracker.class);

    /**
     * 
     */
    private BundleContext bundleContext;
    private LoggerConfigApplier loggerConfigApplier;
    private LoggerConfigurationSource configSource;


    LoggerConfigAdminTracker ( LoggerConfigurationSource source, BundleContext ctx, LoggerConfigApplier configApplier ) {
        super(ctx, ConfigurationAdmin.class, null);

        this.bundleContext = ctx;
        this.configSource = source;

        if ( configApplier == null ) {
            this.loggerConfigApplier = this;
        }
    }


    /**
     * 
     * @param source
     * @param ctx
     */
    public LoggerConfigAdminTracker ( LoggerConfigurationSource source, BundleContext ctx ) {
        this(source, ctx, null);
    }


    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigApplier#applyTo(org.osgi.service.cm.Configuration)
     */
    @Override
    public void applyTo ( Configuration cmConfig ) throws LoggerConfigurationException {
        Dictionary<String, Object> loggerConfig = new Hashtable<>(this.configSource.getConfig());
        try {
            cmConfig.update(loggerConfig);
        }
        catch ( IOException e ) {
            throw new LoggerConfigurationException("Failed to update configuration manager:", e); //$NON-NLS-1$
        }
    }


    /**
     * Applies the current configuration to all tracked instances
     * 
     * @throws LoggerConfigurationException
     */
    public void applyToAll () throws LoggerConfigurationException {
        ServiceReference<ConfigurationAdmin>[] refs = this.getServiceReferences();

        if ( refs == null ) {
            getLog().trace("No ConfigurationAdmin services found"); //$NON-NLS-1$
            return;
        }

        for ( ServiceReference<ConfigurationAdmin> confAdminRef : refs ) {
            this.applyInternal(this.context.getService(confAdminRef));
        }
    }


    protected void applyInternal ( ConfigurationAdmin confAdmin ) throws LoggerConfigurationException {
        try {
            String paxLoggingPid = PAX_LOGGING_PID;
            log.debug("Apply configuration to CM"); //$NON-NLS-1$
            Configuration paxLoggingConfig = confAdmin.getConfiguration(paxLoggingPid, null);
            if ( paxLoggingConfig != null ) {
                this.loggerConfigApplier.applyTo(paxLoggingConfig);
            }
        }
        catch ( IOException e ) {
            throw new LoggerConfigurationException("Failed to obtain current configuration:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ConfigurationAdmin addingService ( ServiceReference<ConfigurationAdmin> reference ) {
        ConfigurationAdmin confAdmin = this.bundleContext.getService(reference);

        log.debug("ConfigAdmin became available"); //$NON-NLS-1$

        try {
            applyInternal(confAdmin);
        }
        catch ( LoggerConfigurationException e ) {
            getLog().error("Failed to set up PAX Logging Configuration:", e); //$NON-NLS-1$
        }

        return confAdmin;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin service ) {
        // nothing to do
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin service ) {
        // nothing to do
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigObserver#configurationUpdated(eu.agno3.runtime.logging.config.LoggerConfigurationSource)
     */
    @Override
    public void configurationUpdated ( LoggerConfigurationSource s ) {
        try {
            this.applyToAll();
        }
        catch ( LoggerConfigurationException e ) {
            log.warn("Failed to update configuration:", e); //$NON-NLS-1$
        }
    }

}
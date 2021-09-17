/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2016 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = LoggerRegistration.class, immediate = true )
public class LoggerRegistration implements ServiceTrackerCustomizer<ElasticsearchLoggerConfig, LoggerRegistrationHolder> {

    private static final Logger log = Logger.getLogger(LoggerRegistration.class);
    private static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$
    private ServiceTracker<ElasticsearchLoggerConfig, LoggerRegistrationHolder> serviceTracker;
    private BundleContext context;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.context = ctx.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(this.context, ElasticsearchLoggerConfig.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.serviceTracker != null ) {
            this.serviceTracker.close();
            this.serviceTracker = null;
        }
        this.context = null;
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


    /**
     * @param cfgRef
     * @param cfg
     * @throws IOException
     */
    protected void updateConfiguration ( ServiceReference<ElasticsearchLoggerConfig> cfgRef, Configuration cfg ) throws IOException {
        Dictionary<String, Object> cfgProps = new Hashtable<>();
        String cfgInstanceId = (String) cfgRef.getProperty(INSTANCE_ID);
        cfgProps.put(INSTANCE_ID, cfgInstanceId);

        FilterBuilder fb = FilterBuilder.get();
        cfgProps.put(
            "LoggerConfig.target", //$NON-NLS-1$
            fb.eq(INSTANCE_ID, cfgInstanceId).toString());

        cfg.update(cfgProps);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public LoggerRegistrationHolder addingService ( ServiceReference<ElasticsearchLoggerConfig> ref ) {
        ElasticsearchLoggerConfig service = this.context.getService(ref);
        if ( service == null || ! ( ref.getProperty("instanceId") instanceof String ) ) { //$NON-NLS-1$
            log.warn("Does not have instanceId set"); //$NON-NLS-1$
            return null;
        }
        String instanceId = (String) ref.getProperty("instanceId"); //$NON-NLS-1$

        LoggerRegistrationHolder holder = new LoggerRegistrationHolder();
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating backend and reader configuration for " + instanceId); //$NON-NLS-1$
            }
            Configuration backend = this.configAdmin.createFactoryConfiguration(ElasticsearchLoggerBackendImpl.INTERNAL_PID);
            holder.setBackend(backend);
            updateConfiguration(ref, backend);

            Configuration reader = this.configAdmin.createFactoryConfiguration(ElasticsearchLogReaderImpl.INTERNAL_PID);
            holder.setReader(reader);
            updateConfiguration(ref, reader);
        }
        catch ( IOException e ) {
            log.warn("Failed to create elasticsearch configuration", e); //$NON-NLS-1$
        }
        return holder;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<ElasticsearchLoggerConfig> ref, LoggerRegistrationHolder holder ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<ElasticsearchLoggerConfig> ref, LoggerRegistrationHolder holder ) {
        if ( holder != null ) {
            try {
                holder.unregister();
            }
            catch ( IOException e ) {
                log.warn("Failed to unregister logger"); //$NON-NLS-1$
            }
        }
    }

}

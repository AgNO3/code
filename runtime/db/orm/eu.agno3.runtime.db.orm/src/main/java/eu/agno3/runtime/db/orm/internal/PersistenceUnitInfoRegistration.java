/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class PersistenceUnitInfoRegistration implements ServiceTrackerCustomizer<PersistenceUnitDescriptor, Configuration> {

    /**
     * 
     */
    private static final String PERSISTENCE_UNIT = "persistenceUnit"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(PersistenceUnitInfoRegistration.class);

    private ServiceTracker<PersistenceUnitDescriptor, Configuration> tracker;
    private BundleContext bundleContext;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        log.debug("Starting PersistenceUnitInfo registrator"); //$NON-NLS-1$
        this.tracker = new ServiceTracker<>(this.bundleContext, PersistenceUnitDescriptor.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
    }


    @Reference
    protected synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<PersistenceUnitDescriptor> reference ) {

        PersistenceUnitDescriptor descriptor = this.bundleContext.getService(reference);

        Dictionary<String, Object> puProps = new Hashtable<>();
        puProps.put(PERSISTENCE_UNIT, descriptor.getPersistenceUnitName());
        puProps.put("dataSourceName", descriptor.getDataSourceName()); //$NON-NLS-1$

        puProps.put(
            "PersistenceUnitDescriptor.target", //$NON-NLS-1$
            FilterBuilder.get().eq(Constants.SERVICE_ID, reference.getProperty(Constants.SERVICE_ID).toString()).toString());

        puProps.put(
            "DataSource.target", //$NON-NLS-1$
            makeDataSourceFilter(descriptor).toString());

        puProps.put(
            "DsUtil.target", //$NON-NLS-1$
            makeDsUtilFilter(descriptor).toString());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Registering PersistenceUnitInfo for PU %s using DataSource %s user %s", //$NON-NLS-1$
                descriptor.getPersistenceUnitName(),
                descriptor.getDataSourceName(),
                descriptor.getDataSourceUser()));
        }

        try {
            Configuration c = this.configAdmin.createFactoryConfiguration(BasePersistenceUnitInfo.PID);
            c.update(puProps);
            return c;
        }
        catch ( Exception e ) {
            log.error("Failed to register PersistenceUnitInfo:", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param descriptor
     * @param fb
     * @return
     */
    protected FilterExpression makeDsUtilFilter ( PersistenceUnitDescriptor descriptor ) {
        FilterBuilder fb = FilterBuilder.get();
        if ( descriptor.getDataSourceUser() != null ) {
            return fb.and(
                fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, descriptor.getDataSourceName()),
                fb.eq(DataSourceFactory.JDBC_USER, descriptor.getDataSourceUser()));
        }
        return fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, descriptor.getDataSourceName());
    }


    /**
     * @param descriptor
     * @param fb
     * @return
     */
    private static FilterExpression makeDataSourceFilter ( PersistenceUnitDescriptor descriptor ) {
        FilterBuilder fb = FilterBuilder.get();
        if ( descriptor.getDataSourceUser() != null ) {
            return fb.and(
                fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, descriptor.getDataSourceName()),
                fb.eq(DataSourceFactory.JDBC_USER, descriptor.getDataSourceUser()),
                fb.eq(DataSourceWrapper.TYPE, descriptor.getDataSourceType())); // $NON-NLS-1$

        }
        return fb.and(
            fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, descriptor.getDataSourceName()),
            fb.eq(DataSourceWrapper.TYPE, descriptor.getDataSourceType())); // $NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<PersistenceUnitDescriptor> reference, Configuration service ) {
        // not needed
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<PersistenceUnitDescriptor> reference, Configuration service ) {
        if ( service != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Unregistering PersistenceUnitInfo for PU %s", service.getProperties().get(PERSISTENCE_UNIT))); //$NON-NLS-1$
            }
            try {
                service.delete();
            }
            catch ( IOException e ) {
                log.error("Failed to remove configuration:", e); //$NON-NLS-1$
            }
        }
    }
}

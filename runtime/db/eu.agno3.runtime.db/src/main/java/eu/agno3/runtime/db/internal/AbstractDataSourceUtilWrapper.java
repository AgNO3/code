/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2014 by mbechler
 */
package eu.agno3.runtime.db.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * @param <T>
 *            datasource service type
 * 
 */
public abstract class AbstractDataSourceUtilWrapper <T extends DataSource> implements ServiceTrackerCustomizer<T, Configuration> {

    private static final Logger log = Logger.getLogger(AbstractDataSourceUtilWrapper.class);

    private static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_SERVER_NAME,
        DataSourceFactory.JDBC_USER, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS
    };

    private Class<T> dsClass;
    private ServiceTracker<T, Configuration> tracker;
    private ConfigurationAdmin configAdmin;

    private String pid;


    protected AbstractDataSourceUtilWrapper ( Class<T> dsClass, String pid ) {
        this.dsClass = dsClass;
        this.pid = pid;
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.tracker = new ServiceTracker<>(ctx.getBundleContext(), this.dsClass, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        if ( this.tracker != null ) {
            this.tracker.close();
            this.tracker = null;
        }
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
    public synchronized Configuration addingService ( ServiceReference<T> reference ) {
        Dictionary<String, Object> dsUtilProperties = new Hashtable<>();

        String driverClass = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String dsUser = (String) reference.getProperty(DataSourceFactory.JDBC_USER);
        String dsName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

        if ( driverClass == null || dsName == null ) {
            log.warn("DataSource is missing required properties, ignoring..."); //$NON-NLS-1$
            return null;
        }

        for ( String copyProperty : COPY_PROPERTIES ) {
            Object v = reference.getProperty(copyProperty);
            if ( v != null ) {
                dsUtilProperties.put(copyProperty, v);
            }
        }

        FilterBuilder fb = FilterBuilder.get();

        FilterExpression utilFilter = fb.eq(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driverClass);

        FilterExpression dsFilter;
        if ( dsUser != null ) {
            dsFilter = fb.and(fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName), fb.eq(DataSourceFactory.JDBC_USER, dsUser));
        }
        else {
            dsFilter = fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName);
        }

        dsUtilProperties.put("DatabaseDriverUtil.target", utilFilter.toString()); //$NON-NLS-1$
        dsUtilProperties.put("DataSource.target", dsFilter.toString()); //$NON-NLS-1$

        log.debug(String.format("Creating DataSourceUtilWrapper for DS %s user %s", //$NON-NLS-1$
            reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME),
            reference.getProperty(DataSourceFactory.JDBC_USER)));

        try {
            Configuration c = this.configAdmin.createFactoryConfiguration(this.pid);
            c.update(dsUtilProperties);
            return c;
        }
        catch ( Exception e ) {
            log.error("Failed to create wrapper:", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public synchronized void modifiedService ( ServiceReference<T> reference, Configuration service ) {
        // no dynamic reconfiguration
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public synchronized void removedService ( ServiceReference<T> reference, Configuration service ) {

        log.debug(String.format("Removing DataSourceUtilWrapper for DS %s user %s", //$NON-NLS-1$
            reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME),
            reference.getProperty(DataSourceFactory.JDBC_USER)));

        if ( service != null ) {
            try {
                service.delete();
            }
            catch ( IOException e ) {
                log.error("Failed to remove configuration:", e); //$NON-NLS-1$
            }
        }

    }
}

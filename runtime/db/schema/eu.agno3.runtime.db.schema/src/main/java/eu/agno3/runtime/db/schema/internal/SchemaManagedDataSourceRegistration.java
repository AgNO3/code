/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.internal;


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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SchemaManagedDataSourceRegistration.class
}, immediate = true )
public class SchemaManagedDataSourceRegistration implements ServiceTrackerCustomizer<DataSource, Configuration> {

    private static final Logger log = Logger.getLogger(SchemaManagedDataSourceRegistration.class);

    private static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_USER,
        DataSourceFactory.OSGI_JDBC_DRIVER_CLASS
    };

    private ServiceTracker<DataSource, Configuration> tracker;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting SchemaManagedDataSource registrations"); //$NON-NLS-1$
        this.tracker = new ServiceTracker<>(context.getBundleContext(), DataSource.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
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
    public Configuration addingService ( ServiceReference<DataSource> reference ) {
        Dictionary<String, Object> adapterProperties = new Hashtable<>();

        String driverClass = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String dsUser = (String) reference.getProperty(DataSourceFactory.JDBC_USER);
        String dsName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);
        String type = (String) reference.getProperty(DataSourceWrapper.TYPE);

        if ( driverClass == null || dsUser == null || dsName == null || type == null ) {
            log.warn("DataSource is missing required properties, ignoring..."); //$NON-NLS-1$
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Creating SchemaManagedDataSourceAdapter for datasource %s user %s", dsName, dsUser)); //$NON-NLS-1$
        }

        for ( String copyProperty : COPY_PROPERTIES ) {
            Object v = reference.getProperty(copyProperty);
            if ( v != null ) {
                adapterProperties.put(copyProperty, v);
            }
        }

        adapterProperties.put(
            "SchemaManager.target", //$NON-NLS-1$
            FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName).toString());

        adapterProperties.put(
            "DataSource.target", //$NON-NLS-1$
            makeDataSourceFilter(dsUser, dsName, type));

        adapterProperties.put(
            "DsUtil.target", //$NON-NLS-1$
            makeDsUtilFilter(dsUser, dsName));

        adapterProperties.put(DataSourceWrapper.TYPE, type);

        try {
            Configuration c = this.configAdmin.createFactoryConfiguration(SchemaManagedDataSourceAdapter.PID);
            c.update(adapterProperties);
            return c;
        }
        catch ( Exception e ) {
            log.error("Failed to create adapter:", e); //$NON-NLS-1$
        }
        return null;
    }


    private static String makeDataSourceFilter ( String dsUser, String dsName, String type ) {
        FilterBuilder fb = FilterBuilder.get();
        return fb
                .and(
                    fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName),
                    fb.eq(DataSourceFactory.JDBC_USER, dsUser),
                    fb.eq(DataSourceWrapper.TYPE, type)) // $NON-NLS-1$
                .toString();
    }


    private static String makeDsUtilFilter ( String dsUser, String dsName ) {
        FilterBuilder fb = FilterBuilder.get();
        return fb.and(fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName), fb.eq(DataSourceFactory.JDBC_USER, dsUser)).toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<DataSource> reference, Configuration service ) {
        // unused
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<DataSource> reference, Configuration service ) {
        if ( service != null ) {
            try {
                service.delete();
            }
            catch ( IOException e ) {
                log.error("Failed to remove adapter configuration:", e); //$NON-NLS-1$
            }
        }
    }

}

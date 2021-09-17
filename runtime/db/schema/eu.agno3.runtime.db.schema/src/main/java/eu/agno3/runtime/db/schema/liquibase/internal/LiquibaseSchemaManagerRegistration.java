/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


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
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.db.schema.ChangeFileProvider;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseChangeLogFactory;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseServiceLocator;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class LiquibaseSchemaManagerRegistration implements ServiceTrackerCustomizer<LiquibaseSchemaManagerConfig, Configuration> {

    private static final Logger log = Logger.getLogger(LiquibaseSchemaManagerRegistration.class);

    private BundleContext bundleContext;

    private String liquibaseSchema = "APP_SCHEMA"; //$NON-NLS-1$
    private String[] contexts;

    private ConfigurationAdmin configAdmin;

    private ServiceTracker<LiquibaseSchemaManagerConfig, Configuration> tracker;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        this.tracker = new ServiceTracker<>(this.bundleContext, LiquibaseSchemaManagerConfig.class, this);
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


    // BEGIN: Dependencies only
    @Reference
    protected synchronized void setChangeFileProvider ( ChangeFileProvider provider ) {
        // dependency only
    }


    protected synchronized void unsetChangeFileProvider ( ChangeFileProvider provider ) {
        // dependency only
    }


    @Reference
    protected synchronized void setChangeLogFactory ( LiquibaseChangeLogFactory logFactory ) {
        // dependency only
    }


    protected synchronized void unsetChangeLogFactory ( LiquibaseChangeLogFactory logFactory ) {
        // dependency only
    }


    @Reference
    protected synchronized void setServiceLocator ( LiquibaseServiceLocator locator ) {
        // dependency only
    }


    protected synchronized void unsetServiceLocator ( LiquibaseServiceLocator locator ) {
        // dependency only
    }


    @Reference
    protected synchronized void setLiquibaseLogger ( liquibase.logging.Logger logger ) {
        // dependency only
    }


    protected synchronized void unsetLiquibaseLogger ( liquibase.logging.Logger logger ) {
        // dependency only
    }


    // END: Dependencies only

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<LiquibaseSchemaManagerConfig> reference ) {

        Dictionary<String, Object> managerProps = new Hashtable<>();

        String driverClass = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String dsName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

        if ( driverClass == null || dsName == null ) {
            log.warn("LiquibaseSchemaManagerConfig is missing required properties, ignoring..."); //$NON-NLS-1$
            return null;
        }

        this.bundleContext.getService(reference);
        log.info("Creating SchemaManager for datasource " + dsName); //$NON-NLS-1$

        managerProps.put(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName);

        managerProps.put(LiquibaseSchemaManagerConfig.MANAGEMENT_SCHEMA_PROPERTY, this.liquibaseSchema);

        if ( this.contexts != null ) {
            managerProps.put(LiquibaseSchemaManagerConfig.CONTEXTS_PROPERTY, this.contexts);
        }

        managerProps.put("Config.target", //$NON-NLS-1$
            makeDataSourceFilter(dsName));

        managerProps.put("DatabaseFactory.target", //$NON-NLS-1$
            FilterBuilder.get().eq(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driverClass).toString());

        managerProps.put("AdminDataSource.target", //$NON-NLS-1$
            makeDataSourceFilter(dsName));

        managerProps.put("AdminDsUtil.target", //$NON-NLS-1$
            makeDataSourceFilter(dsName));

        try {
            Configuration c = this.configAdmin.createFactoryConfiguration(LiquibaseSchemaManager.PID);
            c.update(managerProps);
            return c;
        }
        catch ( Exception e ) {
            log.error("Failed to set up SchemaManager: ", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param dsName
     * @return
     */
    private static String makeDataSourceFilter ( String dsName ) {
        return FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dsName).toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<LiquibaseSchemaManagerConfig> reference, Configuration service ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<LiquibaseSchemaManagerConfig> reference, Configuration service ) {
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

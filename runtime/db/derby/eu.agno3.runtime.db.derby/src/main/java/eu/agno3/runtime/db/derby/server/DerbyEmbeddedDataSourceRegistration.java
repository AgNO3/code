/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.DataSourceWrapper;
import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.derby.DerbyServer;
import eu.agno3.runtime.db.derby.auth.UserAccess;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * Provides automatic datasource registration for embedded derby servers
 * 
 * 
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class DerbyEmbeddedDataSourceRegistration implements ServiceTrackerCustomizer<DerbyServer, DataSourceRegistrations> {

    private static final Logger log = Logger.getLogger(DerbyEmbeddedDataSourceRegistration.class);

    ServiceTracker<DerbyServer, DataSourceRegistrations> tracker;

    private ComponentContext bundleContext;

    static final String[] COPY_PROPERTIES = new String[] {
        DataSourceFactory.JDBC_DATABASE_NAME, DataSourceFactory.JDBC_DATASOURCE_NAME, DataSourceFactory.JDBC_DESCRIPTION,
        DataSourceFactory.JDBC_SERVER_NAME, DataSourceFactory.JDBC_PORT_NUMBER, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION
    };


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting datasource registration for embedded derby instances"); //$NON-NLS-1$
        this.bundleContext = context;
        this.tracker = new ServiceTracker<>(context.getBundleContext(), DerbyServer.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.tracker = null;
        this.bundleContext = null;
    }


    private static Dictionary<String, Object> copyProperties ( ServiceReference<DerbyServer> reference ) {
        Dictionary<String, Object> dsProperties = new Hashtable<>();

        for ( String prop : COPY_PROPERTIES ) {
            if ( reference.getProperty(prop) != null ) {
                dsProperties.put(prop, reference.getProperty(prop));
            }
        }
        return dsProperties;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public DataSourceRegistrations addingService ( ServiceReference<DerbyServer> reference ) {
        String dataSourceName = (String) reference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

        if ( log.isDebugEnabled() ) {
            log.debug("DerbyServer found, register DataSource " + dataSourceName); //$NON-NLS-1$
        }

        AbstractDerbyServer s = (AbstractDerbyServer) this.bundleContext.getBundleContext().getService(reference);

        return makeDataSourceRegistration(reference, dataSourceName, s);
    }


    /**
     * @param reference
     * @param dataSourceName
     * @param s
     * @return
     */
    protected DataSourceRegistrations makeDataSourceRegistration ( ServiceReference<DerbyServer> reference, String dataSourceName,
            AbstractDerbyServer s ) {
        if ( s == null ) {
            log.error("DerbyServer went away"); //$NON-NLS-1$
            return new DataSourceRegistrations();
        }

        if ( dataSourceName == null ) {
            log.warn("Datasource without a name, ignore"); //$NON-NLS-1$
            return new DataSourceRegistrations();
        }

        if ( s.getAuthConfig() == null ) {
            return setupBasicDataSources(reference, dataSourceName, s);
        }

        try {
            return setupAuthenticatedDataSource(reference, dataSourceName, s);
        }
        catch ( CryptoException e ) {
            log.error("Failed to setup authentication", e); //$NON-NLS-1$
            return new DataSourceRegistrations();
        }
    }


    /**
     * @param reference
     * @param dataSourceName
     * @param s
     * @return
     * @throws CryptoException
     */
    private DataSourceRegistrations setupAuthenticatedDataSource ( ServiceReference<DerbyServer> reference, String dataSourceName,
            AbstractDerbyServer s ) throws CryptoException {
        DataSourceRegistrations res = new DataSourceRegistrations();
        Map<String, UserAccess> authedUsers = s.getAuthConfig()
                .getAuthorizedUsers((String) reference.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));

        Properties adminDsProperties = new Properties();
        adminDsProperties.setProperty(DataSourceFactory.JDBC_USER, s.getAuthConfig().getAdminUser());
        adminDsProperties.setProperty(DataSourceFactory.JDBC_PASSWORD, s.getAuthConfig().getAdminPassword());
        setupAdministrativeDataSource(reference, s, res, adminDsProperties);

        if ( authedUsers.size() == 1 ) {
            log.warn("No users configured, not publishing any regular DataSource"); //$NON-NLS-1$
        }

        for ( Entry<String, UserAccess> userEntry : authedUsers.entrySet() ) {
            if ( userEntry.getKey().equals(s.getAuthConfig().getAdminUser()) ) {
                continue;
            }

            try {
                res.addDataSource(setupUserXADataSource(userEntry, reference, dataSourceName, s));
                res.addDataSource(setupUserDataSource(userEntry, reference, dataSourceName, s));
            }
            catch ( DatabaseConfigurationException e ) {
                log.warn("Failed to create datasource for user " + userEntry.getKey(), e); //$NON-NLS-1$
            }
        }
        return res;
    }


    private ServiceRegistration<? extends CommonDataSource> setupUserXADataSource ( Entry<String, UserAccess> userEntry,
            ServiceReference<DerbyServer> reference, String dataSourceName, AbstractDerbyServer s ) throws DatabaseConfigurationException {

        XADataSource ds = null;
        try {
            Properties dsProperties = new Properties();
            dsProperties.setProperty(DataSourceFactory.JDBC_USER, userEntry.getKey());
            dsProperties.setProperty(DataSourceFactory.JDBC_PASSWORD, s.getAuthConfig().getPassword(userEntry.getKey()));
            ds = s.createXADataSource(dsProperties);
        }
        catch ( SQLException e ) {
            throw new DatabaseConfigurationException("Failed to create user datasource:", e); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering authenticated datasource %s for login %s", dataSourceName, userEntry.getKey())); //$NON-NLS-1$
        }
        Dictionary<String, Object> serviceProperties = copyProperties(reference);
        serviceProperties.put(DataSourceFactory.JDBC_USER, userEntry.getKey());
        serviceProperties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_PLAIN);
        return DsUtil.registerSafe(this.bundleContext, XADataSource.class, ds, serviceProperties);
    }


    private ServiceRegistration<? extends CommonDataSource> setupUserDataSource ( Entry<String, UserAccess> userEntry,
            ServiceReference<DerbyServer> reference, String dataSourceName, AbstractDerbyServer s ) throws DatabaseConfigurationException {
        DataSource ds = null;
        try {
            Properties dsProperties = new Properties();
            dsProperties.setProperty(DataSourceFactory.JDBC_USER, userEntry.getKey());
            dsProperties.setProperty(DataSourceFactory.JDBC_PASSWORD, s.getAuthConfig().getPassword(userEntry.getKey()));
            ds = s.createDataSource(dsProperties);
        }
        catch ( SQLException e ) {
            throw new DatabaseConfigurationException("Failed to create user datasource:", e); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering authenticated datasource %s for login %s", dataSourceName, userEntry.getKey())); //$NON-NLS-1$
        }
        Dictionary<String, Object> serviceProperties = copyProperties(reference);
        serviceProperties.put(DataSourceFactory.JDBC_USER, userEntry.getKey());
        serviceProperties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_PLAIN);
        return DsUtil.registerSafe(this.bundleContext, DataSource.class, ds, serviceProperties);
    }


    /**
     * @param reference
     * @param s
     * @param res
     * @param adminDsProperties
     */
    private void setupAdministrativeDataSource ( ServiceReference<DerbyServer> reference, AbstractDerbyServer s, DataSourceRegistrations res,
            Properties adminDsProperties ) {
        try {
            AdministrativeDataSource adminDs = new AdministrativeDataSource(s.createDataSource(adminDsProperties));
            res.setAdminDataSource(DsUtil.registerSafe(this.bundleContext, AdministrativeDataSource.class, adminDs, copyProperties(reference)));
        }
        catch ( SQLException e ) {
            log.error("Failed to create admin datasource:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param reference
     * @param dataSourceName
     * @param s
     * @return
     */
    private DataSourceRegistrations setupBasicDataSources ( ServiceReference<DerbyServer> reference, String dataSourceName, AbstractDerbyServer s ) {
        DataSourceRegistrations res = new DataSourceRegistrations();
        Properties adminDsProperties = new Properties();
        setupAdministrativeDataSource(reference, s, res, adminDsProperties);

        XADataSource xaDs = null;
        DataSource ds = null;
        try {
            xaDs = s.createXADataSource(new Properties());
            ds = s.createDataSource(new Properties());
        }
        catch ( SQLException e ) {
            log.error("Failed to create datasource:", e); //$NON-NLS-1$
            return res;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Registering anonymous datasource " + dataSourceName); //$NON-NLS-1$
        }
        Dictionary<String, Object> serviceProperties = copyProperties(reference);
        serviceProperties.put(DataSourceWrapper.TYPE, DataSourceWrapper.TYPE_PLAIN);
        res.addDataSource(DsUtil.registerSafe(this.bundleContext, XADataSource.class, xaDs, serviceProperties));
        res.addDataSource(DsUtil.registerSafe(this.bundleContext, DataSource.class, ds, serviceProperties));
        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<DerbyServer> reference, DataSourceRegistrations regs ) {

        if ( regs.getAdminDataSource() != null ) {
            regs.getAdminDataSource().setProperties(copyProperties(reference));
        }

        for ( ServiceRegistration<? extends CommonDataSource> service : regs.getDataSources() ) {
            service.setProperties(copyProperties(reference));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<DerbyServer> reference, DataSourceRegistrations regs ) {

        if ( regs.getAdminDataSource() != null ) {
            DsUtil.unregisterSafe(this.bundleContext, regs.getAdminDataSource());
        }

        for ( ServiceRegistration<? extends CommonDataSource> service : regs.getDataSources() ) {
            DsUtil.unregisterSafe(this.bundleContext, service);
        }
    }

}

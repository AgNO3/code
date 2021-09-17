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
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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

import eu.agno3.runtime.db.orm.internal.EntityManagerFactoryRegistration.RegistrationHolder;
import eu.agno3.runtime.db.orm.tx.CMTTransactionServiceImpl;
import eu.agno3.runtime.db.orm.tx.JdbcTransactionServiceImpl;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class EntityManagerFactoryRegistration implements ServiceTrackerCustomizer<BasePersistenceUnitInfo, RegistrationHolder> {

    private static final Logger log = Logger.getLogger(EntityManagerFactoryRegistration.class);
    private ServiceTracker<BasePersistenceUnitInfo, RegistrationHolder> tracker;
    private PersistenceProvider provider;
    private ComponentContext componentContext;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.componentContext = context;
        log.debug("Starting EntityManagerFactory registrator"); //$NON-NLS-1$
        this.tracker = new ServiceTracker<>(context.getBundleContext(), BasePersistenceUnitInfo.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setPersistenceProvider ( PersistenceProvider prov ) {
        this.provider = prov;
    }


    protected synchronized void unsetPersistenceProvider ( PersistenceProvider prov ) {
        if ( this.provider == prov ) {
            this.provider = null;
        }
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
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public RegistrationHolder addingService ( ServiceReference<BasePersistenceUnitInfo> reference ) {

        BasePersistenceUnitInfo info = this.componentContext.getBundleContext().getService(reference);

        if ( info == null ) {
            return null;
        }

        String dataSourceName = info.getProperties().getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

        Map<Object, Object> emfConfig = new Properties();
        Dictionary<String, Object> serviceProperties = new Hashtable<>();

        for ( String property : reference.getPropertyKeys() ) {
            emfConfig.put(property, reference.getProperty(property));
            serviceProperties.put(property, reference.getProperty(property));
        }

        serviceProperties.put("javax.persistence.provider", this.provider.getClass().getName()); //$NON-NLS-1$
        if ( dataSourceName != null ) {
            serviceProperties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSourceName);
        }

        EntityManagerFactory emf = this.provider.createContainerEntityManagerFactory(info, emfConfig);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Registering EntityManagerFactory for persistence unit %s with datasource %s", //$NON-NLS-1$
                info.getPersistenceUnitName(),
                dataSourceName));
        }

        ServiceRegistration<EntityManagerFactory> emfReg = DsUtil
                .registerSafe(this.componentContext, EntityManagerFactory.class, emf, serviceProperties);

        Configuration etsReg = null;
        String factoryPid = null;

        if ( info.getTransactionType() == PersistenceUnitTransactionType.JTA ) {
            factoryPid = CMTTransactionServiceImpl.PID;
        }
        else if ( info.getTransactionType() == PersistenceUnitTransactionType.RESOURCE_LOCAL ) {
            factoryPid = JdbcTransactionServiceImpl.PID;
        }
        else {
            log.warn("Unknown transaction type " + info.getTransactionType()); //$NON-NLS-1$
        }

        if ( factoryPid != null ) {
            try {
                etsReg = this.configAdmin.createFactoryConfiguration(factoryPid);
                Dictionary<String, Object> props = new Hashtable<>();
                props.put("instanceId", dataSourceName); //$NON-NLS-1$
                props.put("persistenceUnit", info.getPersistenceUnitName()); //$NON-NLS-1$
                props.put(
                    "EntityManagerFactory.target", //$NON-NLS-1$
                    FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSourceName).toString());
                etsReg.update(props);
            }
            catch ( Exception e ) {
                log.error("Failed to create entity transaction service registration", e); //$NON-NLS-1$
            }
        }

        return new RegistrationHolder(emf, emfReg, etsReg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<BasePersistenceUnitInfo> reference, RegistrationHolder service ) {
        // not needed
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<BasePersistenceUnitInfo> reference, RegistrationHolder service ) {
        if ( service == null ) {
            return;
        }

        PersistenceUnitInfo info = this.componentContext.getBundleContext().getService(reference);
        if ( log.isDebugEnabled() && info != null ) {
            log.debug("Shutting down EntityManagerFactory for PU " + info.getPersistenceUnitName()); //$NON-NLS-1$
        }

        service.unregister(this.componentContext);
    }

    static class RegistrationHolder {

        private final ServiceRegistration<EntityManagerFactory> emfRegistration;
        private EntityManagerFactory emf;
        private Configuration etsConfig;


        /**
         * @param emf
         * @param emfReg
         * @param etsConfig
         * 
         */
        public RegistrationHolder ( EntityManagerFactory emf, ServiceRegistration<EntityManagerFactory> emfReg, Configuration etsConfig ) {
            this.emf = emf;
            this.emfRegistration = emfReg;
            this.etsConfig = etsConfig;
        }


        synchronized void unregister ( ComponentContext ctx ) {
            if ( this.etsConfig != null ) {
                try {
                    this.etsConfig.delete();
                }
                catch ( IOException e ) {
                    getLog().error("Failed to remove EntityTransactionService registration", e); //$NON-NLS-1$
                }
            }

            if ( this.emfRegistration != null ) {
                DsUtil.unregisterSafe(ctx, this.emfRegistration);
            }

            if ( this.emf instanceof DynamicEntityManagerFactoryProxy ) {
                ( (DynamicEntityManagerFactoryProxy) this.emf ).shutdown();
            }
        }
    }
}

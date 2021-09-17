/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.persistence.AttributeConverter;
import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Version;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SchemaAutoTooling;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cfg.AttributeConverterDefinition;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.internal.ConfigurationServiceImpl;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorBuilderImpl;
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorBuilderImpl;
import org.hibernate.resource.transaction.spi.TransactionCoordinatorBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.ServiceContributor;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.jboss.jandex.IndexView;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

import eu.agno3.runtime.db.DatabaseException;
import eu.agno3.runtime.db.orm.EntityManagerConfigurationFailedException;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.orm.PersistenceUnitInfoProvider;
import eu.agno3.runtime.db.orm.hibernate.UUIDWhenNotSetGenerator;
import eu.agno3.runtime.db.orm.jmx.StatisticsMXBean;
import eu.agno3.runtime.util.classloading.CompositeClassLoader;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "deprecation" )
public class DynamicEntityManagerFactoryProxy implements EntityManagerFactory, PersistenceUnitInfoProvider, MetadataContributor, TypeContributor,
        StandardServiceInitiator<ConnectionProvider> {

    private static final Logger log = Logger.getLogger(DynamicEntityManagerFactoryProxy.class);
    private static final int TIMEOUT = 1000;

    private final Object lock = new Object();

    private DynamicPersistenceUnitInfoProxy info;
    private PersistenceUnitDescriptor descriptor;

    private Map<Object, Object> map;
    private EntityManagerFactory proxied;
    private List<Class<? extends Object>> dynamicEntityClasses = new ArrayList<>();
    private List<URL> extraConfigurationFiles = new ArrayList<>();

    private DynamicHibernatePersistenceProvider provider;

    private boolean initialized = false;
    private boolean shutDown = false;
    private boolean failed = false;
    private Throwable failedEx;
    private StatisticsInvocationHandler statistics;
    private boolean statisticsRegistered;


    /**
     * @param info
     * @param map
     */
    protected DynamicEntityManagerFactoryProxy ( DynamicHibernatePersistenceProvider provider, BasePersistenceUnitInfo info,
            Map<Object, Object> map ) {
        this.info = new DynamicPersistenceUnitInfoProxy(info);
        this.map = map;
        this.provider = provider;
        this.statistics = new StatisticsInvocationHandler(this);
    }


    /**
     * @param info
     * @param map
     * @param extraEntityClasses
     */
    protected DynamicEntityManagerFactoryProxy ( DynamicHibernatePersistenceProvider provider, BasePersistenceUnitInfo info, Map<Object, Object> map,
            List<Class<? extends Object>> extraEntityClasses, List<URL> extraConfigFiles ) {
        this.descriptor = info.getDescriptor();
        this.info = new DynamicPersistenceUnitInfoProxy(info);
        this.provider = provider;
        this.map = map;
        this.dynamicEntityClasses.addAll(extraEntityClasses);
        this.extraConfigurationFiles.addAll(extraConfigFiles);
        this.statistics = new StatisticsInvocationHandler(this);
    }


    protected void reconfigure () throws EntityManagerConfigurationFailedException {
        this.reconfigure(true);
    }


    protected synchronized void reconfigure ( boolean delayInit ) throws EntityManagerConfigurationFailedException {
        this.failed = false;
        this.failedEx = null;

        if ( this.shutDown ) {
            return;
        }

        if ( !this.initialized && delayInit ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Delaying initialization " + this.info.getPersistenceUnitName()); //$NON-NLS-1$
            }
            return;
        }

        if ( log.isDebugEnabled() ) {
            try {
                throw new IllegalStateException();
            }
            catch ( IllegalStateException e ) {
                log.debug("Reconfiguring dynamic entity manager factory " + this.info.getPersistenceUnitName(), e); //$NON-NLS-1$
            }
        }

        StandardServiceRegistry reg;
        MetadataImplementor config;
        synchronized ( this.dynamicEntityClasses ) {

            reg = buildServiceRegistry();

            try {
                config = setupMetadata(reg);
            }
            catch ( HibernateException e ) {
                log.error("EntityManagerFactory setup failed:", e); //$NON-NLS-1$
                throw new EntityManagerConfigurationFailedException("Failed to configure entity manager:", e); //$NON-NLS-1$
            }

        }

        rebuildEntityManagerFactory(reg, config);
    }


    /**
     * @return
     */
    private CompositeClassLoader createClassLoader ( List<Class<? extends Object>> entityClasses ) {
        // we need to construct a composite classloader for which all of the entity classes are visible
        // and pass it both as the current thread context classloader and through the PersistenceUnitInfo
        Set<ClassLoader> classloaders = new HashSet<>();
        classloaders.add(HibernatePersistenceProvider.class.getClassLoader());
        classloaders.add(Version.class.getClassLoader());
        classloaders.add(this.getClass().getClassLoader());

        for ( Class<? extends Object> entityClass : entityClasses ) {
            classloaders.add(entityClass.getClassLoader());
        }

        return new CompositeClassLoader(classloaders);
    }


    /**
     * @param serviceRegistry
     * @param base
     * @param compositeClassloader
     * @throws Exception
     */

    private MetadataImplementor setupMetadata ( StandardServiceRegistry reg ) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.info.getClassLoader());
        MetadataSources metadataSources = new MetadataSources(reg);
        setupMappingConfig(metadataSources);

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder(reg);
        // metadataBuilder.applyImplicitCatalogName(this.info.ge);
        // metadataBuilder.applyImplicitSchemaName(implicitSchemaName);
        metadataBuilder.applyPhysicalNamingStrategy(new QuotedTablePhysicalNamingStrategy());
        metadataBuilder.applyImplicitNamingStrategy(new LegacyHibernateImplicitNamingStrategy());

        setupAttributeConverters(metadataBuilder);

        MetadataImplementor meta = (MetadataImplementor) metadataBuilder.build();

        for ( PersistentClass clazz : meta.getEntityBindings() ) {
            setupBundleHeader(clazz);
        }

        Thread.currentThread().setContextClassLoader(current);
        return meta;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.spi.MetadataContributor#contribute(org.hibernate.boot.spi.InFlightMetadataCollector,
     *      org.jboss.jandex.IndexView)
     */
    @Override
    public void contribute ( InFlightMetadataCollector metaDataCollector, IndexView v ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.model.TypeContributor#contribute(org.hibernate.boot.model.TypeContributions,
     *      org.hibernate.service.ServiceRegistry)
     */
    @Override
    public void contribute ( TypeContributions typeContributions, ServiceRegistry sr ) {
        for ( TypeContributor contributor : this.provider.getTypeContributors() ) {
            if ( contributor.getClass().isAnnotationPresent(PersistenceUnit.class) ) {
                PersistenceUnit pu = contributor.getClass().getAnnotation(PersistenceUnit.class);
                if ( !pu.unitName().equals(this.info.getPersistenceUnitName()) ) {
                    continue;
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding type contributor " + contributor.getClass().getName()); //$NON-NLS-1$
            }

            contributor.contribute(typeContributions, sr);
        }
    }


    /**
     * @param config
     */
    private void setupAttributeConverters ( MetadataBuilder config ) {
        for ( AttributeConverter<?, ?> ac : this.provider.getAttributeConverters() ) {
            config.applyAttributeConverter(new AttributeConverterDefinition(ac, true));
        }
    }


    /**
     * 
     */
    private void setupMappingConfig ( MetadataSources config ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Persistence Unit %s entity classes:", this.info.getPersistenceUnitName())); //$NON-NLS-1$
        }

        for ( Class<? extends Object> entityClass : this.dynamicEntityClasses ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Configured entity class " + entityClass.getName()); //$NON-NLS-1$
            }
            config.addAnnotatedClass(entityClass);
        }

        for ( URL mappingFile : this.extraConfigurationFiles ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Activate mapping file: " + mappingFile); //$NON-NLS-1$
            }
            try ( InputStream s = mappingFile.openStream() ) {
                config.addInputStream(s);
            }
            catch ( IOException e ) {
                log.warn("Failed to add mapping file " + mappingFile, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param clazz
     */
    private static void setupBundleHeader ( PersistentClass clazz ) {
        Map<String, MetaAttribute> metaAttrs = clazz.getMetaAttributes();

        if ( metaAttrs == null ) {
            metaAttrs = new HashMap<>();
            clazz.setMetaAttributes(metaAttrs);
        }

        Class<?> mappedClass = clazz.getMappedClass();

        if ( mappedClass != null ) {
            MetaAttribute bsn = new MetaAttribute(Constants.BUNDLE_SYMBOLICNAME);
            bsn.addValue(FrameworkUtil.getBundle(mappedClass).getSymbolicName());
            metaAttrs.put(Constants.BUNDLE_SYMBOLICNAME, bsn);
        }
        else if ( log.isTraceEnabled() ) {
            log.trace("Mapped class is null " + clazz); //$NON-NLS-1$
        }
    }


    protected synchronized void rebuildEntityManagerFactory () throws EntityManagerConfigurationFailedException {
        StandardServiceRegistry sr = buildServiceRegistry();
        rebuildEntityManagerFactory(sr, setupMetadata(sr));
    }


    /**
     * @param config
     * @throws EntityManagerConfigurationFailedException
     * 
     */
    private synchronized void rebuildEntityManagerFactory ( StandardServiceRegistry reg, MetadataImplementor config )
            throws EntityManagerConfigurationFailedException {

        if ( this.shutDown ) {
            return;
        }

        synchronized ( this.dynamicEntityClasses ) {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.info.getClassLoader());

            EntityManagerFactory old = this.proxied;

            try {
                this.proxied = this.buildEntityManagerFactory(this.info.getPersistenceUnitName(), config, this.map, reg);

                if ( old != null ) {
                    synchronized ( old ) {
                        old.close();
                    }
                }

                registerStatistics();
                this.initialized = true;
            }
            catch ( Exception e ) {
                log.warn("Failed to reconfigure entity manager", e); //$NON-NLS-1$
                this.proxied = null;
                if ( e.getCause() != null ) {
                    throw new EntityManagerConfigurationFailedException(
                        String.format(
                            "Building the entity manager factory for persistence unit %s failed: %s", //$NON-NLS-1$
                            this.info.getPersistenceUnitName(),
                            e.getCause().getMessage()),
                        e);
                }

                throw new EntityManagerConfigurationFailedException(
                    String.format("Building the entity manager factory for persistence unit %s failed.", this.info.getPersistenceUnitName()), //$NON-NLS-1$
                    e);

            }
            finally {
                Thread.currentThread().setContextClassLoader(current);
            }
        }

    }


    /**
     * @param proxied2
     */
    private void registerStatistics () {
        if ( this.statisticsRegistered ) {
            return;
        }
        try {
            this.statisticsRegistered = true;
            ObjectName on = new ObjectName("org.hibernate:type=StatisticService,application=" + this.descriptor.getPersistenceUnitName()); //$NON-NLS-1$
            ManagementFactory.getPlatformMBeanServer().registerMBean(Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {
                StatisticsMXBean.class
            }, this.statistics), on);
        }
        catch ( InstanceAlreadyExistsException e ) {
            log.debug("Statistics already registered", e); //$NON-NLS-1$
        }
        catch ( JMException e ) {
            log.warn("Failed to register hibernate staticistics MBean", e); //$NON-NLS-1$
        }
    }


    /**
     * @param old
     */
    private void unregisterStatistics () {
        try {
            ObjectName on = new ObjectName("hibernate:type=StatisticService,application=" + this.descriptor.getPersistenceUnitName()); //$NON-NLS-1$
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(on);
        }
        catch ( JMException e ) {
            log.warn("Failed to unregister hibernate staticistics MBean", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * 
     */
    private synchronized StandardServiceRegistry buildServiceRegistry () {
        CompositeClassLoader compositeClassloader = createClassLoader(this.dynamicEntityClasses);
        this.info.setClassLoader(compositeClassloader);
        BootstrapServiceRegistryBuilder bootstrapBuilder = configureBootstrapServiceRegistryBuilder();
        return configureServiceRegistryBuilder(this.info, bootstrapBuilder.build());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.StandardServiceInitiator#initiateService(java.util.Map,
     *      org.hibernate.service.spi.ServiceRegistryImplementor)
     */
    @Override
    public ConnectionProvider initiateService ( Map configurationValues, ServiceRegistryImplementor registry ) {
        return new DataSourceConnectionProvider(this.info);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.service.spi.ServiceInitiator#getServiceInitiated()
     */
    @Override
    public Class<ConnectionProvider> getServiceInitiated () {
        return ConnectionProvider.class;
    }


    /**
     * @param bootstrapRegistry
     * @param cl
     * @return
     */
    private StandardServiceRegistry configureServiceRegistryBuilder ( PersistenceUnitInfo i, BootstrapServiceRegistry bootstrapRegistry ) {
        StandardServiceRegistryBuilder sb = new StandardServiceRegistryBuilder(bootstrapRegistry);

        @SuppressWarnings ( {
            "rawtypes", "unchecked"
        } )
        Map<String, Object> props = new HashMap<>((Map) i.getProperties());
        if ( CacheRegionFactory.isInitialized() ) {
            log.info("Enabling second level and query caching"); //$NON-NLS-1$
            props.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
            props.put(AvailableSettings.USE_QUERY_CACHE, true);
            props.put(AvailableSettings.CACHE_REGION_FACTORY, new CacheRegionFactory());
            props.put(AvailableSettings.DEFAULT_CACHE_CONCURRENCY_STRATEGY, "transactional"); //$NON-NLS-1$
        }

        props.put(AvailableSettings.JDBC_TIME_ZONE, "UTC"); //$NON-NLS-1$

        sb.applySettings(props);

        sb.addService(ConfigurationService.class, new ConfigurationServiceImpl(props));

        if ( i.getTransactionType() == PersistenceUnitTransactionType.JTA ) {
            sb.addService(JtaPlatform.class, new TransactionServiceJtaPlatform());
            sb.addService(TransactionCoordinatorBuilder.class, new JtaTransactionCoordinatorBuilderImpl());
        }
        else if ( i.getTransactionType() == PersistenceUnitTransactionType.RESOURCE_LOCAL ) {
            sb.addService(TransactionCoordinatorBuilder.class, new JdbcResourceLocalTransactionCoordinatorBuilderImpl());
        }
        else {
            log.error("Unknown transaction type " + i.getTransactionType()); //$NON-NLS-1$
        }

        sb.addService(ConnectionProvider.class, new DataSourceConnectionProvider(i));
        sb.addService(ClassLoaderService.class, new DynamicClassLoaderService(i, this));

        for ( ServiceContributor contributor : this.provider.getServiceContributors() ) {
            if ( contributor.getClass().isAnnotationPresent(PersistenceUnit.class) ) {
                PersistenceUnit pu = contributor.getClass().getAnnotation(PersistenceUnit.class);
                if ( !pu.unitName().equals(this.info.getPersistenceUnitName()) ) {
                    continue;
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding service contributor " + contributor.getClass().getName()); //$NON-NLS-1$
            }

            contributor.contribute(sb);
        }

        StandardServiceRegistry sr = sb.build();
        sr.getService(MutableIdentifierGeneratorFactory.class).register("uuid2-us", UUIDWhenNotSetGenerator.class); //$NON-NLS-1$
        return sr;
    }


    /**
     * @param standardServiceRegistry
     * @param configuration
     * @return
     * @throws SQLException
     */
    private EntityManagerFactory buildEntityManagerFactory ( String puName, MetadataImplementor hibernateConfiguration,
            Map<Object, Object> configurationValues, StandardServiceRegistry sr ) throws SQLException {

        SessionFactoryBuilder sfb = hibernateConfiguration.getSessionFactoryBuilder();
        SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) sfb.build();
        validateSchema(sessionFactory.getSessionFactoryOptions(), hibernateConfiguration, sr);
        return sessionFactory;
    }


    /**
     * @param sessionFactoryOptions
     * @throws Exception
     */
    private void validateSchema ( SessionFactoryOptions sessionFactoryOptions, MetadataImplementor metadata, ServiceRegistry reg )
            throws SQLException {

        if ( sessionFactoryOptions.getSchemaAutoTooling() == SchemaAutoTooling.VALIDATE || sessionFactoryOptions.getSchemaAutoTooling() == null ) {
            try {
                log.debug("Validating schema " + this.info.getPersistenceUnitName()); //$NON-NLS-1$
                DataSource ds = this.info.getJtaDataSource();

                if ( ds == null ) {
                    throw new HibernateException("No datasource available for " + this.info.getPersistenceUnitName()); //$NON-NLS-1$
                }

                try ( Connection c = ds.getConnection() ) {
                    new SchemaValidator().validate(metadata);
                }
            }
            catch ( HibernateException e ) {
                throw new DatabaseException(String.format(
                    "Database schema validation failed for persistence unit %s", //$NON-NLS-1$
                    this.info.getPersistenceUnitName()), e);
            }

            log.debug("Schema seems valid"); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws Exception
     */
    private BootstrapServiceRegistryBuilder configureBootstrapServiceRegistryBuilder () {
        BootstrapServiceRegistryBuilder builder = new BootstrapServiceRegistryBuilder();
        builder.applyClassLoaderService(new DynamicClassLoaderService(this.info, this));
        builder.disableAutoClose();

        setupIntegrators(builder);
        return builder;
    }


    /**
     * @param builder
     */
    private void setupIntegrators ( BootstrapServiceRegistryBuilder builder ) {
        for ( Integrator integrator : this.provider.getIntegrators() ) {

            if ( integrator.getClass().isAnnotationPresent(PersistenceUnit.class) ) {
                PersistenceUnit pu = integrator.getClass().getAnnotation(PersistenceUnit.class);
                if ( !pu.unitName().equals(this.info.getPersistenceUnitName()) ) {
                    continue;
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding integrator " + integrator.getClass().getName()); //$NON-NLS-1$
            }
            builder.applyIntegrator(integrator);
        }

        builder.applyIntegrator(new ListenerIntegrator(this.descriptor, this.provider));
        builder.applyIntegrator(new CollectionDirtyIntegrator());
    }


    /**
     * Add an entity class to the persistence unit
     * 
     * @param c
     */
    protected void addClass ( Class<? extends Object> c ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Adding class: " + c.getName()); //$NON-NLS-1$
        }
        synchronized ( this.dynamicEntityClasses ) {
            this.dynamicEntityClasses.add(c);
        }

    }


    /**
     * Remove an entity class from the persistence unit
     * 
     * @param c
     */
    protected void removeClass ( Class<? extends Object> c ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing class: " + c.getName()); //$NON-NLS-1$
        }
        synchronized ( this.dynamicEntityClasses ) {
            this.dynamicEntityClasses.remove(c);
        }
    }


    /**
     * Add an mapping file to the persistence unit
     * 
     * @param configFileURL
     */
    protected void addConfigFile ( URL configFileURL ) {
        log.info("Adding configuration file: " + configFileURL); //$NON-NLS-1$
        synchronized ( this.extraConfigurationFiles ) {
            this.extraConfigurationFiles.add(configFileURL);
        }
    }


    /**
     * Remove an mapping file from the persistence unit
     * 
     * @param configFileURL
     */
    protected void removeConfigFile ( URL configFileURL ) {
        log.info("Removing configuration file: " + configFileURL); //$NON-NLS-1$
        synchronized ( this.extraConfigurationFiles ) {
            this.extraConfigurationFiles.remove(configFileURL);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitInfoProvider#getPersistenceUnitInfo()
     */
    @Override
    public PersistenceUnitInfo getPersistenceUnitInfo () {
        return this.info;
    }


    @Override
    public synchronized EntityManager createEntityManager () {
        this.checkProxy();
        return createEMProxy(this.proxied.createEntityManager());
    }


    @Override
    public synchronized EntityManager createEntityManager ( Map m ) {
        this.checkProxy();
        return createEMProxy(this.proxied.createEntityManager(m));
    }


    /**
     * @param em
     * @return
     */
    private EntityManager createEMProxy ( EntityManager em ) {
        return (EntityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {
            EntityManager.class
        }, new DynamicEntityManagerInvocationHandler(em, this));
    }


    @Override
    public synchronized CriteriaBuilder getCriteriaBuilder () {
        this.checkProxy();
        return this.proxied.getCriteriaBuilder();
    }


    @Override
    public synchronized Metamodel getMetamodel () {
        this.checkProxy();
        return this.proxied.getMetamodel();
    }


    @Override
    public synchronized boolean isOpen () {
        if ( this.shutDown ) {
            return false;
        }
        this.checkProxy();
        return this.proxied.isOpen();
    }


    @Override
    public synchronized void close () {
        if ( this.proxied != null ) {
            this.proxied.close();
        }
        this.unregisterStatistics();
    }


    @Override
    public synchronized Map<String, Object> getProperties () {
        this.checkProxy();
        return this.proxied.getProperties();
    }


    @Override
    public synchronized Cache getCache () {
        this.checkProxy();
        return this.proxied.getCache();
    }


    @Override
    public synchronized PersistenceUnitUtil getPersistenceUnitUtil () {
        this.checkProxy();
        return this.proxied.getPersistenceUnitUtil();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.EntityManagerFactory#addNamedEntityGraph(java.lang.String, javax.persistence.EntityGraph)
     */

    @Override
    public synchronized <T> void addNamedEntityGraph ( String arg0, EntityGraph<T> arg1 ) {
        this.checkProxy();
        this.proxied.addNamedEntityGraph(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.EntityManagerFactory#addNamedQuery(java.lang.String, javax.persistence.Query)
     */
    @Override
    public synchronized void addNamedQuery ( String arg0, Query arg1 ) {
        this.checkProxy();
        this.proxied.addNamedQuery(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.EntityManagerFactory#createEntityManager(javax.persistence.SynchronizationType)
     */
    @Override
    public synchronized EntityManager createEntityManager ( SynchronizationType st ) {
        this.checkProxy();
        return createEMProxy(this.proxied.createEntityManager(st));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.EntityManagerFactory#createEntityManager(javax.persistence.SynchronizationType,
     *      java.util.Map)
     */
    @Override
    public synchronized EntityManager createEntityManager ( SynchronizationType st, Map opts ) {
        this.checkProxy();
        return createEMProxy(this.proxied.createEntityManager(st, opts));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.EntityManagerFactory#unwrap(java.lang.Class)
     */
    @Override
    public synchronized <T> T unwrap ( Class<T> arg0 ) {
        this.checkProxy();
        return this.proxied.unwrap(arg0);
    }


    private void checkProxy () {

        if ( this.shutDown ) {
            throw new IllegalStateException("EntityManagerFactory is already shut down"); //$NON-NLS-1$
        }

        if ( this.failed ) {
            throw new PersistenceException("EntityManagerFactory failed:", this.failedEx); //$NON-NLS-1$
        }

        try {
            doReconfigureProxy();
        }
        catch ( EntityManagerConfigurationFailedException e ) {
            throw new PersistenceException("Failed to reconfigure entity manager factory", e); //$NON-NLS-1$
        }

        if ( this.proxied == null ) {
            noProxyAvailable();
        }
    }


    private void noProxyAvailable () {
        log.warn(String.format(
            "No valid entitiy manager factory available, waiting %d msecs", //$NON-NLS-1$
            DynamicEntityManagerFactoryProxy.TIMEOUT));
        try {
            Thread.sleep(DynamicEntityManagerFactoryProxy.TIMEOUT);
        }
        catch ( InterruptedException e ) {
            log.warn("Interrupted during timeout:", e); //$NON-NLS-1$
        }

        if ( this.proxied == null ) {
            throw new IllegalStateException(String.format(
                "No entity manager factory for PU %s available", //$NON-NLS-1$
                this.info.getPersistenceUnitName()));
        }
    }


    private void doReconfigureProxy () throws EntityManagerConfigurationFailedException {
        synchronized ( this.lock ) {
            if ( !this.initialized ) {
                try {
                    this.reconfigure(false);
                }
                catch ( EntityManagerConfigurationFailedException e ) {
                    this.failed = true;
                    this.failedEx = e;
                    throw e;
                }
            }
        }
    }


    /**
     * 
     */
    public synchronized void shutdown () {
        if ( this.proxied != null ) {
            this.proxied.close();
        }
        this.shutDown = true;
    }


    /**
     * @return additional mapping producers
     */
    public Set<AdditionalJaxbMappingProducer> getAdditionalMappingProducers () {
        return this.provider.getAdditionMappingProducers();
    }

}

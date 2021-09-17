/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.service.spi.ServiceContributor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.orm.DynamicPersistenceProvider;
import eu.agno3.runtime.db.orm.EntityManagerConfigurationFailedException;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationListener;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;
import eu.agno3.runtime.update.RefreshListener;


/**
 * @author mbechler
 * 
 */

@Component ( service = {
    PersistenceProvider.class, DynamicPersistenceProvider.class, RefreshListener.class, PlatformStateListener.class
}, property = {
    "javax.persistence.provider=org.hibernate.ejb.HibernatePersistence"
}, immediate = true )
@SuppressWarnings ( "deprecation" )
public class DynamicHibernatePersistenceProvider extends HibernatePersistenceProvider
        implements HibernateConfigurationListener, DynamicPersistenceProvider, RefreshListener, PlatformStateListener {

    private static final Logger log = Logger.getLogger(DynamicHibernatePersistenceProvider.class);

    private DynamicHibernateProxyHolder proxyHolder = new DynamicHibernateProxyHolder();

    private Set<String> classes = new HashSet<>();
    private Set<Integrator> integrators = new HashSet<>();
    private Set<TypeContributor> typeContributors = new HashSet<>();
    private Set<ServiceContributor> serviceContributors = new HashSet<>();
    private Set<AttributeConverter<?, ?>> attributeConverters = new HashSet<>();
    private Set<HibernateConfigurationListener> configurationListeners = new HashSet<>();

    private Set<AdditionalJaxbMappingProducer> additionMappingProducers = new HashSet<>();

    private DynamicHibernateBundleTracker hibernateBundleTracker;

    private boolean updating = false;

    private List<RefreshHolder> toRefresh = new LinkedList<>();

    private boolean needRefreshAll = false;

    private boolean shutdown;


    /**
     * 
     */
    public DynamicHibernatePersistenceProvider () {}


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        if ( state == PlatformState.STOPPING ) {
            this.shutdown = true;
        }
        else if ( state == PlatformState.UPDATING ) {
            this.updating = true;
        }
        else if ( state == PlatformState.STARTED ) {
            this.updating = false;
        }
        else if ( state == PlatformState.STARTED || state == PlatformState.WARNING ) {
            this.hibernateBundleTracker.booted();
            refreshDelayed();
        }

    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindIntegrator ( Integrator integrator ) {
        this.integrators.add(integrator);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding integrator " + integrator.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating || isShuttingDown() ) {
            this.needRefreshAll = true;
        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(integrator.getClass());
        }
    }


    protected synchronized void unbindIntegrator ( Integrator integrator ) {
        this.integrators.remove(integrator);
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding integrator " + integrator.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating || isShuttingDown() ) {
            this.needRefreshAll = true;
        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(integrator.getClass());
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindTypeContributor ( TypeContributor typeContributor ) {
        this.typeContributors.add(typeContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding type contributor " + typeContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating || isShuttingDown() ) {
            this.needRefreshAll = true;
        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(typeContributor.getClass());
        }
    }


    protected synchronized void unbindTypeContributor ( TypeContributor typeContributor ) {
        this.typeContributors.remove(typeContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding type contributor " + typeContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating || isShuttingDown() ) {
            this.needRefreshAll = true;
        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(typeContributor.getClass());
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindServiceContributor ( ServiceContributor serviceContributor ) {
        this.serviceContributors.add(serviceContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding type contributor " + serviceContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(serviceContributor.getClass());
        }
    }


    protected synchronized void unbindServiceContributor ( ServiceContributor serviceContributor ) {
        this.serviceContributors.remove(serviceContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding type contributor " + serviceContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(serviceContributor.getClass());
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindMappingProducer ( AdditionalJaxbMappingProducer serviceContributor ) {
        this.additionMappingProducers.add(serviceContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding type contributor " + serviceContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(serviceContributor.getClass());
        }
    }


    protected synchronized void unbindMappingProducer ( AdditionalJaxbMappingProducer serviceContributor ) {
        this.additionMappingProducers.remove(serviceContributor);
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding type contributor " + serviceContributor.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureReferencedProxies(serviceContributor.getClass());
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindAttributeConverter ( AttributeConverter<?, ?> attributeConverter ) {
        this.attributeConverters.add(attributeConverter);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding attribute converter " + attributeConverter.getClass().getName()); //$NON-NLS-1$
        }

        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureAllProxies();
        }
    }


    protected synchronized void unbindAttributeConverter ( AttributeConverter<?, ?> attributeConverter ) {
        this.attributeConverters.remove(attributeConverter);
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding attribute converter " + attributeConverter.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.updating ) {
            this.needRefreshAll = true;
        }
        else if ( isShuttingDown() ) {

        }
        else {
            this.proxyHolder.reconfigureAllProxies();
        }
    }


    /**
     * @return
     */
    private boolean isShuttingDown () {
        return this.shutdown;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindConfigurationListener ( HibernateConfigurationListener l ) {
        this.configurationListeners.add(l);
    }


    protected synchronized void unbindConfigurationListener ( HibernateConfigurationListener l ) {
        this.configurationListeners.remove(l);
    }


    @Reference
    protected synchronized void bindCacheRegionFactory ( CacheRegionFactory l ) {
        // dep only
    }


    protected synchronized void unbindCacheRegionFactory ( CacheRegionFactory l ) {
        // dep only
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Dynamic hibernate persistence provider initializing"); //$NON-NLS-1$
        this.hibernateBundleTracker = new DynamicHibernateBundleTracker(this);
        this.hibernateBundleTracker.start(context.getBundleContext());
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.hibernateBundleTracker.stop();
        this.proxyHolder.stopAllProxies();
    }


    /**
     * @param pu
     * @param info
     */
    void removeBundleContributions ( String pu, DynamicHibernateBundleInfo info ) {
        for ( Class<? extends Object> c : info.getClassRegistrations().get(pu) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Removing class " + c.getName()); //$NON-NLS-1$
            }
            this.classes.remove(c.getName());
        }

        this.proxyHolder.refreshProxiesRemove(info, pu);
    }


    @Override
    public synchronized EntityManagerFactory createContainerEntityManagerFactory ( PersistenceUnitInfo info, Map map ) {

        if ( ! ( info instanceof BasePersistenceUnitInfo ) ) {
            throw new PersistenceException("Need BasePersistenceInfo"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Creating EMF for PU " + info.getPersistenceUnitName()); //$NON-NLS-1$
        }

        List<Class<? extends Object>> dynamicEntityClasses = new ArrayList<>();
        List<URL> extraMappingFiles = new ArrayList<>();

        for ( DynamicHibernateBundleInfo bundleInfo : this.hibernateBundleTracker.getBundleContributions() ) {
            if ( bundleInfo.getClassRegistrations().containsKey(info.getPersistenceUnitName()) ) {
                dynamicEntityClasses.addAll(bundleInfo.getClassRegistrations().get(info.getPersistenceUnitName()));
            }

            if ( bundleInfo.getMappingFiles().containsKey(info.getPersistenceUnitName()) ) {
                extraMappingFiles.addAll(bundleInfo.getMappingFiles().get(info.getPersistenceUnitName()));
            }
        }

        DynamicEntityManagerFactoryProxy proxy = new DynamicEntityManagerFactoryProxy(
            this,
            (BasePersistenceUnitInfo) info,
            map,
            dynamicEntityClasses,
            extraMappingFiles);
        try {
            proxy.reconfigure();
            this.proxyHolder.addProxy(info.getPersistenceUnitName(), proxy);
        }
        catch ( EntityManagerConfigurationFailedException e ) {
            log.error("Failed to configure entity manager:", e); //$NON-NLS-1$
        }
        return proxy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#startBundleUpdate()
     */
    @Override
    public void startBundleUpdate () {
        log.debug("Starting update"); //$NON-NLS-1$
        this.updating = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesRefreshed()
     */
    @Override
    public void bundlesRefreshed () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesStarted()
     */
    @Override
    public void bundlesStarted () {
        // ignore
    }


    /**
     * 
     */
    @Override
    public void bundlesUpdated () {
        log.debug("Bundles were updated"); //$NON-NLS-1$
        synchronized ( this.toRefresh ) {
            this.updating = false;
            refreshDelayed();
        }

    }


    /**
     * 
     */
    private void refreshDelayed () {
        Set<String> reconfigurePus = new HashSet<>();
        for ( RefreshHolder h : this.toRefresh ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Refreshing proxy for PU " + h.getPu()); //$NON-NLS-1$
            }
            this.proxyHolder.refreshProxies(h.getBundleInfo(), h.getPu(), false);
            reconfigurePus.add(h.getPu());

        }
        this.toRefresh.clear();

        if ( !this.needRefreshAll ) {
            for ( String pu : reconfigurePus ) {
                this.proxyHolder.reconfigurePersistenceUnitProxies(pu);
            }
        }
        else {
            this.proxyHolder.reconfigureAllProxies();
            this.needRefreshAll = false;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.DynamicPersistenceProvider#getPersistenceUnits()
     */
    @Override
    public Set<String> getPersistenceUnits () {
        return this.proxyHolder.getPersistenceUnits();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityManagerConfigurationFailedException
     * 
     * @see eu.agno3.runtime.db.orm.DynamicPersistenceProvider#rebuildEntityManagerFactory(java.lang.String)
     */
    @Override
    public void rebuildEntityManagerFactory ( String pu ) throws EntityManagerConfigurationFailedException {
        this.proxyHolder.rebuildEntityManagerFactory(pu);
    }


    /**
     * @param bundleInfo
     * @param pu
     */
    void refreshProxies ( DynamicHibernateBundleInfo bundleInfo, String pu, boolean noRefresh ) {
        synchronized ( this.toRefresh ) {
            if ( !this.updating ) {
                this.proxyHolder.refreshProxies(bundleInfo, pu, !noRefresh);
            }
            else {
                log.debug("Not refreshing proxies as an update is currently in progress"); //$NON-NLS-1$
                this.toRefresh.add(new RefreshHolder(bundleInfo, pu));
            }
        }
    }


    /**
     * @param entries
     * @throws DatabaseConfigurationException
     */
    void validateEntries ( Map<String, List<String>> entries ) throws DatabaseConfigurationException {
        synchronized ( this.classes ) {
            for ( List<String> classList : entries.values() ) {
                for ( String clazz : classList ) {
                    if ( this.classes.contains(clazz) ) {
                        throw new DatabaseConfigurationException("Bundled tries to register duplicate entity class"); //$NON-NLS-1$
                    }
                }
            }
        }
    }


    /**
     * @return all registered Integrators
     */
    protected Set<Integrator> getIntegrators () {
        return this.integrators;
    }


    /**
     * 
     * @return all registered TypeContributors
     */
    protected Set<TypeContributor> getTypeContributors () {
        return this.typeContributors;
    }


    protected Set<AttributeConverter<?, ?>> getAttributeConverters () {
        return this.attributeConverters;
    }


    /**
     * @return registered service contributors
     */
    public Set<ServiceContributor> getServiceContributors () {
        return this.serviceContributors;
    }


    /**
     * @return the additionMappingProducers
     */
    public Set<AdditionalJaxbMappingProducer> getAdditionMappingProducers () {
        return this.additionMappingProducers;
    }


    /**
     * @param puName
     * @param meta
     */
    @Override
    public void generatedConfiguation ( PersistenceUnitDescriptor puName, Metadata meta ) {
        for ( HibernateConfigurationListener l : this.configurationListeners ) {
            l.generatedConfiguation(puName, meta);
        }
    }


    /**
     * 
     */
    void refreshAllProxies () {
        this.proxyHolder.reconfigureAllProxies();
    }

}

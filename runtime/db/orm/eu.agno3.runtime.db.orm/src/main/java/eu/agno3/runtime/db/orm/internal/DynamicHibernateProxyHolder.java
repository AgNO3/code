/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.apache.log4j.Logger;

import eu.agno3.runtime.db.orm.EntityManagerConfigurationFailedException;


/**
 * @author mbechler
 * 
 */
class DynamicHibernateProxyHolder {

    private static final Logger log = Logger.getLogger(DynamicHibernateProxyHolder.class);
    private Map<String, List<WeakReference<DynamicEntityManagerFactoryProxy>>> proxies = new HashMap<>();


    /**
     * 
     */
    public DynamicHibernateProxyHolder () {}


    /**
     * 
     * @return all known persistence units
     */
    public Set<String> getPersistenceUnits () {
        return this.proxies.keySet();
    }


    /**
     * 
     * @param pu
     * @param proxy
     */
    public void addProxy ( String pu, DynamicEntityManagerFactoryProxy proxy ) {
        synchronized ( this.proxies ) {
            if ( !this.proxies.containsKey(pu) ) {
                this.proxies.put(pu, new ArrayList<WeakReference<DynamicEntityManagerFactoryProxy>>());
            }

            this.proxies.get(pu).add(new WeakReference<>(proxy));
        }
    }


    /**
     * @param clazz
     */
    public void reconfigureReferencedProxies ( Class<?> clazz ) {
        if ( clazz.isAnnotationPresent(PersistenceUnit.class) ) {
            PersistenceUnit pu = clazz.getAnnotation(PersistenceUnit.class);
            reconfigurePersistenceUnitProxies(pu);
        }
        else {
            reconfigureAllProxies();
        }
    }


    /**
     * @param pu
     */
    public void reconfigurePersistenceUnitProxies ( PersistenceUnit pu ) {
        List<WeakReference<DynamicEntityManagerFactoryProxy>> puProxies = this.proxies.get(pu.unitName());
        if ( puProxies != null ) {
            reconfigureProxyList(puProxies);
        }
    }


    /**
     * @param pu
     */
    public void reconfigurePersistenceUnitProxies ( String pu ) {
        List<WeakReference<DynamicEntityManagerFactoryProxy>> puProxies = this.proxies.get(pu);
        if ( puProxies != null ) {
            reconfigureProxyList(puProxies);
        }
    }


    /**
     * Recofigure all existing proxies
     */
    public void reconfigureAllProxies () {
        for ( List<WeakReference<DynamicEntityManagerFactoryProxy>> puProxies : this.proxies.values() ) {
            reconfigureProxyList(puProxies);
        }
    }


    private static void reconfigureProxyList ( List<WeakReference<DynamicEntityManagerFactoryProxy>> puProxies ) {
        for ( WeakReference<DynamicEntityManagerFactoryProxy> puProxy : puProxies ) {
            DynamicEntityManagerFactoryProxy emf = puProxy.get();
            if ( emf != null ) {
                try {
                    log.debug("Reconfiguring proxy " + emf); //$NON-NLS-1$
                    emf.reconfigure();
                }
                catch ( EntityManagerConfigurationFailedException e ) {
                    log.error("Failed to reconfigure proxy: ", e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * Close all proxies
     * 
     */
    public void stopAllProxies () {
        synchronized ( this.proxies ) {
            for ( List<WeakReference<DynamicEntityManagerFactoryProxy>> puProxies : this.proxies.values() ) {
                for ( WeakReference<DynamicEntityManagerFactoryProxy> puProxy : puProxies ) {
                    DynamicEntityManagerFactoryProxy emf = puProxy.get();
                    if ( emf != null && emf.isOpen() ) {
                        try {
                            emf.close();
                        }
                        catch (
                            IllegalArgumentException |
                            PersistenceException e ) {
                            log.warn("Failed to close proxy", e); //$NON-NLS-1$
                        }
                    }
                }
            }

            this.proxies.clear();
        }
    }


    /**
     * Rebuild the entity manager factories of all proxies serving this PU
     * 
     * @param pu
     * @throws EntityManagerConfigurationFailedException
     */
    public void rebuildEntityManagerFactory ( String pu ) throws EntityManagerConfigurationFailedException {
        if ( !this.proxies.containsKey(pu) ) {
            throw new IllegalArgumentException("Unknown persistence unit " + pu); //$NON-NLS-1$
        }
        for ( WeakReference<DynamicEntityManagerFactoryProxy> puProxy : this.proxies.get(pu) ) {
            DynamicEntityManagerFactoryProxy emf = puProxy.get();
            if ( emf != null ) {
                emf.rebuildEntityManagerFactory();
            }
        }
    }


    /**
     * Refresh all proxies referencing thee given PU
     * 
     * @param bundleInfo
     * @param pu
     * @param reconfigure
     */
    public void refreshProxies ( DynamicHibernateBundleInfo bundleInfo, String pu, boolean reconfigure ) {
        synchronized ( this.proxies ) {
            if ( this.proxies.containsKey(pu) ) {
                List<WeakReference<DynamicEntityManagerFactoryProxy>> toRemove = new ArrayList<>();
                for ( WeakReference<DynamicEntityManagerFactoryProxy> proxy : this.proxies.get(pu) ) {
                    refreshBundleEmf(bundleInfo, pu, toRemove, proxy, reconfigure);
                }
                this.proxies.get(pu).removeAll(toRemove);
            }
        }
    }


    /**
     * Refresh all proxies referencing thee given PU
     * 
     * @param bundleInfo
     * @param pu
     */
    public void refreshProxiesRemove ( DynamicHibernateBundleInfo bundleInfo, String pu ) {
        synchronized ( this.proxies ) {
            if ( this.proxies.containsKey(pu) ) {
                List<WeakReference<DynamicEntityManagerFactoryProxy>> toRemove = new ArrayList<>();
                // remove classes from proxies
                for ( WeakReference<DynamicEntityManagerFactoryProxy> proxy : this.proxies.get(pu) ) {
                    refreshBundleEmfRemove(bundleInfo, pu, toRemove, proxy);
                }
                this.proxies.get(pu).removeAll(toRemove);
            }
        }
    }


    /**
     * @param bundleInfo
     * @param pu
     * @param toRemove
     * @param proxy
     * @param reconfigure
     */
    private static void refreshBundleEmf ( DynamicHibernateBundleInfo bundleInfo, String pu,
            List<WeakReference<DynamicEntityManagerFactoryProxy>> toRemove, WeakReference<DynamicEntityManagerFactoryProxy> proxy,
            boolean reconfigure ) {
        DynamicEntityManagerFactoryProxy emf = proxy.get();
        if ( emf != null ) {

            if ( bundleInfo.getClassRegistrations().containsKey(pu) ) {
                for ( Class<? extends Object> c : bundleInfo.getClassRegistrations().get(pu) ) {
                    emf.addClass(c);
                }
            }

            if ( bundleInfo.getMappingFiles().containsKey(pu) ) {
                for ( URL mappingUrl : bundleInfo.getMappingFiles().get(pu) ) {
                    emf.addConfigFile(mappingUrl);
                }
            }

            try {
                if ( reconfigure ) {
                    emf.reconfigure();
                }
            }
            catch ( EntityManagerConfigurationFailedException e ) {
                log.error("Failed to reconfigure proxy after bundle refresh:", e); //$NON-NLS-1$
            }
        }
        else {
            toRemove.add(proxy);
        }
    }


    /**
     * @param bundleInfo
     * @param pu
     * @param toRemove
     * @param proxy
     */
    private static void refreshBundleEmfRemove ( DynamicHibernateBundleInfo bundleInfo, String pu,
            List<WeakReference<DynamicEntityManagerFactoryProxy>> toRemove, WeakReference<DynamicEntityManagerFactoryProxy> proxy ) {
        DynamicEntityManagerFactoryProxy emf = proxy.get();
        if ( emf != null ) {

            if ( bundleInfo.getClassRegistrations().containsKey(pu) ) {
                for ( Class<? extends Object> c : bundleInfo.getClassRegistrations().get(pu) ) {
                    emf.removeClass(c);
                }
            }

            if ( bundleInfo.getMappingFiles().containsKey(pu) ) {
                for ( URL mappingUrl : bundleInfo.getMappingFiles().get(pu) ) {
                    emf.removeConfigFile(mappingUrl);
                }
            }
        }
        else {
            toRemove.add(proxy);
        }
    }

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public class InheritanceProxyCache {

    private Map<CacheKey, ConfigurationObject> defaultsProxyCache = new LRUMap<>(100);
    private Map<CacheKey, ConfigurationObject> enforcementProxyCache = new LRUMap<>(100);


    /**
     * 
     * @param k
     * @return a defaults proxy for the given cache key
     */
    public ConfigurationObject getDefaultsProxy ( CacheKey k ) {
        return this.defaultsProxyCache.get(k);
    }


    /**
     * 
     * @param k
     * @param defaultsProxy
     */
    public void putDefaultsProxy ( CacheKey k, ConfigurationObject defaultsProxy ) {
        this.defaultsProxyCache.put(k, defaultsProxy);
    }


    /**
     * 
     * @param k
     * @return an enforcement proxy for the given cache key
     */
    public ConfigurationObject getEnforcementProxy ( CacheKey k ) {
        return this.enforcementProxyCache.get(k);
    }


    /**
     * 
     * @param k
     * @param enforcementProxy
     */
    public void putEnforcementProxy ( CacheKey k, ConfigurationObject enforcementProxy ) {
        this.enforcementProxyCache.put(k, enforcementProxy);
    }

}

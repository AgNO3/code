/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class EffectiveConfigCacheBean implements Serializable {

    private static final Logger log = Logger.getLogger(EffectiveConfigCacheBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;

    private Ehcache cacheInstance;

    /**
     * 
     */
    private static final long serialVersionUID = -5528652774499320269L;

    private static final String CONFIG_CACHE = "server-webgui-effective-config"; //$NON-NLS-1$


    public ConfigurationObject getEffectiveConfig ( ConfigurationObjectReference ref, String rootObjectType )
            throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        Ehcache cache = getCache();
        Serializable key = makeEffectiveCacheKey(ref, rootObjectType);
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (ConfigurationObject) cached.getObjectValue();
        }

        try {
            ConfigurationObject effectiveConfig = this.ssp.getService(InheritanceService.class).getEffective(ref, rootObjectType);

            if ( effectiveConfig != null && log.isDebugEnabled() ) {
                log.debug("Found config " + effectiveConfig); //$NON-NLS-1$
            }

            cache.put(new Element(key, effectiveConfig));
            return effectiveConfig;
        }

        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null));
            throw e;
        }
    }


    /**
     * @param ref
     * @param rootObjectType
     * @return
     */
    private static Serializable makeEffectiveCacheKey ( ConfigurationObjectReference ref, String rootObjectType ) {
        return String.format("effective/%s/%s", ref.getId(), rootObjectType); //$NON-NLS-1$ ;
    }


    public ConfigurationObject getEffectiveSingletonConfig ( StructuralObject anchor, String objType, String rootObjectType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObjectReference ref = getEffectiveConfigLocation(anchor, objType);
        if ( ref == null ) {
            return null;
        }
        return getEffectiveConfig(ref, rootObjectType);
    }


    public ConfigurationObjectReference getEffectiveConfigLocation ( StructuralObject anchor, String objType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        String key = makeConfigLocationCacheKey(anchor.getId(), objType);
        Ehcache cache = getCache();
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (ConfigurationObjectReference) cached.getObjectValue();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading config effective config for type %s", objType)); //$NON-NLS-1$
        }

        try {
            ConfigurationObjectReference effectiveConfig = this.ssp.getService(ServiceService.class).getServiceConfigurationLocation(anchor, objType);

            if ( log.isDebugEnabled() ) {
                log.debug("Found config " + effectiveConfig); //$NON-NLS-1$
            }

            if ( effectiveConfig != null ) {

            }

            cache.put(new Element(key, effectiveConfig));
            return effectiveConfig;
        }
        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null));
            throw e;
        }

    }


    /**
     * @param id
     * @param objType
     * @param filter
     * @return
     */
    private static String makeConfigLocationCacheKey ( UUID id, String objType ) {
        return String.format("%s/%s", id, objType); //$NON-NLS-1$
    }


    private Ehcache getCache () {
        if ( this.cacheInstance == null ) {
            this.cacheInstance = this.csp.getCacheService().getCache(CONFIG_CACHE);
        }
        return this.cacheInstance;
    }


    public void flush () {
        this.getCache().removeAll();
    }

}

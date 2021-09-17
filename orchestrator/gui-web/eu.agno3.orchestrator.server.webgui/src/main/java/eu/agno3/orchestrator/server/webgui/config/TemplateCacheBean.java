/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.DefaultsService;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
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
public class TemplateCacheBean implements Serializable {

    private static final Logger log = Logger.getLogger(TemplateCacheBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;

    private Ehcache cacheInstance;

    /**
     * 
     */
    private static final long serialVersionUID = -5528652774499320269L;

    private static final String TEMPLATE_CACHE = "server-webgui-templates"; //$NON-NLS-1$


    @SuppressWarnings ( "unchecked" )
    public List<ConfigurationObjectReference> getTemplatesForType ( StructuralObject anchor, String objType, String filter )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        String key = makeTemplateCacheKey(anchor.getId(), objType, filter);
        Ehcache cache = getCache();
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return Collections.unmodifiableList((List<ConfigurationObjectReference>) cached.getObjectValue());
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading templates for type %s and filter '%s'", objType, filter)); //$NON-NLS-1$
        }

        List<ConfigurationObjectReference> refs;

        try {
            refs = this.ssp.getService(InheritanceService.class).getEligibleTemplates(anchor, objType, filter);
            if ( log.isDebugEnabled() ) {
                log.debug("Found templates " + refs); //$NON-NLS-1$
            }
            cache.put(new Element(key, refs));
        }
        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null));
            throw e;
        }

        if ( refs != null ) {
            return new ArrayList<>(refs);
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * @param objectType
     * @param anchor
     * @param rootObjectType
     *            the root object type
     * @return the structural defaults for the given object type at the given anchor
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public ConfigurationObject getDefaultsForTypeAt ( String objectType, StructuralObject anchor, String rootObjectType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( anchor == null ) {
            return null;
        }
        String key = makeDefaultCacheKey(anchor.getId(), objectType, rootObjectType);
        Ehcache cache = getCache();
        Element cached = cache.get(key);
        if ( cached != null && !cached.isExpired() ) {
            return (ConfigurationObject) cached.getObjectValue();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading defaults for type %s at '%s' (root type: %s)", objectType, anchor, rootObjectType)); //$NON-NLS-1$
        }

        ConfigurationObject def = null;
        try {
            def = this.ssp.getService(DefaultsService.class).getDefaultsFor(anchor, objectType, rootObjectType);
            cache.put(new Element(key, def));
        }
        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null));
            throw e;
        }
        return def;
    }


    /**
     * @param id
     * @param objType
     * @param rootObjectType
     * @param filter
     * @return
     */
    private static String makeDefaultCacheKey ( UUID id, String objType, String rootObjectType ) {
        return String.format("default/%s/%s/%s", id, objType, rootObjectType); //$NON-NLS-1$
    }


    /**
     * @param id
     * @param objType
     * @param filter
     * @return
     */
    private static String makeTemplateCacheKey ( UUID id, String objType, String filter ) {
        return String.format("%s/%s/%s", id, objType, filter); //$NON-NLS-1$
    }


    private static String makeApplicableTypeCacheKey ( String baseType ) {
        return "applicable:" + baseType; //$NON-NLS-1$
    }


    public static String makeResourceLibraryCacheKey ( UUID anchor ) {
        return "reslibrary:" + anchor; //$NON-NLS-1$
    }


    private Ehcache getCache () {
        if ( this.cacheInstance == null ) {
            this.cacheInstance = this.csp.getCacheService().getCache(TEMPLATE_CACHE);
        }
        return this.cacheInstance;
    }


    public void flush () {
        this.getCache().removeAll();
    }


    /**
     * @param objectType
     * @return the types applicable for the given base type
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public Set<String> getApplicableTypesFor ( String objectType ) throws ModelServiceException, GuiWebServiceException {
        String key = makeApplicableTypeCacheKey(objectType);
        Ehcache cache = getCache();
        Element cached = cache.get(key);
        if ( cached != null && !cached.isExpired() ) {
            return (Set<String>) cached.getObjectValue();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading applicable types for type %s", objectType)); //$NON-NLS-1$
        }

        Set<String> applicableTypes;

        try {
            applicableTypes = this.ssp.getService(ConfigurationService.class).getApplicableTypes(objectType);
            cache.put(new Element(key, applicableTypes));
        }
        catch ( Exception e ) {
            cache.put(new Element(key, null));
            throw e;
        }

        if ( applicableTypes != null ) {
            return new HashSet<>(applicableTypes);
        }
        return Collections.EMPTY_SET;
    }


    /**
     * 
     * @param anchor
     * @return the resource libraries usable at anchor
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public List<ResourceLibrary> getResourceLibraries ( StructuralObject anchor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        String key = makeResourceLibraryCacheKey(anchor.getId());
        Ehcache cache = getCache();
        Element cached = cache.get(key);
        if ( cached != null && !cached.isExpired() ) {
            return (List<ResourceLibrary>) cached.getObjectValue();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading resourcelibraries at %s", anchor)); //$NON-NLS-1$
        }

        List<ResourceLibrary> resourceLibraries;

        try {
            resourceLibraries = this.ssp.getService(ResourceLibraryService.class).getUsableResourceLibraries(anchor, null, false);
            cache.put(new Element(key, resourceLibraries));
        }
        catch ( Exception e ) {
            cache.put(new Element(key, null));
            throw e;
        }

        if ( resourceLibraries != null ) {
            return new ArrayList<>(resourceLibraries);
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * 
     * @param anchor
     * @param type
     * @return the usable resource libraries matching type
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public List<ResourceLibrary> getResourceLibrariesWithType ( StructuralObject anchor, String type )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<ResourceLibrary> res = new ArrayList<>();
        for ( ResourceLibrary resourceLibrary : getResourceLibraries(anchor) ) {
            if ( resourceLibrary.getType().equals(type) ) {
                res.add(resourceLibrary);
            }
        }

        return res;
    }

}

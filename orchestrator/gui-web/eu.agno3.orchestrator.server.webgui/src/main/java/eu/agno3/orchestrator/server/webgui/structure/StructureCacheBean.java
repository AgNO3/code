/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
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
@Named ( "structureCacheBean" )
public class StructureCacheBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5550995341890556257L;

    private static final Logger log = Logger.getLogger(StructureCacheBean.class);

    private static final String STRUCTURE_CACHE = "server-webgui-structure"; //$NON-NLS-1$

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ServerServiceProvider ssp;

    private Ehcache cacheInstance;


    public StructuralObject getById ( UUID id ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        Ehcache cache = getCache();
        Serializable cacheKey = makeCacheKey(id);
        Element cached = cache.get(cacheKey);

        if ( cached != null && !cached.isExpired() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Returning cached entry " + cached.getObjectValue()); //$NON-NLS-1$
            }
            return (StructuralObject) cached.getObjectValue();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Loading entry for " + id); //$NON-NLS-1$
        }

        StructuralObject loaded = null;

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading structural object " + id); //$NON-NLS-1$
            }
            loaded = this.ssp.getService(StructuralObjectService.class).fetchById(id);
            cache.put(new Element(cacheKey, loaded));
        }
        catch ( Exception e ) {
            cache.put(new Element(cacheKey, null));
            throw e;
        }
        return loaded;
    }


    private Ehcache getCache () {
        if ( this.cacheInstance == null ) {
            this.cacheInstance = this.csp.getCacheService().getCache(STRUCTURE_CACHE);
        }
        return this.cacheInstance;
    }


    public void flush () {
        log.debug("Flushing cache"); //$NON-NLS-1$
        this.getCache().removeAll();
    }


    /**
     * @param id
     * @return
     */
    private static Serializable makeCacheKey ( UUID id ) {
        // TODO: add user information
        return "struct-" + id; //$NON-NLS-1$
    }


    public StructuralObject getParentFor ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( obj == null ) {
            return null;
        }

        Ehcache cache = this.getCache();
        Serializable cacheKey = makeParentCacheKey(obj.getId());
        Element cached = cache.get(cacheKey);

        if ( cached != null ) {
            UUID parentId = (UUID) cached.getObjectValue();
            if ( parentId != null ) {
                return this.getById(parentId);
            }
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Loading parent for " + obj); //$NON-NLS-1$
        }

        return fetchParentInternal(obj, cacheKey);
    }


    /**
     * @param id
     * @return
     */
    private static Serializable makeParentCacheKey ( UUID id ) {
        return "struct-parent-" + id; //$NON-NLS-1$
    }


    private StructuralObject fetchParentInternal ( StructuralObject obj, Serializable cacheKey )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Ehcache cache = this.getCache();

        try {
            StructuralObject parent = this.ssp.getService(StructuralObjectService.class).fetchParent(obj);

            if ( parent != null ) {
                Serializable parentObjCacheKey = makeCacheKey(parent.getId());
                cache.put(new Element(cacheKey, parent.getId()));
                cache.put(new Element(parentObjCacheKey, parent));
                return parent;
            }
        }
        catch ( Exception e ) {
            cache.put(new Element(cacheKey, null));
            throw e;
        }

        cache.put(new Element(cacheKey, null));
        return null;
    }


    /**
     * @param id
     */
    public void flush ( UUID id ) {
        this.getCache().remove(makeCacheKey(id));
    }
}

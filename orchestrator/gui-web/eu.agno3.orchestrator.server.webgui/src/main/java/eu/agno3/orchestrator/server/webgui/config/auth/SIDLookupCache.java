/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.realms.RealmLookupResult;
import eu.agno3.orchestrator.realms.service.RealmLookupService;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class SIDLookupCache {

    private static final Logger log = Logger.getLogger(SIDLookupCache.class);

    private static final String SID_CACHE = "server-webgui-ad-sid"; //$NON-NLS-1$

    private static final int LOOKUP_POS_TTL = 60 * 60;
    private static final int LOOKUP_NEG_TTL = 5 * 60;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;

    private Ehcache cacheInstance;

    private int lookupPosTTL = LOOKUP_POS_TTL;
    private int lookupNegTTL = LOOKUP_NEG_TTL;
    private int searchPosTTL = LOOKUP_POS_TTL;
    private int searchNegTTL = LOOKUP_NEG_TTL;


    public String lookupName ( String domain, String sid ) {
        try {
            RealmLookupResult res = lookup(domain, sid);
            if ( res != null ) {
                return res.getDisplayName();
            }
            return null;
        }
        catch ( Exception e ) {
            log.debug("Lookup failed", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param domain
     * @param sid
     * @return name for SID, null if none can be determined
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public RealmLookupResult lookup ( String domain, String sid ) throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {

        Ehcache cache = getCache();
        Serializable key = makeSIDCacheKey(domain, sid);
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (RealmLookupResult) cached.getObjectValue();
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Looking up %s in %s", sid, domain)); //$NON-NLS-1$
            }
            RealmLookupResult res = this.ssp.getService(RealmLookupService.class).getNameForSID(domain, sid);

            if ( res != null && log.isDebugEnabled() ) {
                log.debug("Found result " + res); //$NON-NLS-1$
            }

            int ttl = res != null ? this.lookupPosTTL : this.lookupNegTTL;
            cache.put(new Element(key, res, ttl, ttl));
            return res;
        }

        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null, this.lookupNegTTL, this.lookupNegTTL));
            throw e;
        }
    }


    /**
     * @param domain
     * @param filter
     * @return search results
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @SuppressWarnings ( "unchecked" )
    public List<RealmLookupResult> searchName ( String domain, String filter )
            throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        Ehcache cache = getCache();

        Serializable key = makeSearchCacheKey(domain, filter);
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (List<RealmLookupResult>) cached.getObjectValue();
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Looking up %s in %s", filter, domain)); //$NON-NLS-1$
            }
            List<RealmLookupResult> res = this.ssp.getService(RealmLookupService.class).search(domain, filter);

            if ( log.isDebugEnabled() ) {
                log.debug("Found result " + res); //$NON-NLS-1$
            }

            int ttl = res != null && !res.isEmpty() ? this.searchPosTTL : this.searchNegTTL;
            cache.put(new Element(key, res, ttl, ttl));
            return res;
        }

        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null, this.searchNegTTL, this.searchNegTTL));
            throw e;
        }
    }


    /**
     * @param origin
     * @param name
     * @return results for domain name
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     * @throws ModelObjectNotFoundException
     */
    @SuppressWarnings ( "unchecked" )
    public List<RealmLookupResult> lookupDomainByName ( String origin, String name )
            throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        Ehcache cache = getCache();

        Serializable key = makeDomainNameCacheKey(origin, name);
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (List<RealmLookupResult>) cached.getObjectValue();
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Looking up domain %s in %s", name, origin)); //$NON-NLS-1$
            }
            List<RealmLookupResult> res = this.ssp.getService(RealmLookupService.class).lookupDomainByName(origin, name);

            if ( log.isDebugEnabled() ) {
                log.debug("Found result " + res); //$NON-NLS-1$
            }

            int ttl = res != null && !res.isEmpty() ? this.lookupPosTTL : this.lookupNegTTL;
            cache.put(new Element(key, res, ttl, ttl));
            return res;
        }

        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null, this.lookupNegTTL, this.lookupNegTTL));
            throw e;
        }
    }


    /**
     * @param origin
     * @param sid
     * @return result for domain SID
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     * @throws ModelObjectNotFoundException
     */
    public RealmLookupResult lookupDomain ( String origin, String sid )
            throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        Ehcache cache = getCache();

        Serializable key = makeDomainSIDCacheKey(origin, sid);
        Element cached = cache.get(key);

        if ( cached != null && !cached.isExpired() ) {
            return (RealmLookupResult) cached.getObjectValue();
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Looking up domain %s in %s", sid, origin)); //$NON-NLS-1$
            }
            RealmLookupResult res = this.ssp.getService(RealmLookupService.class).lookupDomainSID(origin, sid);

            if ( log.isDebugEnabled() ) {
                log.debug("Found result " + res); //$NON-NLS-1$
            }

            int ttl = res != null ? this.lookupPosTTL : this.lookupNegTTL;
            cache.put(new Element(key, res, ttl, ttl));
            return res;
        }

        catch ( ModelObjectNotFoundException e ) {
            cache.put(new Element(key, null, this.lookupNegTTL, this.lookupNegTTL));
            throw e;
        }
    }


    /**
     * @param domain
     * @param filter
     * @return
     */
    private static Serializable makeSearchCacheKey ( String domain, String filter ) {
        return String.format("search/%s/%s", domain, filter); //$NON-NLS-1$ ;
    }


    /**
     * @param ref
     * @param rootObjectType
     * @return
     */
    private static Serializable makeSIDCacheKey ( String domain, String sid ) {
        return String.format("sid/%s/%s", domain, sid); //$NON-NLS-1$ ;
    }


    /**
     * @param domain
     * @param name
     * @return
     */
    private static Serializable makeDomainNameCacheKey ( String domain, String name ) {
        return String.format("domname/%s/%s", domain, name); //$NON-NLS-1$ ;
    }


    /**
     * @param domain
     * @param name
     * @return
     */
    private static Serializable makeDomainSIDCacheKey ( String domain, String sid ) {
        return String.format("domsid/%s/%s", domain, sid); //$NON-NLS-1$ ;
    }


    private Ehcache getCache () {
        if ( this.cacheInstance == null ) {
            this.cacheInstance = this.csp.getCacheService().getCache(SID_CACHE);
        }
        return this.cacheInstance;
    }


    public void flush () {
        this.getCache().removeAll();
    }

}

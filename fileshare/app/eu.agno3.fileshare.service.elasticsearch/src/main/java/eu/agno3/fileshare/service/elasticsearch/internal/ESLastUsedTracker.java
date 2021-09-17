/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2017 by mbechler
 */
package eu.agno3.fileshare.service.elasticsearch.internal;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.api.internal.LastUsedTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientProvider;
import eu.agno3.runtime.elasticsearch.ElasticsearchMappingException;
import eu.agno3.runtime.elasticsearch.IndexHandle;
import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingBuilder;
import eu.agno3.runtime.elasticsearch.MappingManager;
import eu.agno3.runtime.elasticsearch.MappingsBuilder;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;


/**
 * @author mbechler
 *
 */
@Component ( service = LastUsedTracker.class )
public class ESLastUsedTracker implements LastUsedTracker {

    private static final Logger log = Logger.getLogger(ESLastUsedTracker.class);
    private static final String FAVORITE_TRACKING_TYPE = "favorite-usage"; //$NON-NLS-1$

    private ClientProvider esClientProvider;
    private MappingManager mappingManager;
    private IndexHandle indexHandle;
    private VFSServiceInternal vfs;


    @Reference
    protected synchronized void setClientProvider ( ClientProvider n ) {
        this.esClientProvider = n;
    }


    protected synchronized void unsetClientProvider ( ClientProvider n ) {
        if ( this.esClientProvider == n ) {
            this.esClientProvider = null;
        }
    }


    @Reference
    protected synchronized void setMappingManager ( MappingManager mm ) {
        this.mappingManager = mm;
    }


    protected synchronized void unsetMappingManager ( MappingManager mm ) {
        if ( this.mappingManager == mm ) {
            this.mappingManager = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext cc ) throws IOException {
        MappingsBuilder mb = this.mappingManager.createBuilder();
        MappingBuilder favmap = mb.forType(FAVORITE_TRACKING_TYPE);
        favmap.field("lastUsed") //$NON-NLS-1$
                .type("long"); //$NON-NLS-1$
        favmap.dynamic("default_fields") //$NON-NLS-1$
                .matchType("string") //$NON-NLS-1$
                .type("keyword").disableNorms(); //$NON-NLS-1$
        Collection<Mapping> mappings = mb.build();
        try {
            this.indexHandle = this.mappingManager.ensureIndexExists("user-favorites", true, mappings); //$NON-NLS-1$
        }
        catch ( ElasticsearchMappingException e ) {
            log.error("Failed to create elasticsearch mapping", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext cc ) {

    }


    /**
     * @return the indexHandle
     */
    IndexHandle getIndexHandle () {
        return this.indexHandle;
    }


    /**
     * @return the vfs
     */
    VFSServiceInternal getVfs () {
        return this.vfs;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.LastUsedTracker#getLastUsedEntities(java.util.UUID)
     */
    @Override
    public Map<EntityKey, DateTime> getLastUsedEntities ( UUID userId ) {
        try ( Client client = this.esClientProvider.client() ) {
            return Detach.runDetached(new DetachedRunnable<Map<EntityKey, DateTime>>() {

                @Override
                public Map<EntityKey, DateTime> run () throws Exception {

                    SearchSourceBuilder b = new SearchSourceBuilder();
                    b.query(QueryBuilders.termQuery("userId", userId)); //$NON-NLS-1$
                    b.sort(SortBuilders.fieldSort("lastUsed").order(SortOrder.DESC).unmappedType("long")); //$NON-NLS-1$ //$NON-NLS-2$
                    b.size(20);
                    b.from(0);
                    SearchRequest srb = new SearchRequest(getIndexHandle().getReadName());
                    srb.source(b);
                    srb.types(FAVORITE_TRACKING_TYPE);

                    SearchResponse resp = client.search(srb);

                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug(String.format("Found %d usage trackers", resp.getHits().getHits().length)); //$NON-NLS-1$
                    }

                    Map<EntityKey, DateTime> lastUsedByEntity = new HashMap<>();
                    for ( SearchHit h : resp.getHits().getHits() ) {
                        String entityIdStr = (String) h.getSourceAsMap().get("entityId"); //$NON-NLS-1$
                        Long lastUsedLong = (Long) h.getSourceAsMap().get("lastUsed"); //$NON-NLS-1$

                        if ( entityIdStr == null || lastUsedLong == null ) {
                            continue;
                        }

                        DateTime lastUsed = new DateTime(lastUsedLong);
                        EntityKey parsedKey = getVfs().parseEntityKey(entityIdStr);
                        lastUsedByEntity.put(parsedKey, lastUsed);
                    }

                    return lastUsedByEntity;
                }
            });
        }
        catch ( Exception e ) {
            log.debug("Failed to get favorite last usage", e); //$NON-NLS-1$
            return Collections.EMPTY_MAP;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.LastUsedTracker#trackUsage(java.util.UUID,
     *      eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public void trackUsage ( UUID userId, EntityKey entity ) {
        try ( Client client = this.esClientProvider.client() ) {
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    String id = userId.toString() + entity; // $NON-NLS-1$
                    long ts = System.currentTimeMillis();
                    Map<String, Object> upsert = new HashMap<>();
                    upsert.put("userId", userId); //$NON-NLS-1$
                    upsert.put("entityId", entity); //$NON-NLS-1$
                    upsert.put("lastUsed", ts); //$NON-NLS-1$

                    Map<String, Object> update = new HashMap<>();
                    update.put("lastUsed", ts); //$NON-NLS-1$

                    UpdateRequest upd = new UpdateRequest(getIndexHandle().getWriteName(), FAVORITE_TRACKING_TYPE, id);
                    upd.upsert(upsert);
                    upd.doc(update);

                    client.update(upd);
                    return null;
                }
            });
        }
        catch ( Exception e ) {
            log.debug("Could not store favorite tracker", e); //$NON-NLS-1$
        }
    }
}

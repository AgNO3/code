/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.internal;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;
import eu.agno3.runtime.elasticsearch.ClientProvider;
import eu.agno3.runtime.elasticsearch.ElasticsearchMappingException;
import eu.agno3.runtime.elasticsearch.IndexHandle;
import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingBuilder;
import eu.agno3.runtime.elasticsearch.MappingManager;
import eu.agno3.runtime.elasticsearch.MappingsBuilder;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLoggerBackend;
import eu.agno3.runtime.eventlog.EventLoggerException;
import eu.agno3.runtime.eventlog.elasticsearch.index.ElasticsearchIndexSelection;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;


/**
 * @author mbechler
 *
 */
@Component (
    service = EventLoggerBackend.class,
    configurationPid = ElasticsearchLoggerBackendImpl.INTERNAL_PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ElasticsearchLoggerBackendImpl implements EventLoggerBackend, ActionListener<IndexResponse> {

    private static final Logger log = Logger.getLogger(ElasticsearchLoggerBackendImpl.class);

    /**
     * 
     */
    public static final String INTERNAL_PID = "event.es.backend"; //$NON-NLS-1$

    private ClientProvider clientProvider;

    private Object indexCreationLock = new Object();
    private IndexHandle lastCreated;
    private String lastCreatedName;

    private int maxParallel = 100;
    private Semaphore concurrency = new Semaphore(this.maxParallel);

    MappingManager mappingManager;

    Collection<Mapping> mapping;

    ElasticsearchLoggerConfig config = new ElasticsearchLoggerConfig();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( !this.clientProvider.allowsAdminOperations() ) {
            return;
        }
        try ( Client cl = this.clientProvider.client() ) {
            this.mapping = createMapping().build();
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    ElasticsearchLoggerBackendImpl.this.mappingManager
                            .migrateMappings(cl, getManagedIndices(cl), ElasticsearchLoggerBackendImpl.this.mapping);
                    return null;
                }
            });
        }
        catch ( Exception e ) {
            log.error("Failed to run index migration", e); //$NON-NLS-1$
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


    /**
     * @param cl
     * @return
     * @throws ClientException
     */
    Set<String> getManagedIndices ( Client cl ) throws ClientException {
        MetaData meta = cl.admin().cluster().state(new ClusterStateRequest()).getState().getMetaData();
        String[] concreteAllOpenIndices = meta.getConcreteAllOpenIndices();
        Set<String> filteredIndices = new HashSet<>();
        for ( String index : concreteAllOpenIndices ) {
            if ( !index.startsWith(ElasticsearchLoggerBackendImpl.this.config.getIndexName()) ) {
                continue;
            }
            filteredIndices.add(index);
        }
        return filteredIndices;
    }


    /**
     * @return
     * @throws IOException
     */

    @SuppressWarnings ( "nls" )
    private MappingsBuilder createMapping () throws IOException {
        MappingsBuilder mbs = this.mappingManager.createBuilder();
        MappingBuilder mb = mbs.defaults();
        mb.dynamic("string_fields").matchType("string").type("keyword").disableNorms();
        mb.field("message").type("text");
        mb.field("timestamp").type("long");
        mb.field("expiration").type("date");
        mb.field("type").type("keyword").disableNorms();
        mb.field("severity").type("keyword").disableNorms();
        mb.field("dedup_key").type("keyword").disableNorms();
        return mbs;
    }


    /**
     * @param n
     */
    @Reference
    public synchronized void setClientProvider ( ClientProvider n ) {
        this.clientProvider = n;
    }


    /**
     * 
     * @param n
     */
    public synchronized void unsetClientProvider ( ClientProvider n ) {
        if ( this.clientProvider == n ) {
            this.clientProvider = null;
        }
    }


    /**
     * @param lc
     */
    @Reference
    public synchronized void setLoggerConfig ( ElasticsearchLoggerConfig lc ) {
        this.config = lc;
    }


    /**
     * @param lc
     */
    public synchronized void unsetLoggerConfig ( ElasticsearchLoggerConfig lc ) {
        if ( this.config == lc ) {
            this.config = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getPriority()
     */
    @Override
    public int getPriority () {
        return 100;
    }


    void acquireSemaphore () throws InterruptedException {
        this.concurrency.acquire();
    }


    void releaseSemaphore () {
        this.concurrency.release();
    }


    @Override
    public synchronized void reset () {
        this.lastCreatedName = null;
        this.lastCreated = null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#log(eu.agno3.runtime.eventlog.Event, byte[])
     */
    @Override
    public Future<IndexResponse> log ( Event ev, byte[] bytes ) {

        if ( this.config.isIgnorePostdated() && this.config.getRetainDays() > 0
                && ev.getTimestamp().isBefore(DateTime.now().minusDays(this.config.getRetainDays())) ) {
            log.warn("Tried to log an event that is older than retention time"); //$NON-NLS-1$
            return null;
        }

        try ( Client cl = this.clientProvider.client() ) {

            ActionListener<IndexResponse> listener = this;

            return Detach.runDetached(new DetachedRunnable<Future<IndexResponse>>() {

                @Override
                public Future<IndexResponse> run () throws Exception {
                    IndexHandle idx = ensureIndex(ev.getTimestamp());

                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug(String.format("Logging event of type %s to index %s", ev.getType(), idx.getWriteName())); //$NON-NLS-1$
                    }

                    // used to be setCreate, but that does not autogenerate id anymore
                    // used to set timestamp/ttl

                    IndexRequest index = new IndexRequest(idx.getWriteName(), ev.getType()).source(bytes, XContentType.JSON);

                    CompletableFuture<IndexResponse> future = new CompletableFuture<>();
                    acquireSemaphore();
                    cl.indexAsync(index, new ActionListener<IndexResponse>() {

                        @Override
                        public void onFailure ( Exception ex ) {
                            listener.onFailure(ex);
                            future.completeExceptionally(ex);
                        }


                        @Override
                        public void onResponse ( IndexResponse resp ) {
                            listener.onResponse(resp);
                            future.complete(resp);
                        }

                    });
                    return future;
                }

            });

        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to store event", e); //$NON-NLS-1$
        }
    }


    /**
     * @param eventTime
     * @return
     * @throws ElasticsearchMappingException
     */
    protected IndexHandle ensureIndex ( DateTime eventTime ) throws ElasticsearchMappingException {
        String idxName = ElasticsearchIndexSelection.makeIndex(this.config.getIndexType(), this.config.getIndexName(), eventTime);

        if ( this.lastCreatedName == null || !this.lastCreatedName.equals(idxName) ) {
            synchronized ( this.indexCreationLock ) {
                if ( this.lastCreatedName == null || !this.lastCreatedName.equals(idxName) ) {
                    this.lastCreated = this.mappingManager.ensureIndexExists(idxName, false, this.mapping);
                    this.lastCreatedName = idxName;
                }
            }
        }
        return this.lastCreated;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#bulkLog(java.util.List, java.util.Map)
     */
    @Override
    public Future<?> bulkLog ( List<Event> evs, Map<Event, byte[]> data ) {

        try ( Client cl = this.clientProvider.client() ) {
            Map<Event, IndexHandle> idcs = new HashMap<>();
            for ( Event ev : evs ) {
                idcs.put(ev, ensureIndex(ev.getTimestamp()));
            }

            return Detach.runDetached(new DetachedRunnable<Future<BulkResponse>>() {

                @Override
                public Future<BulkResponse> run () throws Exception {

                    BulkRequest bulk = new BulkRequest();

                    for ( Event ev : evs ) {
                        // used to be setCreate, but that does not autogenerate ids anymore
                        // used to set timestamp/ttl
                        bulk.add(new IndexRequest(idcs.get(ev).getWriteName(), ev.getType()).source(data.get(ev), XContentType.JSON));
                    }

                    CompletableFuture<BulkResponse> future = new CompletableFuture<>();
                    acquireSemaphore();
                    cl.bulkAsync(bulk, new ActionListener<BulkResponse>() {

                        @Override
                        public void onResponse ( BulkResponse resp ) {
                            releaseSemaphore();
                            future.complete(resp);
                        }


                        @Override
                        public void onFailure ( Exception t ) {
                            releaseSemaphore();
                            getLog().error("Failed to write bulk event log", t); //$NON-NLS-1$
                            future.completeExceptionally(t);
                        }
                    });
                    return future;
                }

            });

        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to store event", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    @Override
    public void runMaintenance () {
        if ( !this.clientProvider.allowsAdminOperations() ) {
            return;
        }
        getLog().debug("Running maintenance"); //$NON-NLS-1$
        try ( Client cl = this.clientProvider.client() ) {
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    ClusterStateResponse clState = cl.admin().cluster().state(new ClusterStateRequest());
                    MetaData metaData = clState.getState().getMetaData();
                    if ( ElasticsearchLoggerBackendImpl.this.config.getRetainDays() > 0 ) {
                        deleteExpiredIndices(cl, metaData.getConcreteAllIndices());
                    }

                    clState = cl.admin().cluster().state(new ClusterStateRequest());
                    metaData = clState.getState().getMetaData();
                    String[] concreteAllOpenIndices = metaData.getConcreteAllOpenIndices();
                    if ( ElasticsearchLoggerBackendImpl.this.config.getRetainOpenDays() > 0 ) {
                        closeOldIndices(cl, concreteAllOpenIndices);
                    }

                    clState = cl.admin().cluster().state(new ClusterStateRequest());
                    metaData = clState.getState().getMetaData();
                    optimizeIndices(cl, concreteAllOpenIndices);

                    ClusterHealthResponse res = cl.admin().cluster()
                            .health(new ClusterHealthRequest().waitForYellowStatus().timeout(new TimeValue(10000)));
                    if ( res.isTimedOut() ) {
                        getLog().warn("Timeout waiting for index to be recovered: " + res.getStatus()); //$NON-NLS-1$
                    }
                    return null;
                }
            });
        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to run maintenance", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cl
     * @param indices
     * @throws ClientException
     */
    void optimizeIndices ( Client cl, String[] indices ) throws ClientException {
        DateTime optimizeBefore;
        DateTime optimizeAfter;

        switch ( this.config.getIndexType() ) {
        default:
        case DAILY:
            optimizeBefore = DateTime.now().withMillisOfDay(0).minusDays(1);
            optimizeAfter = DateTime.now().withMillisOfDay(0).minusDays(7);
            break;
        case MONTHLY:
            optimizeBefore = DateTime.now().withMillisOfDay(0).minusMonths(1);
            optimizeAfter = DateTime.now().withMillisOfDay(0).minusMonths(3);
            break;
        case WEEKLY:
            optimizeBefore = DateTime.now().withMillisOfDay(0).minusWeeks(1);
            optimizeAfter = DateTime.now().withMillisOfDay(0).minusWeeks(7);
            break;
        case YEARLY:
            optimizeBefore = DateTime.now().withMillisOfDay(0).minusYears(1);
            optimizeAfter = DateTime.now().withMillisOfDay(0).minusYears(3);
            break;
        }

        List<String> mostlyRead = new LinkedList<>();
        for ( String index : indices ) {
            if ( !index.startsWith(this.config.getIndexName()) ) {
                continue;
            }
            DateTime indexDate = getIndexDate(index);

            if ( indexDate.isBefore(optimizeBefore) && indexDate.isAfter(optimizeAfter) ) {
                mostlyRead.add(index);
            }
        }

        if ( mostlyRead.isEmpty() ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Optimizing indices " + mostlyRead); //$NON-NLS-1$
        }

        String[] idxArray = mostlyRead.toArray(new String[] {});
        // do this one by one ... this costs some resources
        for ( String idx : this.mappingManager.expandIndicesBacking(cl, idxArray) ) {
            try {
                IndicesStatsResponse stats = cl.admin().indices().stats(new IndicesStatsRequest().indices(idx));
                // TODO: add another metric?
                long deleted = stats.getTotal().docs.getDeleted();
                if ( log.isDebugEnabled() ) {
                    log.debug("Deleted docs is is " + deleted); //$NON-NLS-1$
                }
                if ( deleted >= 1 ) {
                    log.info("Optimizing index " + idx); //$NON-NLS-1$
                    cl.admin().indices().forceMerge(new ForceMergeRequest(idx).maxNumSegments(1));
                }
            }
            catch ( ElasticsearchException e ) {
                log.warn("Failed to optimize index " + idx, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param cl
     * @param indices
     * @return
     * @throws ClientException
     */
    Set<String> closeOldIndices ( Client cl, String[] openIndices ) throws ClientException {
        if ( log.isDebugEnabled() ) {
            log.debug("Open indices " + Arrays.asList(openIndices)); //$NON-NLS-1$
        }
        Set<String> old = new HashSet<>();
        DateTime threshold = DateTime.now().withMillisOfDay(0).minusDays(this.config.getRetainOpenDays());
        if ( log.isDebugEnabled() ) {
            log.debug("Closing indices before " + threshold); //$NON-NLS-1$
        }
        for ( String openIndex : openIndices ) {
            if ( !openIndex.startsWith(this.config.getIndexName()) ) {
                continue;
            }

            DateTime indexDate = getIndexDate(openIndex);
            if ( indexDate.isBefore(threshold) ) {
                old.add(openIndex);
            }
        }

        if ( old.isEmpty() ) {
            return old;
        }

        log.info("Closing old indices " + old); //$NON-NLS-1$

        cl.admin().indices().flush(new FlushRequest(old.toArray(new String[] {})));
        cl.admin().indices().close(new CloseIndexRequest(old.toArray(new String[] {})));

        return old;
    }


    /**
     * @param openIndex
     * @return
     */
    private DateTime getIndexDate ( String openIndex ) {
        String date = openIndex.substring(this.config.getIndexName().length());
        int sep = date.indexOf('@');
        if ( sep >= 0 ) {
            date = date.substring(0, sep);
        }
        return ElasticsearchIndexSelection.getDateFormat(this.config.getIndexType()).parseDateTime(date);
    }


    /**
     * @param cl
     * @param indices
     * @throws ClientException
     */
    Set<String> deleteExpiredIndices ( Client cl, String[] indices ) throws ClientException {
        Set<String> expired = new HashSet<>();

        for ( String index : indices ) {
            if ( !index.startsWith(this.config.getIndexName()) ) {
                continue;
            }

            DateTime indexDate = getIndexDate(index);
            if ( indexDate.isBefore(DateTime.now().withMillisOfDay(0).minusDays(this.config.getRetainDays())) ) {
                expired.add(index);
            }
        }

        if ( expired.isEmpty() ) {
            return expired;
        }

        log.info("Removing expired indices " + expired); //$NON-NLS-1$
        this.mappingManager.removeIndices(cl, expired, IndicesOptions.fromOptions(true, true, true, true));
        return expired;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.elasticsearch.action.ActionListener#onFailure(java.lang.Exception)
     */
    @Override
    public void onFailure ( Exception t ) {
        releaseSemaphore();
        log.error("Failed to write event log entry", t); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.elasticsearch.action.ActionListener#onResponse(java.lang.Object)
     */
    @Override
    public void onResponse ( IndexResponse resp ) {
        releaseSemaphore();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getExcludeStreams()
     */
    @Override
    public Set<String> getExcludeStreams () {
        return this.config.getExcludeStreams();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getIncludeStreams()
     */
    @Override
    public Set<String> getIncludeStreams () {
        return this.config.getIncludeStreams();
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.internal;


import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;
import eu.agno3.runtime.elasticsearch.ClientProvider;
import eu.agno3.runtime.elasticsearch.MappingManager;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLoggerException;
import eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader;
import eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder;
import eu.agno3.runtime.eventlog.elasticsearch.QueryParams;
import eu.agno3.runtime.eventlog.elasticsearch.index.ElasticsearchIndexSelection;
import eu.agno3.runtime.eventlog.impl.MapEvent;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;


/**
 * @author mbechler
 *
 */
@Component (
    service = ElasticsearchLogReader.class,
    configurationPid = ElasticsearchLogReaderImpl.INTERNAL_PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ElasticsearchLogReaderImpl implements ElasticsearchLogReader {

    private static final Logger log = Logger.getLogger(ElasticsearchLogReaderImpl.class);

    /**
     * 
     */
    public static final String INTERNAL_PID = "event.es.reader"; //$NON-NLS-1$

    private static final String DEDUP_STATS_AGG = "stats"; //$NON-NLS-1$
    private static final String MOST_RECENT_EVENT_AGG = "most_recent"; //$NON-NLS-1$
    private static final String DEDUP_AGG = "deduplicated"; //$NON-NLS-1$
    private static final String ACKNOWLEGED = "acknowleged"; //$NON-NLS-1$
    private static final int PATTERN_LIMIT = 20;

    private ClientProvider esClientProvider;
    MappingManager mappingManager;

    ElasticsearchLoggerConfig config = new ElasticsearchLoggerConfig();


    @Reference
    protected synchronized void setClientProvider ( ClientProvider cp ) {
        this.esClientProvider = cp;
    }


    protected synchronized void unsetClientProvider ( ClientProvider cp ) {
        if ( this.esClientProvider == cp ) {
            this.esClientProvider = null;
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader#getOpenRetentionDays()
     */
    @Override
    public int getOpenRetentionDays () {
        return this.config.getRetainOpenDays();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader#getRetentionDays()
     */
    @Override
    public int getRetentionDays () {
        return this.config.getRetainDays();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader#searchEvents(eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder)
     */
    @Override
    public SearchResponse searchEvents ( QueryParamBuilder params ) {
        return searchEvents(params.build());
    }


    private SearchResponse searchEvents ( QueryParams params ) {
        try ( Client client = this.esClientProvider.client() ) {
            return Detach.runDetached(new DetachedRunnable<SearchResponse>() {

                @Override
                public SearchResponse run () throws Exception {
                    QueryBuilder qb = params.getCustomQuery();
                    if ( qb == null ) {
                        qb = QueryBuilders.matchAllQuery();
                    }

                    qb = setupBasic(params, qb);

                    SearchRequest search = new SearchRequest();
                    search.indicesOptions(IndicesOptions.fromOptions(true, true, true, false));

                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

                    if ( params.getTypes() != null ) {
                        search.types(params.getTypes());
                    }

                    qb = setupDateRange(client, params, qb, search);
                    setupSorting(params, sourceBuilder);
                    setupAggregations(params, sourceBuilder);

                    if ( params.getPageSize() != null ) {
                        sourceBuilder.size(params.getPageSize());
                    }

                    if ( params.getPageFrom() != null ) {
                        sourceBuilder.from(params.getPageFrom());
                    }

                    sourceBuilder.query(qb);
                    search.source(sourceBuilder);

                    return client.search(search);
                }

            });

        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to find events", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader#countEvents(eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder)
     */
    @Override
    public long countEvents ( QueryParamBuilder params ) {
        return countEvents(params.build());
    }


    /**
     * @param params
     * @return
     */
    private long countEvents ( QueryParams params ) {
        try ( Client client = this.esClientProvider.client() ) {
            return Detach.runDetached(new DetachedRunnable<Long>() {

                @Override
                public Long run () throws Exception {

                    QueryBuilder qb = params.getCustomQuery();
                    if ( qb == null ) {
                        qb = QueryBuilders.matchAllQuery();
                    }

                    qb = setupBasic(params, qb);

                    SearchRequest search = new SearchRequest();
                    search.indicesOptions(IndicesOptions.fromOptions(true, true, true, false));

                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                    sourceBuilder.size(0);

                    if ( params.getTypes() != null ) {
                        search.types(params.getTypes());
                    }

                    if ( params.getStartDate() != null ) {
                        qb = makeDateRangeFilter(params, qb);
                        String[] indices = makeIndices(params);
                        ensureIndicesOpen(client, params, indices);
                        search.indices(ElasticsearchLogReaderImpl.this.mappingManager.expandIndicesRead(client, indices));
                        search.indicesOptions(IndicesOptions.fromOptions(true, true, true, true));
                    }
                    else {
                        search.indices(
                            ElasticsearchLogReaderImpl.this.mappingManager
                                    .toReadPattern(client, ElasticsearchLogReaderImpl.this.config.getIndexName() + "*")); //$NON-NLS-1$
                    }

                    sourceBuilder.query(qb);
                    search.source(sourceBuilder);
                    return client.search(search).getHits().getTotalHits();
                }
            });
        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to count events", e); //$NON-NLS-1$
        }
    }


    /**
     * @param params
     * @param search
     */
    protected void setupAggregations ( QueryParams params, SearchSourceBuilder search ) {
        if ( params.getCustomAggregations() != null ) {
            for ( AbstractAggregationBuilder<?> ab : params.getCustomAggregations() ) {
                search.aggregation(ab);
            }
        }

        if ( !params.isIncludeDuplicates() ) {
            AggregationBuilder aggregation = AggregationBuilders.terms(DEDUP_AGG).field(MapEvent.DEDUP_KEY)
                    .subAggregation(AggregationBuilders.topHits(MOST_RECENT_EVENT_AGG).sort(MapEvent.TIMESTAMP, SortOrder.DESC).size(1))
                    .subAggregation(AggregationBuilders.stats(DEDUP_STATS_AGG).field(MapEvent.TIMESTAMP));
            search.aggregation(aggregation);
        }
    }


    /**
     * @param params
     * @param search
     */
    protected void setupSorting ( QueryParams params, SearchSourceBuilder search ) {
        if ( params.getSorting() != null ) {
            for ( SortBuilder<?> sb : params.getSorting() ) {
                search.sort(sb);
            }
        }
        else {
            search.sort(SortBuilders.fieldSort(MapEvent.TIMESTAMP).unmappedType("long").order(SortOrder.DESC)); //$NON-NLS-1$
        }

    }


    /**
     * @param params
     * @param qb
     * @return
     */
    protected QueryBuilder setupBasic ( QueryParams params, QueryBuilder o ) {
        QueryBuilder qb = o;
        if ( !params.isIncludeExpired() ) {
            qb = QueryBuilders.boolQuery().must(qb).should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(MapEvent.EXPIRE)))
                    .minimumShouldMatch(1).should(QueryBuilders.rangeQuery(MapEvent.EXPIRE).gte(System.currentTimeMillis()));
        }

        if ( !params.isIncludeAcknowledged() ) {
            qb = QueryBuilders.boolQuery().must(qb).must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(ACKNOWLEGED)));
        }
        return qb;
    }


    /**
     * @param client
     * @param params
     * @param qb
     * @param search
     * @return
     * @throws ClientException
     */
    protected QueryBuilder setupDateRange ( Client client, QueryParams params, QueryBuilder o, SearchRequest search ) throws ClientException {
        QueryBuilder qb = o;
        String[] indices;
        if ( params.getStartDate() != null ) {
            qb = makeDateRangeFilter(params, qb);
            indices = makeIndices(params);
            ensureIndicesOpen(client, params, indices);
            String[] expanded = this.mappingManager.expandIndicesRead(client, indices);
            if ( log.isDebugEnabled() ) {
                log.debug("Searching using indices " + Arrays.toString(expanded)); //$NON-NLS-1$
            }
            search.indices(expanded);
            search.indicesOptions(IndicesOptions.fromOptions(true, true, true, true));

        }
        else {
            String readPattern = this.mappingManager.toReadPattern(client, this.config.getIndexName() + "*"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Searching using pattern " + readPattern); //$NON-NLS-1$
            }
            search.indices(readPattern);
        }
        return qb;
    }


    /**
     * @param params
     * @return
     */
    String[] makeIndices ( QueryParams params ) {
        return ElasticsearchIndexSelection.getIndexPatternsForRange(
            this.config.getIndexType(),
            this.config.getIndexName(),
            PATTERN_LIMIT,
            params.getStartDate(),
            params.getEndDate());
    }


    /**
     * @param params
     * @param qb
     * @return
     */
    static QueryBuilder makeDateRangeFilter ( QueryParams params, QueryBuilder qb ) {
        RangeQueryBuilder rangeFilter = QueryBuilders.rangeQuery(MapEvent.TIMESTAMP).gte(params.getStartDate().getMillis());
        if ( params.getEndDate() != null ) {
            rangeFilter.lte(params.getEndDate().getMillis());
        }

        return QueryBuilders.boolQuery().must(qb).must(rangeFilter);
    }


    /**
     * @param client
     * @param params
     * @param indices
     * @throws ClientException
     */
    void ensureIndicesOpen ( Client client, QueryParams params, String[] indices ) throws ClientException {
        if ( this.config.getRetainOpenDays() > 0 && params.getStartDate().isBefore(DateTime.now().minusDays(this.config.getRetainOpenDays())) ) {
            long start = System.currentTimeMillis();
            ClusterStateResponse clusterStateResponse = client.admin().cluster().state(new ClusterStateRequest());
            String[] closedIndices = clusterStateResponse.getState().getMetaData().getConcreteAllClosedIndices();
            if ( closedIndices == null ) {
                return;
            }
            HashSet<String> closed = new HashSet<>(Arrays.asList(closedIndices));
            // closedIndices = new String[closed.size()];

            if ( closed.size() > 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Need to open indices " + closed); //$NON-NLS-1$
                }

                client.admin().indices().open(new OpenIndexRequest(closedIndices));
                ClusterHealthResponse res = client.admin().cluster()
                        .health(new ClusterHealthRequest(closedIndices).waitForYellowStatus().timeout(new TimeValue(10000)));

                if ( res.isTimedOut() ) {
                    log.warn("Timeout waiting for index to be recovered"); //$NON-NLS-1$
                }

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Opening indices took %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
                }
            }
        }
    }


    @Override
    public List<MapEvent> findAllEvents ( QueryParamBuilder pb ) {
        return findAllEvents(pb.build());
    }


    @Override
    public List<MapEvent> findDeduplicated ( QueryParamBuilder pb ) {
        return findDeduplicated(pb.filterDuplicates().build());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader#findDeduplicatedRaw(eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder)
     */
    @Override
    public List<String> findDeduplicatedRaw ( QueryParamBuilder pb ) {
        return findDeduplicatedRaw(pb.build());
    }


    /**
     * @param build
     */
    private List<MapEvent> findDeduplicated ( QueryParams params ) {
        SearchResponse resp = this.searchEvents(params);

        List<MapEvent> events = new LinkedList<>();
        Aggregations aggregations = resp.getAggregations();
        StringTerms terms = aggregations.get(DEDUP_AGG);

        for ( SearchHit hit : resp.getHits() ) {
            if ( hit.getSourceAsMap().get(MapEvent.DEDUP_KEY) == null ) {
                events.add(makeEvent(hit.getId(), hit.getSourceAsMap()));
                continue;
            }

            String dedupKey = (String) hit.getSourceAsMap().get(MapEvent.DEDUP_KEY);
            Bucket b = terms.getBucketByKey(dedupKey);

            TopHits h = b.getAggregations().get(MOST_RECENT_EVENT_AGG);
            Stats st = b.getAggregations().get(DEDUP_STATS_AGG);

            if ( !hit.getId().equals(h.getHits().getAt(0).getId()) ) {
                continue;
            }

            MapEvent ev = makeEvent(hit.getId(), hit.getSourceAsMap());
            ev.put(MapEvent.FIRST_SEEN, Double.valueOf(st.getMin()).longValue());
            ev.put(MapEvent.LAST_SEEN, Double.valueOf(st.getMax()).longValue());
            ev.put(MapEvent.COUNT_SEEN, st.getCount());
            events.add(ev);
        }
        return events;
    }


    /**
     * @param build
     */
    private List<String> findDeduplicatedRaw ( QueryParams params ) {
        SearchResponse resp = this.searchEvents(params);

        List<String> events = new LinkedList<>();
        StringTerms terms = resp.getAggregations().get(DEDUP_AGG);

        for ( SearchHit hit : resp.getHits() ) {
            if ( hit.getSourceAsMap().get(MapEvent.DEDUP_KEY) == null ) {
                events.add(hit.getSourceAsString());
                continue;
            }

            String dedupKey = (String) hit.getSourceAsMap().get(MapEvent.DEDUP_KEY);
            Bucket b = terms.getBucketByKey(dedupKey);

            TopHits h = b.getAggregations().get(MOST_RECENT_EVENT_AGG);
            if ( !hit.getId().equals(h.getHits().getAt(0).getId()) ) {
                continue;
            }

            events.add(hit.getSourceAsString());
        }
        return events;
    }


    private List<MapEvent> findAllEvents ( QueryParams params ) {
        SearchResponse resp = this.searchEvents(params);
        List<MapEvent> events = new LinkedList<>();
        for ( SearchHit hit : resp.getHits() ) {
            events.add(makeEvent(hit.getId(), hit.getSourceAsMap()));
        }
        return events;
    }


    @Override
    public void acknowledge ( Event ev ) {
        checkEvent(ev);

        try ( Client cl = this.esClientProvider.client() ) {
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    String idx = ElasticsearchIndexSelection.makeIndex(
                        ElasticsearchLogReaderImpl.this.config.getIndexType(),
                        ElasticsearchLogReaderImpl.this.config.getIndexName(),
                        ev.getTimestamp());

                    UpdateRequest upd = new UpdateRequest(
                        ElasticsearchLogReaderImpl.this.mappingManager.expandIndexWrite(cl, idx),
                        ev.getType(),
                        ev.getId());
                    upd.fetchSource(ACKNOWLEGED, StringUtils.EMPTY);
                    upd.doc(ACKNOWLEGED, true);
                    upd.setRefreshPolicy(RefreshPolicy.IMMEDIATE);

                    cl.update(upd);
                    return null;
                }
            });

        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to acknowledge event", e); //$NON-NLS-1$
        }
    }


    @Override
    public void delete ( Event ev ) {
        checkEvent(ev);
        try ( Client cl = this.esClientProvider.client() ) {
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    String idx = ElasticsearchIndexSelection.makeIndex(
                        ElasticsearchLogReaderImpl.this.config.getIndexType(),
                        ElasticsearchLogReaderImpl.this.config.getIndexName(),
                        ev.getTimestamp());

                    DeleteRequest del = new DeleteRequest(
                        ElasticsearchLogReaderImpl.this.mappingManager.expandIndexWrite(cl, idx),
                        ev.getType(),
                        ev.getId()).setRefreshPolicy(RefreshPolicy.IMMEDIATE);

                    cl.delete(del);
                    return null;
                }
            });
        }
        catch ( Exception e ) {
            throw new EventLoggerException("Failed to delete event", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ev
     */
    private static void checkEvent ( Event ev ) {
        if ( ev.getId() == null || ev.getType() == null || ev.getTimestamp() == null ) {
            throw new IllegalArgumentException("No ID known for the event"); //$NON-NLS-1$
        }
    }


    private static MapEvent makeEvent ( String id, Map<String, Object> source ) {
        MapEvent ev = MapEvent.fromMap(source);
        ev.put(MapEvent.ID, id);
        return ev;
    }
}

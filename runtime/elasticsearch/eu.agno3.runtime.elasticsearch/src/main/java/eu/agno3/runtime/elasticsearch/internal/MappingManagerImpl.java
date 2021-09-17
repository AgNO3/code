/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 6, 2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;
import eu.agno3.runtime.elasticsearch.ClientProvider;
import eu.agno3.runtime.elasticsearch.ElasticsearchMappingException;
import eu.agno3.runtime.elasticsearch.IndexHandle;
import eu.agno3.runtime.elasticsearch.IndexSettings;
import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingComparator;
import eu.agno3.runtime.elasticsearch.MappingManager;
import eu.agno3.runtime.elasticsearch.MappingMigrationStatus;
import eu.agno3.runtime.elasticsearch.MappingStatus;
import eu.agno3.runtime.elasticsearch.MappingsBuilder;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;


/**
 * @author mbechler
 *
 */
@Component ( service = MappingManager.class )
public class MappingManagerImpl implements MappingManager {

    private static final Logger log = Logger.getLogger(MappingManagerImpl.class);

    private static final String INDEX_REFRESH_INTERVAL = "index.refresh_interval"; //$NON-NLS-1$
    private static final String INDEX_NUMBER_OF_REPLICAS = "index.number_of_replicas"; //$NON-NLS-1$
    private static final long SHUTDOWN_TIMEOUT = 30;

    private static Settings DEFAULT_INDEX_SETTING;
    private static Settings DEFAULT_INDEX_CREATION_SETTINGS;
    private ClientProvider esClientProvider;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    MappingComparator comparator;
    Map<String, IndexHandle> cache = new ConcurrentHashMap<>();

    static {
        DEFAULT_INDEX_SETTING = Settings.builder().put(INDEX_REFRESH_INTERVAL, new TimeValue(1, TimeUnit.SECONDS)).put(INDEX_NUMBER_OF_REPLICAS, 0)
                .build();
        DEFAULT_INDEX_CREATION_SETTINGS = Settings.builder().put(INDEX_REFRESH_INTERVAL, new TimeValue(1, TimeUnit.SECONDS))
                .put(INDEX_NUMBER_OF_REPLICAS, 0).build();
    }


    /**
     * 
     */
    public MappingManagerImpl () {}


    /**
     * 
     * @param cp
     */
    public MappingManagerImpl ( ClientProvider cp ) {
        this.esClientProvider = cp;
        this.comparator = new MappingComparatorImpl();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {}


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        try {
            log.debug("Shutting down executor"); //$NON-NLS-1$
            List<Runnable> remain = this.executor.shutdownNow();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Waiting for %d running tasks", remain.size())); //$NON-NLS-1$
            }
            if ( !this.executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS) ) {
                log.error("Timeout waiting for reindexing job to complete"); //$NON-NLS-1$
            }
        }
        catch ( InterruptedException e ) {
            log.error("Interrupted while waiting for reindexing to complete", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setClientProvider ( ClientProvider cp ) {
        this.esClientProvider = cp;
    }


    protected synchronized void unsetClientProvider ( ClientProvider cp ) {
        if ( this.esClientProvider == cp ) {
            this.esClientProvider = null;
        }
    }


    @Reference
    protected synchronized void setMappingComparator ( MappingComparator mc ) {
        this.comparator = mc;
    }


    protected synchronized void unsetMappingComparator ( MappingComparator mc ) {
        if ( this.comparator == mc ) {
            this.comparator = null;
        }
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
     * @see eu.agno3.runtime.elasticsearch.MappingManager#createBuilder()
     */
    @Override
    public MappingsBuilder createBuilder () {
        return new MappingsBuilderImpl();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.MappingManager#clearCache()
     */
    @Override
    public void clearCache () {
        this.cache.clear();
    }


    @Override
    public IndexHandle ensureIndexExists ( String name, boolean checkMapping, Collection<Mapping> mappings ) throws ElasticsearchMappingException {
        return this.ensureIndexExists(name, checkMapping, mappings, null);
    }


    @Override
    public IndexHandle ensureIndexExists ( String name, boolean checkMapping, Collection<Mapping> mappings, IndexSettings indexSettings )
            throws ElasticsearchMappingException {

        final String primary = getPrimaryAliasName(name);
        IndexHandle cached = this.cache.get(primary);
        if ( cached != null ) {
            return cached;
        }

        if ( !this.esClientProvider.allowsAdminOperations() ) {
            return new IndexHandleImpl(name, getReadAlias(name), getWriteAlias(name), name);
        }

        try ( Client cl = this.esClientProvider.client() ) {
            return Detach.runDetached(new DetachedRunnable<IndexHandle>() {

                @Override
                public IndexHandle run () throws Exception {
                    IndexHandle actualName = ensureAliasing(name, cl, true);
                    if ( actualName.getBacking() != null ) {
                        if ( getLog().isDebugEnabled() ) {
                            getLog().debug("Index already existed " + actualName); //$NON-NLS-1$
                        }
                        if ( checkMapping ) {
                            migrateMappings(cl, Collections.singleton(name), mappings, indexSettings);
                        }
                        MappingManagerImpl.this.cache.put(name, actualName);
                        return actualName;
                    }

                    // we must create an alias to each index and use that
                    // to allow for seamless migration
                    String actualIndexName = makeNewIndexName(primary, 1);

                    if ( !cl.admin().indices().exists(new IndicesExistsRequest(actualIndexName)).isExists() ) {
                        createIndex(cl, actualIndexName, mappings, indexSettings);
                    }

                    cl.admin().indices().aliases(
                        new IndicesAliasesRequest()
                                .addAliasAction(IndicesAliasesRequest.AliasActions.add().index(actualIndexName).alias(actualName.getReadName()))
                                .addAliasAction(IndicesAliasesRequest.AliasActions.add().index(actualIndexName).alias(actualName.getWriteName())));

                    IndexHandleImpl handle = new IndexHandleImpl(name, actualName.getReadName(), actualName.getWriteName(), actualIndexName);
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Created index " + handle); //$NON-NLS-1$
                    }
                    MappingManagerImpl.this.cache.put(name, handle);
                    return handle;
                }
            });
        }
        catch ( Exception e ) {
            throw new ElasticsearchMappingException("Failed to create index", e); //$NON-NLS-1$
        }
    }


    /**
     * @param name
     * @param cl
     * @param noCheckExists
     *            whether to check that the backing index exists. if true, the handle may contain a null backing index.
     * @throws ClientException
     */
    IndexHandle ensureAliasing ( String name, Client cl, boolean noCheckExists ) throws ClientException {

        if ( name.indexOf('*') >= 0 ) {
            throw new IllegalArgumentException("Index is a wildcard"); //$NON-NLS-1$
        }

        String primary = getPrimaryAliasName(name);
        String readAlias = getReadAlias(primary);
        String writeAlias = getWriteAlias(primary);
        String backingRead = getAliasStatus(cl, readAlias);
        String backingWrite = getAliasStatus(cl, writeAlias);

        if ( backingRead != null && backingWrite != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Already has both aliases %s: backing index is read %s write %s", name, backingRead, backingWrite)); //$NON-NLS-1$
            }
            return new IndexHandleImpl(name, readAlias, writeAlias, backingWrite);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Backing indices for %s are read %s write %s", name, backingRead, backingWrite)); //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(backingRead) && StringUtils.isBlank(backingWrite) ) {
            if ( !cl.admin().indices().exists(new IndicesExistsRequest(name)).isExists() ) {
                if ( noCheckExists ) {
                    return new IndexHandleImpl(name, readAlias, writeAlias, null);
                }
                throw new IndexNotFoundException(name);
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Index exists " + name); //$NON-NLS-1$
            }
        }

        if ( backingRead == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating read alias " + readAlias); //$NON-NLS-1$
            }

            cl.admin().indices()
                    .aliases(new IndicesAliasesRequest().addAliasAction(IndicesAliasesRequest.AliasActions.add().index(name).alias(readAlias)));
        }

        if ( backingWrite == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating write alias " + writeAlias); //$NON-NLS-1$
            }

            cl.admin().indices()
                    .aliases(new IndicesAliasesRequest().addAliasAction(IndicesAliasesRequest.AliasActions.add().index(name).alias(writeAlias)));
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Created aliases for " + name); //$NON-NLS-1$
        }
        return new IndexHandleImpl(name, readAlias, writeAlias, name);
    }


    @Override
    public String toReadPattern ( Client client, String pattern ) {
        return getReadAlias(pattern);
    }


    /**
     * @param name
     * @return
     */
    static String getWriteAlias ( String name ) {
        return "wr_" + name; //$NON-NLS-1$
    }


    /**
     * @param name
     * @return
     */
    static String getReadAlias ( String name ) {
        return "rd_" + name; //$NON-NLS-1$
    }


    @Override
    public List<IndexHandle> expandIndices ( Client cl, String... indices ) throws ClientException {
        List<IndexHandle> handles = new ArrayList<>();
        for ( String ind : indices ) {
            handles.add(expandIndex(cl, ind));
        }
        return handles;
    }


    @Override
    public List<IndexHandle> expandIndices ( Client cl, Set<String> indices ) throws ClientException {
        List<IndexHandle> handles = new ArrayList<>();
        for ( String ind : indices ) {
            handles.add(expandIndex(cl, ind));
        }
        return handles;
    }


    @Override
    public String[] expandIndicesRead ( Client client, String[] indices ) {
        String[] res = new String[indices.length];
        int i = 0;
        for ( String ind : indices ) {
            res[ i++ ] = getReadAlias(getPrimaryAliasName(ind));
        }
        return res;
    }


    @Override
    public String expandIndexRead ( Client cl, String idx ) {
        return getReadAlias(getPrimaryAliasName(idx));
    }


    @Override
    public String[] expandIndicesWrite ( Client client, String[] indices ) {
        String[] res = new String[indices.length];
        int i = 0;
        for ( String ind : indices ) {
            res[ i++ ] = getWriteAlias(getPrimaryAliasName(ind));
        }
        return res;
    }


    @Override
    public String expandIndexWrite ( Client cl, String idx ) {
        return getWriteAlias(getPrimaryAliasName(idx));
    }


    @Override
    public String[] expandIndicesBacking ( Client client, String[] indices ) throws ClientException {
        String[] res = new String[indices.length];
        int i = 0;
        for ( String ind : indices ) {
            String backing;
            try {
                backing = expandIndex(client, ind).getBacking();
            }
            catch ( IndexNotFoundException e ) {
                // so that this can be used for expansions tolerating non existant indices
                if ( log.isTraceEnabled() ) {
                    log.trace("Index does not exist " + ind, e); //$NON-NLS-1$
                }
                backing = ind;
            }
            res[ i++ ] = backing;
        }
        return res;
    }


    /**
     * @param cl
     * @param ind
     * @return
     * @throws ClientException
     */
    private IndexHandle expandIndex ( Client cl, String ind ) throws ClientException {
        IndexHandle cached = this.cache.get(ind);
        if ( cached != null ) {
            return cached;
        }
        cached = ensureAliasing(ind, cl, false);
        this.cache.put(ind, cached);
        return cached;
    }


    @Override
    public void removeIndices ( Client cl, Set<String> toRemove, IndicesOptions options ) throws ClientException {
        String[] backing = expandIndicesBacking(cl, toRemove.toArray(new String[] {}));
        IndicesAliasesRequest alias = new IndicesAliasesRequest();
        boolean haveAlias = false;
        for ( String idx : toRemove ) {
            String primary = getPrimaryAliasName(idx);
            String readAlias = getReadAlias(primary);
            String writeAlias = getWriteAlias(primary);

            alias.addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(idx).alias(readAlias));
            alias.addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(idx).alias(writeAlias));
            this.cache.remove(idx);
        }
        if ( haveAlias ) {
            cl.admin().indices().aliases(alias);
        }
        cl.admin().indices().delete(new DeleteIndexRequest(backing).indicesOptions(options));
    }


    /**
     * @param cl
     * @param name
     * @param mappings
     * @param indexSettings
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws ClientException
     */
    void createIndex ( Client cl, String name, Collection<Mapping> mappings, IndexSettings indexSettings )
            throws InterruptedException, ExecutionException, ClientException {
        CreateIndexRequest create = new CreateIndexRequest(name);
        if ( log.isDebugEnabled() ) {
            log.debug("Creating index " + name); //$NON-NLS-1$
        }
        for ( Mapping m : mappings ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Mapping %s: %s", m.getTargetType(), m.toSource())); //$NON-NLS-1$
            }
            create.mapping(m.getTargetType(), m.toSource(), XContentType.JSON);
        }

        if ( indexSettings != null ) {
            create.settings(indexSettings.toSource(), indexSettings.getContentType());
        }
        else {
            create.settings(DEFAULT_INDEX_CREATION_SETTINGS);
        }
        cl.admin().indices().create(create);
        cl.admin().cluster().health(new ClusterHealthRequest(name).waitForYellowStatus());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.MappingManager#migrateMappings(java.util.Collection, java.util.Collection)
     */
    @Override
    public MappingMigrationStatus migrateMappings ( Collection<String> names, Collection<Mapping> targetMappings )
            throws ElasticsearchMappingException {
        return migrateMappings(names, targetMappings, null);
    }


    @Override
    public MappingMigrationStatus migrateMappings ( Collection<String> names, Collection<Mapping> targetMappings, IndexSettings indexSettings )
            throws ElasticsearchMappingException {
        try ( Client cl = this.esClientProvider.client() ) {
            return Detach.runDetached(new DetachedRunnable<MappingMigrationStatus>() {

                @Override
                public MappingMigrationStatus run () throws Exception {
                    return migrateMappingsInternal(cl, names, targetMappings, indexSettings);
                }

            });
        }
        catch ( Exception e ) {
            throw new ElasticsearchMappingException("Failed to run mapping migration", e); //$NON-NLS-1$
        }
    }


    @Override
    public MappingMigrationStatus migrateMappings ( Client cl, Collection<String> name, Collection<Mapping> targetMappings )
            throws ElasticsearchMappingException {
        return migrateMappings(cl, name, targetMappings, null);
    }


    @Override
    public MappingMigrationStatus migrateMappings ( Client cl, Collection<String> name, Collection<Mapping> targetMappings,
            IndexSettings indexSettings ) throws ElasticsearchMappingException {
        try {
            return migrateMappingsInternal(cl, name, targetMappings, indexSettings);
        }
        catch ( Exception e ) {
            throw new ElasticsearchMappingException("Failed to run mapping migration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cl
     * @param names
     * @param indexSettings
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     * @throws ClientException
     */
    MappingMigrationStatus migrateMappingsInternal ( Client cl, Collection<String> names, Collection<Mapping> targetMappings,
            IndexSettings indexSettings ) throws InterruptedException, ExecutionException, IOException, ClientException {
        if ( log.isDebugEnabled() ) {
            log.debug("Migrating mappings on " + names); //$NON-NLS-1$
        }
        Set<String> indexAliases = new HashSet<>();
        Set<String> backingFound = new HashSet<>();
        for ( String name : names ) {
            IndexHandle handle = ensureAliasing(name, cl, false);
            indexAliases.add(handle.getReadName());
            if ( handle.getBacking() != null ) {
                backingFound.add(handle.getBacking());
            }
        }

        Set<String> oldIndices = new HashSet<>(names);
        oldIndices.removeAll(backingFound);

        if ( !oldIndices.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found old index versions " + oldIndices); //$NON-NLS-1$
            }
        }

        Map<String, MappingMetaData> mappingsByType = new HashMap<>();
        MappingMetaData defaultMapping = null;
        for ( Mapping m : targetMappings ) {
            MappingMetaData meta = new MappingMetaData(new CompressedXContent(m.toSource()));
            if ( Mapping.DEFAULT.equals(m.getTargetType()) ) {
                defaultMapping = meta;
            }
            else {
                mappingsByType.put(m.getTargetType(), meta);
            }
        }

        String[] aliasArray = indexAliases.toArray(new String[indexAliases.size()]);
        GetMappingsRequest gmReq = new GetMappingsRequest();
        gmReq.indices(aliasArray);
        GetMappingsResponse gmResp = cl.admin().indices().getMappings(gmReq);
        GetSettingsRequest getSettingsReq = new GetSettingsRequest();
        getSettingsReq.indices(aliasArray);
        GetSettingsResponse settingsResp = cl.admin().indices().getSettings(getSettingsReq);

        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> m = gmResp.mappings();
        MappingStatus overallStatus = MappingStatus.CURRENT;

        Collection<Future<?>> futures = new LinkedList<>();

        Iterator<String> idxIt = m.keysIt();
        while ( idxIt.hasNext() ) {
            String idx = idxIt.next();

            if ( !backingFound.contains(idx) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Skipping index " + idx); //$NON-NLS-1$
                }
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Found index " + idx); //$NON-NLS-1$
            }

            Settings settings = settingsResp.getIndexToSettings().get(idx);

            updateSettings(cl, idx, settings, indexSettings);

            Version created = Version.fromId(settings.getAsInt("index.version.created", null)); //$NON-NLS-1$
            Integer upgradeId = settings.getAsInt("index.version.upgraded", null); //$NON-NLS-1$
            Version upgraded = upgradeId != null ? Version.fromId(upgradeId) : null;

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Current version is %s, index creation %s, upgraded %s", Version.CURRENT, created, upgraded)); //$NON-NLS-1$
            }

            boolean reindexRequired = false;
            if ( created.major < Version.CURRENT.major ) {
                log.debug("Index was created by older major version, reindexing required"); //$NON-NLS-1$
                reindexRequired = true;
            }

            ImmutableOpenMap<String, MappingMetaData> md = m.get(idx);
            Iterator<String> mit = md.keysIt();

            if ( !mit.hasNext() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Does not have any mapping yet " + idx); //$NON-NLS-1$
                }
            }

            while ( mit.hasNext() ) {
                String typek = mit.next();
                MappingMetaData tgtMapping = mappingsByType.getOrDefault(typek, defaultMapping);
                MappingStatus status = this.comparator.needsUpdate(md.get(typek), tgtMapping, defaultMapping);
                if ( log.isDebugEnabled() ) {
                    log.equals("Status is " + status); //$NON-NLS-1$
                }
                if ( status == MappingStatus.NEEDS_REINDEX ) {
                    reindexRequired |= true;
                }
                else if ( status == MappingStatus.NEEDS_UPDATE ) {

                    // this did not actually put the mapping?!?

                    PutMappingRequest pm = new PutMappingRequest(idx);
                    pm.type(typek);
                    String src = tgtMapping.source().string();
                    pm.source(src, XContentType.JSON);
                    cl.admin().indices().putMapping(pm);
                    if ( overallStatus == MappingStatus.CURRENT ) {
                        overallStatus = MappingStatus.NEEDS_UPDATE;
                    }
                }
                else if ( log.isDebugEnabled() ) {
                    log.debug("Mapping is up to date on " + idx); //$NON-NLS-1$
                }
            }

            if ( reindexRequired ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Index needs reindexing " + idx); //$NON-NLS-1$
                }
                overallStatus = MappingStatus.NEEDS_REINDEX;
                Future<?> f = reindex(cl, idx, targetMappings, indexSettings);
                if ( f != null ) {
                    futures.add(f);
                }
                else {
                    log.warn("Failed to start reindex of " + idx); //$NON-NLS-1$
                }
            }
        }

        return new MappingMigrationStatus(overallStatus, futures);
    }


    /**
     * @param cl
     * @param curSettings
     * @param indexSettings
     * @throws ClientException
     */
    private static void updateSettings ( Client cl, String idx, Settings curSettings, IndexSettings indexSettings ) throws ClientException {
        if ( log.isDebugEnabled() ) {
            log.debug("Current settings are " + curSettings); //$NON-NLS-1$
            log.debug("Target settings are " + ( indexSettings != null ? indexSettings.toSource() : null )); //$NON-NLS-1$
        }

        if ( indexSettings != null ) {
            cl.admin().indices().updateSettings(new UpdateSettingsRequest(idx).settings(indexSettings.toSource(), XContentType.JSON));
        }
        else {
            cl.admin().indices().updateSettings(new UpdateSettingsRequest(idx).settings(DEFAULT_INDEX_SETTING));
        }
    }


    /**
     * @param cl
     * @param mappings
     * @param indexSettings
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws ClientException
     */
    private Future<?> reindex ( Client cl, String oldIndexName, Collection<Mapping> mappings, IndexSettings indexSettings )
            throws InterruptedException, ExecutionException, ClientException {

        int oldVersion = getIndexVersion(oldIndexName);
        String primaryAliasName = getPrimaryAliasName(oldIndexName);
        String readAliasName = getReadAlias(primaryAliasName);
        String writeAliasName = getWriteAlias(primaryAliasName);

        if ( getAliasStatus(cl, readAliasName) == null ) {
            return null;
        }

        if ( getAliasStatus(cl, writeAliasName) == null ) {
            return null;
        }

        int newVersion = oldVersion + 1;
        String newIndexName = makeNewIndexName(primaryAliasName, newVersion);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Primary name %s old name %s new name %s", primaryAliasName, oldIndexName, newIndexName)); //$NON-NLS-1$
        }

        ReindexRunnable task = new ReindexRunnable(
            this.esClientProvider.client(),
            oldIndexName,
            newIndexName,
            readAliasName,
            writeAliasName,
            mappings,
            indexSettings);
        log.debug("Submitting reindex task to executor"); //$NON-NLS-1$
        return this.executor.submit(task);
    }


    /**
     * @param cl
     * @param oldIndexName
     * @param alName
     * @throws ClientException
     */
    private static String getAliasStatus ( Client cl, String alName ) throws ClientException {
        GetAliasesResponse resp = cl.admin().indices().getAliases(new GetAliasesRequest(alName));

        boolean exists = cl.admin().indices().aliasesExist(alName).isExists();
        boolean indexExists = !exists && cl.admin().indices().exists(new IndicesExistsRequest(alName)).isExists();

        if ( indexExists ) {
            log.error("There is an index with the name of the alias " + alName); //$NON-NLS-1$
            return null;
        }

        ImmutableOpenMap<String, List<AliasMetaData>> alMetas = resp.getAliases();
        Iterator<String> keysIt = alMetas.keysIt();

        if ( !keysIt.hasNext() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Failed to find alias " + alName); //$NON-NLS-1$
            }
            if ( exists ) {
                log.error("Alias exists but we could not get information about it " + alName); //$NON-NLS-1$
            }
            return null;
        }

        boolean found = false;
        String backing = null;
        while ( keysIt.hasNext() ) {
            String aliasedIndex = keysIt.next();

            if ( backing != null ) {
                log.warn("Alias points to multiple indices " + alName); //$NON-NLS-1$
            }
            else {
                backing = aliasedIndex;
            }

            for ( AliasMetaData alm : alMetas.get(aliasedIndex) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "Index %s aliased by %s", //$NON-NLS-1$
                        aliasedIndex,
                        alm.getAlias()));
                }

                if ( alName.equals(alm.getAlias()) ) {
                    found = true;
                    if ( !log.isDebugEnabled() ) {
                        break;
                    }
                }
            }
        }
        return found ? backing : null;
    }


    /**
     * @param cl
     * @param oldIndexName
     * @param newIndexName
     * @throws ClientException
     */
    static void reindexData ( Client cl, String oldIndexName, String newIndexName, boolean exists ) throws ClientException {

        // set index settings for fast insertion
        Settings actualSettings = cl.admin().indices().getSettings(new GetSettingsRequest().indices(newIndexName)).getIndexToSettings()
                .get(newIndexName);

        cl.admin().indices().updateSettings(new UpdateSettingsRequest(newIndexName).settings(Settings.builder().put(INDEX_NUMBER_OF_REPLICAS, 0) // $NON-NLS-1$
                .put(INDEX_REFRESH_INTERVAL, -1))); // $NON-NLS-1$

        TimeValue scrollTimeout = TimeValue.timeValueMinutes(5);
        int bulkSize = 1024;
        long startTs = System.currentTimeMillis();
        long count = 0;

        log.info(String.format("Reindexing %s -> %s, this may take some time", oldIndexName, newIndexName)); //$NON-NLS-1$

        SearchRequest sr = new SearchRequest(oldIndexName);
        sr.source(new SearchSourceBuilder().size(bulkSize).query(QueryBuilders.matchAllQuery()));
        sr.scroll(scrollTimeout);
        SearchResponse searchResponse = cl.search(sr);
        try {
            do {
                BulkRequest bulk = new BulkRequest();

                boolean haveSome = false;
                for ( SearchHit searchHit : searchResponse.getHits().getHits() ) {
                    count++;
                    haveSome = true;
                    bulk.add(
                        new IndexRequest(newIndexName, searchHit.getType(), searchHit.getId()).opType(OpType.CREATE)
                                .source(searchHit.getSourceRef(), XContentType.JSON));
                }
                if ( haveSome ) {
                    cl.bulk(bulk);
                }

                searchResponse = cl.searchScroll(new SearchScrollRequest(searchResponse.getScrollId()).scroll(scrollTimeout));
            }
            while ( !searchResponse.isTimedOut() && searchResponse.getHits().getHits().length > 0 && searchResponse.getScrollId() != null );
        }
        finally {
            if ( searchResponse.getScrollId() != null ) {
                ClearScrollRequest csr = new ClearScrollRequest();
                csr.scrollIds(Arrays.asList(searchResponse.getScrollId()));
                cl.clearScroll(csr);
            }
        }

        // change regular index settings

        cl.admin().indices().updateSettings(
            new UpdateSettingsRequest(newIndexName)
                    .settings(Settings.builder().put(INDEX_NUMBER_OF_REPLICAS, actualSettings.getAsInt(INDEX_NUMBER_OF_REPLICAS, 0)) // $NON-NLS-1$
                            .put(INDEX_REFRESH_INTERVAL, actualSettings.getAsTime(INDEX_REFRESH_INTERVAL, new TimeValue(1, TimeUnit.SECONDS)))));

        // this did not actually force merge
        cl.admin().indices().forceMerge(new ForceMergeRequest(newIndexName));

        log.info(String.format("Reindexed %d documents in %d ms", count, System.currentTimeMillis() - startTs)); //$NON-NLS-1$
    }


    static String makeNewIndexName ( String alName, int version ) {
        String realAlName = alName;
        if ( realAlName.charAt(0) == '_' ) {
            // strip prefix added during migration
            realAlName = realAlName.substring(1);
        }
        return String.format("%s@%d", realAlName, version); //$NON-NLS-1$
    }


    private static int getIndexVersion ( String oldIndexName ) {
        int sepPos = oldIndexName.indexOf('@');
        if ( sepPos < 0 ) {
            return 0;
        }
        return Integer.parseInt(oldIndexName.substring(sepPos + 1));
    }


    private static String getPrimaryAliasName ( String idx ) {
        int sepPos = idx.indexOf('@');
        if ( sepPos < 0 ) {
            return idx; // $NON-NLS-1$
        }
        return idx.substring(0, sepPos);
    }

    /**
     * @author mbechler
     *
     */
    public class ReindexRunnable implements Runnable {

        private Client client;
        private String oldIndex;
        private String newIndex;
        private String writeAlias;
        private IndexSettings indexSettings;
        private Collection<Mapping> mappings;
        private String readAlias;
        private long replicationTimeout = -1;


        /**
         * @param cl
         * @param oldIndex
         * @param newIndex
         * @param readAlias
         * @param writeAlias
         * @param mappings
         * @param indexSettings
         * 
         */
        public ReindexRunnable ( Client cl, String oldIndex, String newIndex, String readAlias, String writeAlias, Collection<Mapping> mappings,
                IndexSettings indexSettings ) {
            this.client = cl;
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
            this.readAlias = readAlias;
            this.writeAlias = writeAlias;
            this.mappings = mappings;
            this.indexSettings = indexSettings;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            try {
                boolean exists = false;
                if ( this.client.admin().indices().exists(new IndicesExistsRequest(this.newIndex)).isExists() ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("New index does already exist " + this.newIndex); //$NON-NLS-1$
                    }
                    exists = true;
                }

                if ( !exists ) {
                    createIndex(this.client, this.newIndex, this.mappings, this.indexSettings);
                }

                // switch write alias to new index
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Alias %s %s -> %s", this.writeAlias, this.oldIndex, this.newIndex)); //$NON-NLS-1$
                }

                this.client.admin().indices().aliases(
                    new IndicesAliasesRequest().addAliasAction(IndicesAliasesRequest.AliasActions.add().index(this.newIndex).alias(this.writeAlias))
                            .addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(this.oldIndex).alias(this.writeAlias)));

                // set old index read only
                this.client.admin().indices()
                        .updateSettings(new UpdateSettingsRequest(this.oldIndex).settings(Settings.builder().put("index.blocks.write", true))); //$NON-NLS-1$

                // reindex old data
                reindexData(this.client, this.oldIndex, this.newIndex, exists);

                // wait for status
                if ( this.replicationTimeout > 0 ) {
                    getLog().debug("Waiting for shards to replicate"); //$NON-NLS-1$
                    ClusterHealthResponse status = this.client.admin().cluster().health(
                        new ClusterHealthRequest(this.newIndex).waitForGreenStatus()
                                .timeout(new TimeValue(this.replicationTimeout, TimeUnit.SECONDS)));

                    if ( status.isTimedOut() ) {
                        getLog().debug("Timeout waiting for cluster state"); //$NON-NLS-1$
                    }
                }

                // switch read alias to new index
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Alias %s %s -> %s", this.readAlias, this.oldIndex, this.newIndex)); //$NON-NLS-1$
                }

                this.client.admin().indices().aliases(
                    new IndicesAliasesRequest().addAliasAction(IndicesAliasesRequest.AliasActions.add().index(this.newIndex).alias(this.readAlias))
                            .addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(this.oldIndex).alias(this.readAlias)));

                // remove old index
                this.client.admin().indices().delete(new DeleteIndexRequest(this.oldIndex));

            }
            catch ( Exception e ) {
                getLog().error("Reindexing failed", e); //$NON-NLS-1$
            }
            finally {
                try {
                    this.client.close();
                }
                catch ( ClientException e ) {
                    getLog().warn("Failed to close client", e); //$NON-NLS-1$
                }
                this.client = null;
                getLog().debug("Finished execution"); //$NON-NLS-1$
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#finalize()
         */
        @Override
        protected void finalize () throws Throwable {
            if ( this.client != null ) {
                this.client.close();
                this.client = null;
            }
        }

    }

}

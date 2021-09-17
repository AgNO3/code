/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;

import eu.agno3.runtime.elasticsearch.AdminClient;
import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;
import eu.agno3.runtime.elasticsearch.ClusterAdminClient;
import eu.agno3.runtime.elasticsearch.IndexAdminClient;


/**
 * @author mbechler
 *
 */
public final class NodeClient implements Client, AdminClient, ClusterAdminClient, IndexAdminClient {

    private final org.elasticsearch.client.Client delegate;


    /**
     * @param cl
     * 
     */
    public NodeClient ( org.elasticsearch.client.Client cl ) {
        this.delegate = cl;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws ClientException {
        this.delegate.close();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#admin()
     */
    @Override
    public AdminClient admin () {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.AdminClient#cluster()
     */
    @Override
    public ClusterAdminClient cluster () {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.AdminClient#indices()
     */
    @Override
    public IndexAdminClient indices () {
        return this;
    }


    /**
     * @param e
     * @return
     */
    private static ClientException wrap ( Exception e ) {
        return new ClientException("Request failed", e); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#search(org.elasticsearch.action.search.SearchRequest)
     */
    @Override
    public SearchResponse search ( SearchRequest srb ) throws ClientException {
        try {
            return this.delegate.search(srb).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#index(org.elasticsearch.action.index.IndexRequest)
     */
    @Override
    public IndexResponse index ( IndexRequest source ) throws ClientException {
        try {
            return this.delegate.index(source).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#indexAsync(org.elasticsearch.action.index.IndexRequest,
     *      org.elasticsearch.action.ActionListener)
     */
    @Override
    public void indexAsync ( IndexRequest index, ActionListener<IndexResponse> actionListener ) {
        this.delegate.index(index, actionListener);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ClientException
     *
     * @see eu.agno3.runtime.elasticsearch.Client#update(org.elasticsearch.action.update.UpdateRequest)
     */
    @Override
    public UpdateResponse update ( UpdateRequest upd ) throws ClientException {
        try {
            return this.delegate.update(upd).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#bulk(org.elasticsearch.action.bulk.BulkRequest)
     */
    @Override
    public BulkResponse bulk ( BulkRequest bulk ) throws ClientException {
        try {
            return this.delegate.bulk(bulk).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#bulkAsync(org.elasticsearch.action.bulk.BulkRequest,
     *      org.elasticsearch.action.ActionListener)
     */
    @Override
    public void bulkAsync ( BulkRequest bulk, ActionListener<BulkResponse> actionListener ) {
        this.delegate.bulk(bulk, actionListener);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#searchScroll(org.elasticsearch.action.search.SearchScrollRequest)
     */
    @Override
    public SearchResponse searchScroll ( SearchScrollRequest scroll ) throws ClientException {
        try {
            return this.delegate.searchScroll(scroll).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#clearScroll(org.elasticsearch.action.search.ClearScrollRequest)
     */
    @Override
    public ClearScrollResponse clearScroll ( ClearScrollRequest scroll ) throws ClientException {
        try {
            return this.delegate.clearScroll(scroll).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#delete(org.elasticsearch.action.delete.DeleteRequest)
     */
    @Override
    public DeleteResponse delete ( DeleteRequest del ) throws ClientException {
        try {
            return this.delegate.delete(del).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.ClusterAdminClient#health(org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest)
     */
    @Override
    public ClusterHealthResponse health ( ClusterHealthRequest hr ) throws ClientException {
        try {
            return this.delegate.admin().cluster().health(hr).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.ClusterAdminClient#state(org.elasticsearch.action.admin.cluster.state.ClusterStateRequest)
     */
    @Override
    public ClusterStateResponse state ( ClusterStateRequest clusterStateRequest ) throws ClientException {
        try {
            return this.delegate.admin().cluster().state(clusterStateRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#aliases(org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest)
     */
    @Override
    public IndicesAliasesResponse aliases ( IndicesAliasesRequest request ) throws ClientException {
        try {
            return this.delegate.admin().indices().aliases(request).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#aliasesExist(java.lang.String)
     */
    @Override
    public AliasesExistResponse aliasesExist ( String alName ) {
        return this.delegate.admin().indices().prepareAliasesExist(alName).get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#close(org.elasticsearch.action.admin.indices.close.CloseIndexRequest)
     */
    @Override
    public CloseIndexResponse close ( CloseIndexRequest closeIndexRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().close(closeIndexRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#open(org.elasticsearch.action.admin.indices.open.OpenIndexRequest)
     */
    @Override
    public OpenIndexResponse open ( OpenIndexRequest openIndexRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().open(openIndexRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#create(org.elasticsearch.action.admin.indices.create.CreateIndexRequest)
     */
    @Override
    public CreateIndexResponse create ( CreateIndexRequest create ) throws ClientException {
        try {
            return this.delegate.admin().indices().create(create).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#delete(org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest)
     */
    @Override
    public DeleteIndexResponse delete ( DeleteIndexRequest deleteRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().delete(deleteRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#exists(org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest)
     */
    @Override
    public IndicesExistsResponse exists ( IndicesExistsRequest indicesExistsRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().exists(indicesExistsRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#flush(org.elasticsearch.action.admin.indices.flush.FlushRequest)
     */
    @Override
    public FlushResponse flush ( FlushRequest flushRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().flush(flushRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#forceMerge(org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest)
     */
    @Override
    public ForceMergeResponse forceMerge ( ForceMergeRequest forceMergeRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().forceMerge(forceMergeRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#getAliases(org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest)
     */
    @Override
    public GetAliasesResponse getAliases ( GetAliasesRequest getAliasesRequest ) throws ClientException {
        try {
            return this.delegate.admin().indices().getAliases(getAliasesRequest).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#getMappings(org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest)
     */
    @Override
    public GetMappingsResponse getMappings ( GetMappingsRequest gmReq ) throws ClientException {
        try {
            return this.delegate.admin().indices().getMappings(gmReq).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#getSettings(org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest)
     */
    @Override
    public GetSettingsResponse getSettings ( GetSettingsRequest getSettingsReq ) throws ClientException {
        try {
            return this.delegate.admin().indices().getSettings(getSettingsReq).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#putMapping(org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest)
     */
    @Override
    public PutMappingResponse putMapping ( PutMappingRequest source ) throws ClientException {
        try {
            return this.delegate.admin().indices().putMapping(source).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#refresh(org.elasticsearch.action.admin.indices.refresh.RefreshRequest)
     */
    @Override
    public RefreshResponse refresh ( RefreshRequest refreshReq ) throws ClientException {
        try {
            return this.delegate.admin().indices().refresh(refreshReq).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#stats(org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest)
     */
    @Override
    public IndicesStatsResponse stats ( IndicesStatsRequest indices ) throws ClientException {
        try {
            return this.delegate.admin().indices().stats(indices).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexAdminClient#updateSettings(org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest)
     */
    @Override
    public UpdateSettingsResponse updateSettings ( UpdateSettingsRequest settings ) throws ClientException {
        try {
            return this.delegate.admin().indices().updateSettings(settings).get();
        }
        catch (
            InterruptedException |
            ExecutionException e ) {
            throw wrap(e);
        }
    }
}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


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


/**
 * @author mbechler
 *
 */

@SuppressWarnings ( "javadoc" )
public interface IndexAdminClient {

    IndicesAliasesResponse aliases ( IndicesAliasesRequest request ) throws ClientException;


    DeleteIndexResponse delete ( DeleteIndexRequest indicesOptions ) throws ClientException;


    CreateIndexResponse create ( CreateIndexRequest create ) throws ClientException;


    GetMappingsResponse getMappings ( GetMappingsRequest gmReq ) throws ClientException;


    GetSettingsResponse getSettings ( GetSettingsRequest getSettingsReq ) throws ClientException;


    PutMappingResponse putMapping ( PutMappingRequest source ) throws ClientException;


    RefreshResponse refresh ( RefreshRequest indicesOptions ) throws ClientException;


    FlushResponse flush ( FlushRequest flushRequest ) throws ClientException;


    CloseIndexResponse close ( CloseIndexRequest closeIndexRequest ) throws ClientException;


    OpenIndexResponse open ( OpenIndexRequest openIndexRequest ) throws ClientException;


    ForceMergeResponse forceMerge ( ForceMergeRequest forceMergeRequest ) throws ClientException;


    UpdateSettingsResponse updateSettings ( UpdateSettingsRequest settings ) throws ClientException;


    IndicesStatsResponse stats ( IndicesStatsRequest indices ) throws ClientException;


    GetAliasesResponse getAliases ( GetAliasesRequest getAliasesRequest ) throws ClientException;


    IndicesExistsResponse exists ( IndicesExistsRequest indicesExistsRequest ) throws ClientException;


    AliasesExistResponse aliasesExist ( String alName );

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public interface ClusterAdminClient {

    ClusterHealthResponse health ( ClusterHealthRequest hr ) throws ClientException;


    ClusterStateResponse state ( ClusterStateRequest clusterStateRequest ) throws ClientException;

}

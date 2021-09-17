/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import org.elasticsearch.action.ActionListener;
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


/**
 * @author mbechler
 *
 */
public interface Client extends AutoCloseable {

    @Override
    void close () throws ClientException;


    /**
     * @param srb
     * @return search response
     * @throws ClientException
     */
    SearchResponse search ( SearchRequest srb ) throws ClientException;


    /**
     * @param source
     * @return index response
     * @throws ClientException
     */
    IndexResponse index ( IndexRequest source ) throws ClientException;


    /**
     * @param index
     * @param actionListener
     */
    void indexAsync ( IndexRequest index, ActionListener<IndexResponse> actionListener );


    /**
     * @param upd
     * @return update response
     * @throws ClientException
     */
    UpdateResponse update ( UpdateRequest upd ) throws ClientException;


    /**
     * @param bulk
     * @return bulk response
     * @throws ClientException
     */
    BulkResponse bulk ( BulkRequest bulk ) throws ClientException;


    /**
     * @param bulk
     * @param actionListener
     */
    void bulkAsync ( BulkRequest bulk, ActionListener<BulkResponse> actionListener );


    /**
     * @param scroll
     * @return search response
     * @throws ClientException
     */
    SearchResponse searchScroll ( SearchScrollRequest scroll ) throws ClientException;


    /**
     * @param scroll
     * @param scrollIds
     * @return clear scroll response
     * @throws ClientException
     */
    ClearScrollResponse clearScroll ( ClearScrollRequest scroll ) throws ClientException;


    /**
     * @param del
     * @return delete response
     * @throws ClientException
     */
    DeleteResponse delete ( DeleteRequest del ) throws ClientException;


    /**
     * @return admin client
     */
    AdminClient admin ();

}

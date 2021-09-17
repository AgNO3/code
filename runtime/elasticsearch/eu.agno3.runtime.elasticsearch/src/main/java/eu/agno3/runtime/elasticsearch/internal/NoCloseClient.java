/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 25, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


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

import eu.agno3.runtime.elasticsearch.AdminClient;
import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;


/**
 * @author mbechler
 *
 */
public class NoCloseClient implements Client {

    private RESTClient client;


    /**
     * @param client
     */
    public NoCloseClient ( RESTClient client ) {
        this.client = client;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#close()
     */
    @Override
    public void close () throws ClientException {}


    @Override
    public int hashCode () {
        return this.client.hashCode();
    }


    @Override
    public AdminClient admin () {
        return this.client.admin();
    }


    @Override
    public SearchResponse search ( SearchRequest srb ) throws ClientException {
        return this.client.search(srb);
    }


    @Override
    public IndexResponse index ( IndexRequest source ) throws ClientException {
        return this.client.index(source);
    }


    @Override
    public void indexAsync ( IndexRequest index, ActionListener<IndexResponse> actionListener ) {
        this.client.indexAsync(index, actionListener);
    }


    @Override
    public boolean equals ( Object obj ) {
        return this.client.equals(obj);
    }


    @Override
    public UpdateResponse update ( UpdateRequest upd ) throws ClientException {
        return this.client.update(upd);
    }


    @Override
    public BulkResponse bulk ( BulkRequest bulk ) throws ClientException {
        return this.client.bulk(bulk);
    }


    @Override
    public void bulkAsync ( BulkRequest bulk, ActionListener<BulkResponse> actionListener ) {
        this.client.bulkAsync(bulk, actionListener);
    }


    @Override
    public SearchResponse searchScroll ( SearchScrollRequest scroll ) throws ClientException {
        return this.client.searchScroll(scroll);
    }


    @Override
    public ClearScrollResponse clearScroll ( ClearScrollRequest scroll ) throws ClientException {
        return this.client.clearScroll(scroll);
    }


    @Override
    public DeleteResponse delete ( DeleteRequest del ) throws ClientException {
        return this.client.delete(del);
    }


    @Override
    public String toString () {
        return this.client.toString();
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;

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
import org.elasticsearch.client.RestHighLevelClient;

import eu.agno3.runtime.elasticsearch.AdminClient;
import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;


/**
 * @author mbechler
 *
 */
public class RESTClient implements Client {

    private final RestHighLevelClient delegate;


    /**
     * @param highLevelRestClient
     */
    public RESTClient ( RestHighLevelClient highLevelRestClient ) {
        this.delegate = highLevelRestClient;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws ClientException {
        try {
            this.delegate.close();
        }
        catch ( IOException e ) {
            throw wrap(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#admin()
     */
    @Override
    public AdminClient admin () {
        throw new UnsupportedOperationException();
    }


    /**
     * @param e
     * @return
     */
    private static ClientException wrap ( IOException e ) {
        return new ClientException("Request failed", e); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ClientException
     *
     * @see eu.agno3.runtime.elasticsearch.Client#search(org.elasticsearch.action.search.SearchRequest)
     */
    @Override
    public SearchResponse search ( SearchRequest srb ) throws ClientException {
        try {
            return this.delegate.search(srb);
        }
        catch ( IOException e ) {
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
            return this.delegate.index(source);
        }
        catch ( IOException e ) {
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
        this.delegate.indexAsync(index, actionListener);
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
            return this.delegate.update(upd);
        }
        catch ( IOException e ) {
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
            return this.delegate.bulk(bulk);
        }
        catch ( IOException e ) {
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
        this.delegate.bulkAsync(bulk, actionListener);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Client#searchScroll(org.elasticsearch.action.search.SearchScrollRequest)
     */
    @Override
    public SearchResponse searchScroll ( SearchScrollRequest scroll ) throws ClientException {
        try {
            return this.delegate.searchScroll(scroll);
        }
        catch ( IOException e ) {
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
            return this.delegate.clearScroll(scroll);
        }
        catch ( IOException e ) {
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
            return this.delegate.delete(del);
        }
        catch ( IOException e ) {
            throw wrap(e);
        }
    }

}

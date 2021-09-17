/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.ClosedInputStream;

import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPResponse;


/**
 * @author mbechler
 *
 */
public class ICAPResponseImpl implements AutoCloseable, ICAPResponse {

    private int statusCode;
    private Map<String, String> headerParts;
    private ChunkedInputStream inputStream;
    private Map<String, List<String>> respHeaders;
    private ICAPConnectionImpl conn;


    /**
     * @param conn
     * @param statusCode
     * @param respHeaders
     * @param headerParts
     */
    public ICAPResponseImpl ( ICAPConnectionImpl conn, int statusCode, Map<String, List<String>> respHeaders, Map<String, String> headerParts ) {
        this.conn = conn;
        this.statusCode = statusCode;
        this.respHeaders = respHeaders;
        this.headerParts = headerParts;
    }


    /**
     * 
     * @param conn
     * @param statusCode
     * @param respHeaders
     * @param headerParts
     * @param bodyInputStream
     */
    public ICAPResponseImpl ( ICAPConnectionImpl conn, int statusCode, Map<String, List<String>> respHeaders, Map<String, String> headerParts,
            ChunkedInputStream bodyInputStream ) {
        this.conn = conn;
        this.statusCode = statusCode;
        this.respHeaders = respHeaders;
        this.headerParts = headerParts;
        this.inputStream = bodyInputStream;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getStatusCode()
     */
    @Override
    public int getStatusCode () {
        return this.statusCode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getResponseHeaders()
     */
    @Override
    public Map<String, List<String>> getResponseHeaders () {
        return this.respHeaders;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getHeaderParts()
     */
    @Override
    public Map<String, String> getHeaderParts () {
        return this.headerParts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getInputStream()
     */
    @Override
    public InputStream getInputStream () {
        if ( this.inputStream == null ) {
            return ClosedInputStream.CLOSED_INPUT_STREAM;
        }
        return this.inputStream;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#close()
     */
    @Override
    public void close () throws IOException, ICAPException {
        try {
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
        }
        catch ( IOException e ) {
            this.conn.error(e);
            throw e;
        }
        finally {
            this.conn.releaseResponse(this);
        }
    }

}

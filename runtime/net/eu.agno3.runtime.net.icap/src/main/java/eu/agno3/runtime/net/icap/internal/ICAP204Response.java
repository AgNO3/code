/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPResponse;


/**
 * @author mbechler
 *
 */
public class ICAP204Response implements ICAPResponse {

    private Map<String, List<String>> icapResponseHeaders;
    private Map<String, String> headerParts;
    private InputStream inputStream;


    /**
     * @param icapResponseHeaders
     * @param inputStream
     * @param headerParts
     */
    public ICAP204Response ( Map<String, List<String>> icapResponseHeaders, InputStream inputStream, Map<String, String> headerParts ) {
        this.icapResponseHeaders = icapResponseHeaders;
        this.inputStream = inputStream;
        this.headerParts = headerParts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getStatusCode()
     */
    @Override
    public int getStatusCode () {
        return 204;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#getResponseHeaders()
     */
    @Override
    public Map<String, List<String>> getResponseHeaders () {
        return this.icapResponseHeaders;
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
        return this.inputStream;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPResponse#close()
     */
    @Override
    public void close () throws IOException, ICAPException {
        // ignore
    }

}

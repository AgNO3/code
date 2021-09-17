/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import javax.net.ssl.SSLEngine;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;


/**
 * @author mbechler
 *
 */
public class TLSConnectionFactory extends SslConnectionFactory {

    private final TLSConnectionStatisticsInternal stats;


    /**
     * @param stats
     */
    public TLSConnectionFactory ( TLSConnectionStatisticsInternal stats ) {
        super();
        this.stats = stats;
    }


    /**
     * 
     * @param factory
     * @param nextProtocol
     * @param stats
     */
    public TLSConnectionFactory ( SslContextFactory factory, String nextProtocol, TLSConnectionStatisticsInternal stats ) {
        super(factory, nextProtocol);
        this.stats = stats;
    }


    /**
     * 
     * @param nextProtocol
     * @param stats
     */
    public TLSConnectionFactory ( String nextProtocol, TLSConnectionStatisticsInternal stats ) {
        super(nextProtocol);
        this.stats = stats;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.SslConnectionFactory#newSslConnection(org.eclipse.jetty.server.Connector,
     *      org.eclipse.jetty.io.EndPoint, javax.net.ssl.SSLEngine)
     */
    @Override
    protected SslConnection newSslConnection ( Connector connector, EndPoint endPoint, SSLEngine engine ) {
        return new TLSConnection(connector.getByteBufferPool(), connector.getExecutor(), endPoint, engine, this.stats);
    }

}

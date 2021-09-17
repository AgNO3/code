/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.syslog.internal;


import org.graylog2.syslog4j.impl.net.tcp.ssl.pool.PooledSSLTCPNetSyslogConfig;

import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class ProperPooledSSLTCPNetSyslogConfig extends PooledSSLTCPNetSyslogConfig {

    /**
     * 
     */
    private static final long serialVersionUID = 1347643589332827598L;

    private TLSContext ctx;


    /**
     * @param ctx
     * 
     */
    public ProperPooledSSLTCPNetSyslogConfig ( TLSContext ctx ) {
        this.ctx = ctx;
    }


    /**
     * @return the ctx
     */
    public TLSContext getTLSContext () {
        return this.ctx;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.graylog2.syslog4j.impl.net.tcp.ssl.pool.PooledSSLTCPNetSyslogConfig#getSyslogClass()
     */
    @Override
    public Class getSyslogClass () {
        return ProperSSLNetSyslog.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.graylog2.syslog4j.impl.net.tcp.ssl.pool.PooledSSLTCPNetSyslogConfig#getSyslogWriterClass()
     */
    @Override
    public Class getSyslogWriterClass () {
        return ProperSSLNetSyslogWriter.class;
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.syslog.internal;


import javax.net.SocketFactory;

import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogWriter;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoRuntimeException;


/**
 * @author mbechler
 *
 */
public class ProperSSLNetSyslogWriter extends SSLTCPNetSyslogWriter {

    /**
     * 
     */
    private static final long serialVersionUID = -4790069762181192153L;


    /**
     * {@inheritDoc}
     *
     * @see org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogWriter#obtainSocketFactory()
     */
    @Override
    protected SocketFactory obtainSocketFactory () {
        try {
            return ( (ProperPooledSSLTCPNetSyslogConfig) this.syslog.getConfig() ).getTLSContext().getSocketFactory();
        }
        catch ( CryptoException e ) {
            throw new CryptoRuntimeException(e);
        }
    }

}

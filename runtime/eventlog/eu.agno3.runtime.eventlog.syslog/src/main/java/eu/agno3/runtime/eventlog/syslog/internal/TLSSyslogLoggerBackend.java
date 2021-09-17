/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.syslog.internal;


import java.util.Dictionary;

import org.graylog2.syslog4j.impl.net.AbstractNetSyslogConfigIF;
import org.graylog2.syslog4j.impl.net.tcp.ssl.pool.PooledSSLTCPNetSyslogConfig;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
@Component ( service = TLSSyslogLoggerBackend.class, configurationPid = "eventLog.syslog.tls", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TLSSyslogLoggerBackend extends SyslogLoggerBackend {

    private TLSContext tlsContext;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.syslog.internal.SyslogLoggerBackend#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected synchronized void activate ( ComponentContext ctx ) {
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.syslog.internal.SyslogLoggerBackend#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Deactivate
    @Override
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    @Reference ( target = "(|(subsystem=syslog)(role=client)(role=default))" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    /**
     * @param type
     * @param cfg
     * @return
     */
    @Override
    protected AbstractNetSyslogConfigIF makeConfig ( String type, Dictionary<String, Object> cfg ) {
        switch ( type ) {
        case "tcp": //$NON-NLS-1$
            PooledSSLTCPNetSyslogConfig pcfg = new ProperPooledSSLTCPNetSyslogConfig(this.tlsContext);
            makePoolConfig(cfg, pcfg);
            return pcfg;
        default:
            return null;
        }
    }
}

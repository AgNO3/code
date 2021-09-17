/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.runtime.messaging.broker.transport.impl;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.apache.activemq.transport.tcp.SslTransportServer;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.messaging.broker.BrokerConfigurationException;
import eu.agno3.runtime.messaging.broker.transport.TransportConfiguration;
import eu.agno3.runtime.messaging.broker.transport.TransportFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    TransportFactory.class
}, configurationPid = TransportConfiguration.SSL_PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SSLTransportFactory extends TCPTransportFactory {

    private TLSContext tlsContext;

    private int clientAuth = NO_CLIENT_AUTH;

    private static final int NO_CLIENT_AUTH = 0;
    private static final int WANT_CLIENT_AUTH = 1;
    private static final int NEED_CLIENT_AUTH = 2;


    @Reference ( target = "(subsystem=jms/server)" )
    protected synchronized void setTLSContext ( TLSContext tcf ) {
        this.tlsContext = tcf;
    }


    protected synchronized void unsetTLSContext ( TLSContext tcf ) {
        if ( this.tlsContext == tcf ) {
            this.tlsContext = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.messaging.broker.transport.impl.TCPTransportFactory#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    protected void activate ( ComponentContext context ) throws BrokerConfigurationException, IOException {
        super.activate(context);

        String clientAuthSpec = (String) context.getProperties().get("clientAuth"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(clientAuthSpec) ) {
            String spec = clientAuthSpec.trim();
            if ( "no".equalsIgnoreCase(spec) ) { //$NON-NLS-1$
                this.clientAuth = NO_CLIENT_AUTH;
            }
            else if ( "want".equalsIgnoreCase(spec) ) { //$NON-NLS-1$
                this.clientAuth = WANT_CLIENT_AUTH;
            }
            else if ( "need".equalsIgnoreCase(spec) ) { //$NON-NLS-1$
                this.clientAuth = NEED_CLIENT_AUTH;
            }
        }
    }


    @Override
    protected URI createBrokerURI ( String bindToSpec, int port ) throws URISyntaxException {
        return new URI("nio+ssl", null, bindToSpec, port, null, null, null); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.transport.impl.TCPTransportFactory#createTransport()
     */
    @Override
    public TransportServer createTransport () throws BrokerConfigurationException {
        try {
            SslTransportFactory sslTransportFactory = new SslTransportFactory();
            SslTransportServer s = new SslTransportServer(sslTransportFactory, this.getLocation(), this.tlsContext.getServerSocketFactory());

            // TODO: should be moved to the tls config
            setupClientAuth(s);
            s.setTransportOption(new HashMap<String, Object>());
            s.setDaemon(true);
            s.bind();
            return s;
        }
        catch (
            URISyntaxException |
            IOException |
            CryptoException e ) {
            throw new BrokerConfigurationException(e);
        }

    }


    /**
     * @param s
     */
    protected void setupClientAuth ( SslTransportServer s ) {
        if ( this.clientAuth == NEED_CLIENT_AUTH ) {
            s.setWantClientAuth(false);
            s.setNeedClientAuth(true);
        }
        else if ( this.clientAuth == WANT_CLIENT_AUTH ) {
            s.setWantClientAuth(true);
            s.setNeedClientAuth(false);
        }
        else {
            s.setWantClientAuth(false);
            s.setNeedClientAuth(false);
        }
    }
}

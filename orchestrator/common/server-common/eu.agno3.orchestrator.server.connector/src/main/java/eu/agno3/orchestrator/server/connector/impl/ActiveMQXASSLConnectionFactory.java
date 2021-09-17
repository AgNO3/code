/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.net.URI;

import javax.jms.JMSException;
import javax.net.ssl.SSLContext;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.Transport;
import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class ActiveMQXASSLConnectionFactory extends ActiveMQXAConnectionFactory {

    private static final String TLS_PROTOCOL = "TLSv1.2"; //$NON-NLS-1$
    private static final String FAILED_TO_CONFIGURE_TRANSPORT = "Failed to configure transport"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ActiveMQXAConnectionFactory.class);

    private TLSContext tlsContext;


    /**
     * 
     */
    public ActiveMQXASSLConnectionFactory () {
        super();
    }


    /**
     * @param tlsContext
     * 
     */
    public ActiveMQXASSLConnectionFactory ( TLSContext tlsContext ) {
        super();
        this.tlsContext = tlsContext;
    }


    /**
     * @param tlsContext
     * @param userName
     * @param password
     * @param brokerURL
     */
    public ActiveMQXASSLConnectionFactory ( TLSContext tlsContext, String userName, String password, String brokerURL ) {
        super(userName, password, brokerURL);
        this.tlsContext = tlsContext;
    }


    /**
     * @param tlsContext
     * @param userName
     * @param password
     * @param brokerURL
     */
    public ActiveMQXASSLConnectionFactory ( TLSContext tlsContext, String userName, String password, URI brokerURL ) {
        super(userName, password, brokerURL);
        this.tlsContext = tlsContext;
    }


    /**
     * @param tlsContext
     * @param brokerURL
     */
    public ActiveMQXASSLConnectionFactory ( TLSContext tlsContext, String brokerURL ) {
        super(brokerURL);
        this.tlsContext = tlsContext;
    }


    /**
     * @param tlsContext
     * @param brokerURL
     */
    public ActiveMQXASSLConnectionFactory ( TLSContext tlsContext, URI brokerURL ) {
        super(brokerURL);
        this.tlsContext = tlsContext;
    }


    @Override
    protected Transport createTransport () throws JMSException {
        SslContext existing = SslContext.getCurrentSslContext();
        try {
            SslContext.setCurrentSslContext(createSSLContext());
            Transport t = super.createTransport();

            return t;
        }
        catch ( Exception e ) {
            log.warn(FAILED_TO_CONFIGURE_TRANSPORT, e);
            throw new JMSException(FAILED_TO_CONFIGURE_TRANSPORT);
        }
        finally {
            SslContext.setCurrentSslContext(existing);
        }
    }


    private SslContext createSSLContext () throws CryptoException {
        SSLContext context = this.tlsContext.getContext();
        SslContext ctx = new SslContext(this.tlsContext.getKeyManagers(), this.tlsContext.getTrustManagers(), this.tlsContext.getRandomSource());
        ctx.setSSLContext(context);
        ctx.setProtocol(TLS_PROTOCOL);
        return ctx;
    }
}

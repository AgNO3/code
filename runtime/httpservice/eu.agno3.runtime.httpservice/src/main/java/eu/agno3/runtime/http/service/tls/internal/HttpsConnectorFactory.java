/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.http.service.ReverseProxyConfig;
import eu.agno3.runtime.http.service.connector.ConnectorFactory;
import eu.agno3.runtime.http.service.internal.HttpConnectorFactory;
import eu.agno3.runtime.jmx.MBeanHolder;


/**
 * @author mbechler
 *
 */
@Component ( configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = HttpsConnectorFactory.SSL_PID, service = {
    ConnectorFactory.class, MBeanHolder.class
}, property = {
    "protocol=https"
} )
public class HttpsConnectorFactory extends HttpConnectorFactory implements MBeanHolder {

    private static final Logger log = Logger.getLogger(HttpsConnectorFactory.class);

    /**
     * PID for configuration factory
     */
    public static final String SSL_PID = "httpservice.connector.https"; //$NON-NLS-1$

    private TLSContext tlsContext;

    private TLSConnectionStatisticsInternal stats = new TLSConnectionStatisticsImpl();


    @Reference ( target = "(subsystem=https)" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Override
    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindReverseProxyConfig ( ReverseProxyConfig rpc ) {
        super.bindReverseProxyConfig(rpc);
    }


    @Override
    protected synchronized void unbindReverseProxyConfig ( ReverseProxyConfig rpc ) {
        super.unbindReverseProxyConfig(rpc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.MBeanHolder#getObjectName()
     */
    @Override
    public String getObjectName () {
        return String.format("eu.agno3.runtime.http.service.tls:connector=\"%s\"", getConnectorName()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.MBeanHolder#getMBean()
     */
    @Override
    public Object getMBean () {
        return this.stats;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.connector.ConnectorFactory#getConfigurationPID()
     */
    @Override
    public String getConfigurationPID () {
        return SSL_PID;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.internal.HttpConnectorFactory#makeHttpConfiguration()
     */
    @Override
    protected HttpConfiguration makeHttpConfiguration () {
        HttpConfiguration cfg = super.makeHttpConfiguration();
        cfg.addCustomizer(new ExtendedSecureRequestCustomizer());
        return cfg;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.connector.AbstractConnectorFactory#createConnectionFactories()
     */
    @Override
    protected List<ConnectionFactory> createConnectionFactories () {
        try {
            return Arrays.asList(
                new TLSConnectionFactory(makeTLSContextFactory(), HttpVersion.HTTP_1_1.asString(), this.stats),
                createConnectionFactory());
        }
        catch ( CryptoException e ) {
            log.error("Failed to create ssl connection factory", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.connector.AbstractConnectorFactory#updateConfig(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    @Modified
    protected void updateConfig ( ComponentContext context ) {
        Dictionary<String, Object> props = context.getProperties();

        if ( props.get(HttpServiceConfig.CONNECTOR_NAME) == null ) {
            log.error("Connector does not specify a name"); //$NON-NLS-1$
        }

        super.updateConfig(context);
    }


    /**
     * @return
     * @throws CryptoException
     */
    private SslContextFactory makeTLSContextFactory () throws CryptoException {
        return new SslContextTLSContextFactory(this.tlsContext);
    }

}

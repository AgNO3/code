/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2013 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.http.service.ProxiableConnectorFactory;
import eu.agno3.runtime.http.service.ReverseProxyConfig;
import eu.agno3.runtime.http.service.connector.AbstractConnectorFactory;
import eu.agno3.runtime.http.service.connector.ConnectorFactory;
import eu.agno3.runtime.http.service.internal.proxy.ReverseProxyConfigImpl;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@Component ( configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = HttpConnectorFactory.PID, service = {
    ConnectorFactory.class
}, property = {
    "protocol=http"
} )
public class HttpConnectorFactory extends AbstractConnectorFactory implements ProxiableConnectorFactory {

    private static final Logger log = Logger.getLogger(HttpConnectorFactory.class);

    /**
     * PID for configuration factory
     */
    public static final String PID = "httpservice.connector.http"; //$NON-NLS-1$

    private int headerCacheSize = -1;
    private int outputBufferSize = -1;
    private int requestHeaderSize = -1;
    private int responseHeaderSize = -1;
    private int securePort = -1;
    private String secureScheme = null;
    private boolean sendDateHeader = false;
    private boolean sendServerVersion = false;

    private ReverseProxyConfig reverseProxyConfig;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.connector.AbstractConnectorFactory#createConnectionFactory()
     */
    @Override
    protected ConnectionFactory createConnectionFactory () {
        return new HttpConnectionFactory(makeHttpConfiguration());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.connector.AbstractConnectorFactory#wrapConnectionFactories(java.util.List)
     */
    @Override
    protected List<ConnectionFactory> wrapConnectionFactories ( List<ConnectionFactory> cfs ) {
        if ( this.reverseProxyConfig != null && this.reverseProxyConfig.isHAProxy() ) {
            List<ConnectionFactory> wcfs = new LinkedList<>(cfs);
            wcfs.add(0, new ProxyConnectionFactory(cfs.get(0).getProtocol()));
            return wcfs;
        }
        return super.wrapConnectionFactories(cfs);
    }


    /**
     * @return
     */
    protected HttpConfiguration makeHttpConfiguration () {
        HttpConfiguration config = new ExtendedHttpConfiguration(this);

        if ( this.headerCacheSize > 0 ) {
            config.setHeaderCacheSize(this.headerCacheSize);
        }

        if ( this.outputBufferSize > 0 ) {
            config.setOutputBufferSize(this.outputBufferSize);
        }

        if ( this.requestHeaderSize > 0 ) {
            config.setRequestHeaderSize(this.requestHeaderSize);
        }

        if ( this.responseHeaderSize > 0 ) {
            config.setResponseHeaderSize(this.responseHeaderSize);
        }

        if ( this.securePort > 0 ) {
            config.setSecurePort(this.securePort);
        }

        if ( this.secureScheme != null ) {
            config.setSecureScheme(this.secureScheme);
        }

        config.setSendDateHeader(this.sendDateHeader);
        config.setSendServerVersion(this.sendServerVersion);
        config.setSendXPoweredBy(false);
        return config;
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindReverseProxyConfig ( ReverseProxyConfig rpc ) {
        this.reverseProxyConfig = rpc;
    }


    protected synchronized void unbindReverseProxyConfig ( ReverseProxyConfig rpc ) {
        if ( this.reverseProxyConfig == rpc ) {
            this.reverseProxyConfig = null;
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

        configureBuffers(props);

        if ( ConfigUtil.parseBoolean(props, "behindReverseProxy", false) ) { //$NON-NLS-1$
            this.reverseProxyConfig = ReverseProxyConfigImpl.fromProperties(props);
        }

        if ( props.get(HttpServiceConfig.SECURE_PORT_ATTRIBUTE) != null ) {
            this.securePort = Integer.parseInt((String) props.get(HttpServiceConfig.SECURE_PORT_ATTRIBUTE));
        }

        if ( props.get(HttpServiceConfig.SECURE_SCHEME_ATTRIBUTE) != null ) {
            this.secureScheme = (String) props.get(HttpServiceConfig.SECURE_SCHEME_ATTRIBUTE);
        }

        configureVerbosity(props);
    }


    /**
     * @param props
     */
    private void configureVerbosity ( Dictionary<String, Object> props ) {
        String sendDateHeaderSpec = (String) props.get(HttpServiceConfig.SEND_DATE_ATTRIBUTE);
        if ( sendDateHeaderSpec != null ) {
            if ( sendDateHeaderSpec.equals(Boolean.TRUE.toString()) ) {
                this.sendDateHeader = true;
            }
            else if ( sendDateHeaderSpec.equals(Boolean.FALSE.toString()) ) {
                this.sendDateHeader = false;
            }
        }

        String sendVersionHeaderSpec = (String) props.get(HttpServiceConfig.SEND_VERSION_ATTRIBUTE);
        if ( sendVersionHeaderSpec != null ) {
            if ( sendVersionHeaderSpec.equals(Boolean.TRUE.toString()) ) {
                this.sendServerVersion = true;
            }
            else if ( sendVersionHeaderSpec.equals(Boolean.FALSE.toString()) ) {
                this.sendServerVersion = false;
            }
        }
    }


    /**
     * @param props
     */
    private void configureBuffers ( Dictionary<String, Object> props ) {
        if ( props.get(HttpServiceConfig.HEADER_CACHE_SIZE_ATTRIBUTE) != null ) {
            this.headerCacheSize = Integer.parseInt((String) props.get(HttpServiceConfig.HEADER_CACHE_SIZE_ATTRIBUTE));
        }

        if ( props.get(HttpServiceConfig.OUTPUT_BUFFER_SIZE_ATTRIBUTE) != null ) {
            this.outputBufferSize = Integer.parseInt((String) props.get(HttpServiceConfig.OUTPUT_BUFFER_SIZE_ATTRIBUTE));
        }

        if ( props.get(HttpServiceConfig.REQUEST_HEADER_SIZE_ATTRIBUTE) != null ) {
            this.requestHeaderSize = Integer.parseInt((String) props.get(HttpServiceConfig.REQUEST_HEADER_SIZE_ATTRIBUTE));
        }

        if ( props.get(HttpServiceConfig.RESPONSE_HEADER_SIZE_ATTRIBUTE) != null ) {
            this.responseHeaderSize = Integer.parseInt((String) props.get(HttpServiceConfig.RESPONSE_HEADER_SIZE_ATTRIBUTE));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.connector.ConnectorFactory#getConfigurationPID()
     */
    @Override
    public String getConfigurationPID () {
        return PID;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.ProxiableConnectorFactory#getReverseProxyConfig()
     */
    @Override
    public ReverseProxyConfig getReverseProxyConfig () {
        return this.reverseProxyConfig;
    }


    /**
     * @return whether a reverse proxy config is available
     * 
     */
    public boolean isBehindReverseProxy () {
        return this.reverseProxyConfig != null;
    }

}

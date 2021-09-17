/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.elasticsearch.Client;
import eu.agno3.runtime.elasticsearch.ClientException;
import eu.agno3.runtime.elasticsearch.ClientProvider;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = ClientProvider.class, configurationPid = "es.client", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class RESTClientProvider implements ClientProvider {

    private static final Logger log = Logger.getLogger(RESTClientProvider.class);

    private String server;
    private int serverPort;
    private String serverProto;
    private RESTClient client;


    /**
     * 
     */
    public RESTClientProvider () {}


    /**
     * 
     * @param host
     * @param port
     * @param proto
     */
    public RESTClientProvider ( String host, int port, String proto ) {
        this.server = host;
        this.serverPort = port;
        this.serverProto = proto;
    }


    @Activate
    @SuppressWarnings ( "resource" )
    protected synchronized void activate ( ComponentContext ctx ) {

        this.server = ConfigUtil.parseString(
            ctx.getProperties(),
            "server", //$NON-NLS-1$
            "localhost"); //$NON-NLS-1$

        this.serverPort = ConfigUtil.parseInt(ctx.getProperties(), "port", 9200); //$NON-NLS-1$
        this.serverProto = ConfigUtil.parseString(
            ctx.getProperties(),
            "protocol", //$NON-NLS-1$
            "http"); //$NON-NLS-1$

        RestClientBuilder lowLevelClientBuilder = RestClient.builder(new HttpHost(this.server, this.serverPort, this.serverProto))
                .setHttpClientConfigCallback(new HttpClientConfigCallback() {

                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient ( HttpAsyncClientBuilder hacb ) {
                        // client can be further configured here, e.g. auth
                        return hacb;
                    }
                });
        this.client = new RESTClient(new RestHighLevelClient(lowLevelClientBuilder));
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        RESTClient cl = this.client;
        if ( cl != null ) {
            this.client = null;
            try {
                cl.close();
            }
            catch ( ClientException e ) {
                log.error("Failed to close ES client", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.ClientProvider#allowsAdminOperations()
     */
    @Override
    public boolean allowsAdminOperations () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.ClientProvider#client()
     */

    @Override
    public Client client () throws ClientException {
        return new NoCloseClient(this.client);
    }

}

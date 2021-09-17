/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import org.eclipse.jetty.server.HttpConfiguration;


/**
 * @author mbechler
 *
 */
public class ExtendedHttpConfiguration extends HttpConfiguration {

    private HttpConnectorFactory httpConnectorFactory;


    /**
     * @param httpConnectorFactory
     */
    public ExtendedHttpConfiguration ( HttpConnectorFactory httpConnectorFactory ) {
        this.httpConnectorFactory = httpConnectorFactory;
    }


    /**
     * @return the httpConnectorFactory
     */
    public HttpConnectorFactory getHttpConnectorFactory () {
        return this.httpConnectorFactory;
    }

}

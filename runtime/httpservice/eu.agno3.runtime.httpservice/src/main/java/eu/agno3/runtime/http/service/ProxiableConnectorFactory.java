/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service;


/**
 * @author mbechler
 *
 */
public interface ProxiableConnectorFactory {

    /**
     * 
     * @return the reverse proxy config, null if not behind proxy
     */
    ReverseProxyConfig getReverseProxyConfig ();
}

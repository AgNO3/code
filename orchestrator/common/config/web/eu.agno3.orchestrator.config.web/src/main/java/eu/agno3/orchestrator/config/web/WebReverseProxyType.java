/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public enum WebReverseProxyType {

    /**
     * No proxy integration
     */
    NONE,

    /**
     * Specify custom headers
     */
    CUSTOM,

    /**
     * Use RFC7239 Forwarded header
     */
    RFC7239
}

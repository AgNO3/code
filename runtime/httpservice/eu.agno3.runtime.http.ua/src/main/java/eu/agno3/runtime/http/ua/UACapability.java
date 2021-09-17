/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua;


/**
 * @author mbechler
 *
 */
public enum UACapability {

    /**
     * General Content security policy 1.0
     */
    CSP10,

    /**
     * CSP support via Content-Security-Policy header
     */
    CSP10_STANDARD_HEADER,

    /**
     * CSP support via X-Content-Security-Policy header
     * 
     */
    CSP10_EXPERIMENTAL_HEADER,

    /**
     * CSP support via X-Webkit-CSP header
     */
    CSP10_WEBKIT_HEADER,

    /**
     * Support for sandboxing via CSP header
     */
    CSP10_SANDBOXING,

    /**
     * Sandboxed iframes
     */
    FRAME_SANDBOXING,

    /**
     * Client does not (properly) support multi auth challenges
     */
    NO_MULTI_AUTH
}

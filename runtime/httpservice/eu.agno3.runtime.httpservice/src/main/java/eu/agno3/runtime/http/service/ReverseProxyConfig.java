/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author mbechler
 *
 */
public interface ReverseProxyConfig {

    /**
     * 
     * @return using HA PROXY protocol
     */
    boolean isHAProxy ();


    /**
     * @return the override port
     */
    Integer getOverridePort ();


    /**
     * @return the override host
     */
    String getOverrideHost ();


    /**
     * @return the override scheme
     */
    String getOverrideScheme ();


    /**
     * @param req
     * @return wrapped request
     */
    HttpServletRequest wrapRequest ( HttpServletRequest req );


    /**
     * @param wrappedResp
     * @param wrappedReq
     * @return wrapped response
     */
    HttpServletResponse wrapResponse ( HttpServletResponse wrappedResp, HttpServletRequest wrappedReq );


    /**
     * @param request
     * @return the original clients requested host name
     */
    String getOriginalHost ( HttpServletRequest request );


    /**
     * @param request
     * @return the original clients requested port
     */
    Integer getOriginalPort ( HttpServletRequest request );


    /**
     * @param request
     * @return the original clients requested scheme
     */
    String getOriginalScheme ( HttpServletRequest request );


    /**
     * @param request
     * @return the original clients remote address
     */
    String getOriginalRemoteAddr ( HttpServletRequest request );


    /**
     * @param request
     * @return the proxy path
     */
    String getProxiedVia ( HttpServletRequest request );


    /**
     * @param request
     * @return the original ciphers used for client transport security
     */
    String getOriginalSSLCipherSpec ( HttpServletRequest request );


    /**
     * @return whether a way to retrieve the original TLS ciphers is configured
     */
    boolean haveOriginalSSLCipherSpec ();

}

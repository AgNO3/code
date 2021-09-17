/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


import java.net.URI;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface ICAPConfiguration {

    /**
     * @return the socket timeout in milliseconds
     */
    int getSocketTimeout ();


    /**
     * @return whether to try StartTLS
     */
    boolean isTryStartTLS ();


    /**
     * @return whether to require StartTLS
     */
    boolean isRequireStartTLS ();


    /**
     * @return the server URI to use
     */
    URI selectServerUri ();


    /**
     * @return the server URI to send in the request
     */
    URI getOverrideRequestURI ();


    /**
     * @return whether to send icaps:// URI in the procotol request
     */
    boolean isSendICAPSInRequest ();


    /**
     * @return request headers to always add
     */
    Map<String, String> getRequestHeaders ();

}

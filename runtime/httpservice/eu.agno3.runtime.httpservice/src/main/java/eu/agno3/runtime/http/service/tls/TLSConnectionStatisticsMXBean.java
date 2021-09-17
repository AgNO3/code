/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 2, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.tls;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface TLSConnectionStatisticsMXBean{

    /**
     * @return total number of connection attempts
     */
    long getNumTotal ();


    /**
     * @return number of connections for which the handshake succeeded
     */
    long getNumSuccessful ();


    /**
     * @return number of connection for which the handshake failed
     */
    long getNumFailed ();


    /**
     * @return failure count by reason
     */
    Map<String, Long> getFailureReasons ();


    /**
     * @return protocol usage in sucessful connections
     */
    Map<String, Long> getSuccessfulProtocols ();


    /**
     * @return cipher usage in successful connections
     */
    Map<String, Long> getSuccessfulCiphers ();


    /**
     * @return rejected protocol usages
     */
    Map<String, Long> getDisabledProtocols ();

}

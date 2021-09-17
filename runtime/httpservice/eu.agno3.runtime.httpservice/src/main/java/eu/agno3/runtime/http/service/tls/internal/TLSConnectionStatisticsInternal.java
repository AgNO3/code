/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 2, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import eu.agno3.runtime.http.service.tls.TLSConnectionStatisticsMXBean;


/**
 * @author mbechler
 *
 */
public interface TLSConnectionStatisticsInternal extends TLSConnectionStatisticsMXBean {

    /**
     * @param session
     */
    void trackSuccessful ( SSLSession session );


    /**
     * @param e
     */
    void trackFailure ( SSLHandshakeException e );

}

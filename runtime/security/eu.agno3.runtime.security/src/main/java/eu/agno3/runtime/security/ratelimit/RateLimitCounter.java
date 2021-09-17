/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit;


import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface RateLimitCounter {

    /**
     * @return the successCount
     */
    int getSuccessCount ();


    /**
     * @return the failCount
     */
    int getFailCount ();


    /**
     * @return the lastFail
     */
    DateTime getLastFail ();


    /**
     * @return the lastSucess
     */
    DateTime getLastSucess ();


    /**
     * 
     */
    void flip ();


    /**
     * 
     */
    void fail ();


    /**
     * 
     */
    void success ();

}
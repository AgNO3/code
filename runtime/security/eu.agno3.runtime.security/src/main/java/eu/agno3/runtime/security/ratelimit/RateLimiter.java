/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit;


/**
 * @author mbechler
 *
 * @param <T>
 */
public interface RateLimiter <T> {

    /**
     * @param obj
     * @return an existing or new counter
     */
    RateLimitCounter get ( T obj );


    /**
     * @param obj
     */
    void fail ( T obj );


    /**
     * @param obj
     */
    void success ( T obj );


    /**
     * @param obj
     * @return the current delay for the object
     */
    int makeDelay ( T obj );


    /**
     * 
     */
    void maintenance ();

}
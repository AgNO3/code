/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit;




/**
 * @author mbechler
 *
 */
public interface LoginRateLimiter {

    /**
     * 
     * @param up
     * @param sourceAddress
     * @return the new delay
     */
    public int recordFailAttempt ( Object up, String sourceAddress );


    /**
     * 
     * @param up
     * @param sourceAddress
     */
    public void recordSuccessAttempt ( Object up, String sourceAddress );


    /**
     * 
     * @param up
     * @param sourceAddress
     * @return whether the login attempt should be prevented
     */
    public boolean preventLogin ( Object up, String sourceAddress );


    /**
     * 
     * @param up
     * @param sourceAddress
     * @return the number of seconds the user must wait for the next login attempt
     */
    public int getNextLoginDelay ( Object up, String sourceAddress );

}

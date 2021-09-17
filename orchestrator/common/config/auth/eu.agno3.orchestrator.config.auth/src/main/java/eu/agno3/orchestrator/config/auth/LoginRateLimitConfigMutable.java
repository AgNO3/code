/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LoginRateLimitConfig.class )
public interface LoginRateLimitConfigMutable extends LoginRateLimitConfig {

    /**
     * 
     * @param cleanInterval
     */
    void setCleanInterval ( Duration cleanInterval );


    /**
     * 
     * @param disableGlobalDelay
     */
    void setDisableGlobalDelay ( Boolean disableGlobalDelay );


    /**
     * 
     * @param disableUserLockout
     */
    void setDisableUserLockout ( Boolean disableUserLockout );


    /**
     * 
     * @param disableLaxSourceCheck
     */
    void setDisableLaxSourceCheck ( Boolean disableLaxSourceCheck );

}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:rateLimit" )
public interface LoginRateLimitConfig extends ConfigurationObject {

    /**
     * 
     * @return interval after which if no failed attempt has been recorded in the last intveral, the failure counter
     *         will be reset
     */
    Duration getCleanInterval ();


    /**
     * 
     * @return disable the additive global delay
     */
    Boolean getDisableGlobalDelay ();


    /**
     * 
     * @return disable tracking of per user failures
     */
    Boolean getDisableUserLockout ();


    /**
     * 
     * @return disable lax source checking ( which means that if for a source a successful login is recorded it will not
     *         be penalized)
     */
    Boolean getDisableLaxSourceCheck ();

}

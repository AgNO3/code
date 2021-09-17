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
@MapAs ( PasswordPolicyConfig.class )
public interface PasswordPolicyConfigMutable extends PasswordPolicyConfig {

    /**
     * 
     * @param ignoreUnknownAge
     */
    void setIgnoreUnknownAge ( Boolean ignoreUnknownAge );


    /**
     * 
     * @param maximumPasswordAge
     */
    void setMaximumPasswordAge ( Duration maximumPasswordAge );


    /**
     * 
     * @param entropyLowerLimit
     */
    void setEntropyLowerLimit ( Integer entropyLowerLimit );


    /**
     * @param enableAgeCheck
     */
    void setEnableAgeCheck ( Boolean enableAgeCheck );
}

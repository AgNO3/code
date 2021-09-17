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
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:pwPolicy" )
public interface PasswordPolicyConfig extends ConfigurationObject {

    /**
     * 
     * @return whether to ignore maximum password age restriction for authenticators that do not provide a password age
     */
    Boolean getIgnoreUnknownAge ();


    /**
     * 
     * @return maximum password age to enforce
     */
    Duration getMaximumPasswordAge ();


    /**
     * 
     * @return lower limit for acceptable estimated password entropy
     */
    Integer getEntropyLowerLimit ();


    /**
     * @return whether to check password age
     */
    Boolean getEnableAgeCheck ();

}

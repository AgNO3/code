/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.internal;


import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.service.config.PolicyConfiguration;


/**
 * @author mbechler
 *
 */
public interface PolicyRule {

    /**
     * 
     * @return a priority for the rule
     */
    float getPriority ();


    /**
     * @param policyConfig
     * @param ctx
     * @return whether the request is acceptable
     */
    PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx );

}

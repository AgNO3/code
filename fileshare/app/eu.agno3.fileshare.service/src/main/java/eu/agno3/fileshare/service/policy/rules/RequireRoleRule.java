/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.rules;


import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext;
import eu.agno3.fileshare.service.policy.internal.PolicyRule;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyRule.class )
public class RequireRoleRule implements PolicyRule {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#getPriority()
     */
    @Override
    public float getPriority () {
        return -200;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#isFulfilledForAccess(eu.agno3.fileshare.service.config.PolicyConfiguration,
     *      eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext)
     */
    @Override
    public PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx ) {

        if ( policyConfig.getRequireAnyRole() == null || policyConfig.getRequireAnyRole().isEmpty() ) {
            return null;
        }

        for ( String role : policyConfig.getRequireAnyRole() ) {
            if ( ctx.getAuthSubject().hasRole(role) ) {
                return null;
            }
        }

        return new PolicyViolation("roles.missing"); //$NON-NLS-1$
    }

}

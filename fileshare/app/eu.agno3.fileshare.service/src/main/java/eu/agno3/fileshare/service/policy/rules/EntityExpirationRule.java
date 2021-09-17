/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.rules;


import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext;
import eu.agno3.fileshare.service.policy.internal.PolicyRule;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyRule.class )
public class EntityExpirationRule implements PolicyRule {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#getPriority()
     */
    @Override
    public float getPriority () {
        return -100.0f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#isFulfilledForAccess(eu.agno3.fileshare.service.config.PolicyConfiguration,
     *      eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext)
     */
    @Override
    public PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx ) {

        if ( ! ( ctx.getEntity() instanceof FileEntity ) ) {
            return null;
        }

        if ( policyConfig.getMaximumExpirationDuration() == null ) {
            return null;
        }

        if ( ctx.getEntity().getCreated().plus(policyConfig.getMaximumExpirationDuration()).isBeforeNow() ) {
            return new PolicyViolation("entity.maxExpiration", policyConfig.getMaximumExpirationDuration().getStandardDays()); //$NON-NLS-1$
        }

        return null;
    }
}

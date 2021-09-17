/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.rules;


import java.util.HashSet;

import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext;
import eu.agno3.fileshare.service.policy.internal.PolicyRule;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyRule.class )
public class ShareTypeRule implements PolicyRule {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#getPriority()
     */
    @Override
    public float getPriority () {
        return -600.0f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#isFulfilledForAccess(eu.agno3.fileshare.service.config.PolicyConfiguration,
     *      eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext)
     */
    @Override
    public PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx ) {
        if ( ctx.isOwner() || ctx.getEntity() == null ) {
            return null;
        }

        if ( ctx.getGrant() == null ) {
            return new PolicyViolation("shareType.noGrant"); //$NON-NLS-1$
        }

        GrantType grantType = getGrantType(ctx.getGrant());
        if ( grantType == null || ( policyConfig.getAllowedShareTypes() != null && !policyConfig.getAllowedShareTypes().contains(grantType) ) ) {
            return new PolicyViolation(
                "shareType.invalidType", grantType != null ? grantType.name() : null, new HashSet<>(policyConfig.getAllowedShareTypes())); //$NON-NLS-1$;
        }
        return null;
    }


    /**
     * @param grant
     */
    private static GrantType getGrantType ( Grant grant ) {

        if ( grant == null ) {
            return null;
        }

        if ( grant instanceof SubjectGrant ) {
            return GrantType.SUBJECT;
        }
        else if ( grant instanceof MailGrant ) {
            return GrantType.MAIL;
        }
        else if ( grant instanceof TokenGrant ) {
            return GrantType.LINK;
        }
        return null;
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.rules;


import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext;
import eu.agno3.fileshare.service.policy.internal.PolicyRule;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.AuthFactorType;
import eu.agno3.runtime.security.principal.factors.CertificateFactor;
import eu.agno3.runtime.security.principal.factors.OneTimeFactor;
import eu.agno3.runtime.security.principal.factors.PasswordFactor;
import eu.agno3.runtime.security.principal.factors.SSOFactor;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyRule.class )
public class AuthTypeRule implements PolicyRule {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#getPriority()
     */
    @Override
    public float getPriority () {
        return -1000.0f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#isFulfilledForAccess(eu.agno3.fileshare.service.config.PolicyConfiguration,
     *      eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext)
     */
    @Override
    public PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx ) {

        PrincipalCollection principals = ctx.getAuthSubject().getPrincipals();

        if ( principals == null ) {
            return new PolicyViolation("error"); //$NON-NLS-1$
        }

        Collection<AuthFactor> authFactors = principals.byType(AuthFactor.class);

        Set<AuthFactorType> types = EnumSet.noneOf(AuthFactorType.class);
        int extraFactors = 0;
        boolean containsHardware = false;
        for ( AuthFactor factor : authFactors ) {
            types.add(factor.getFactorType());
            if ( factor instanceof OneTimeFactor ) {
                OneTimeFactor otf = (OneTimeFactor) factor;
                if ( otf.isHardware() != null && otf.isHardware() ) {
                    containsHardware = true;
                }
            }
            else if ( factor instanceof CertificateFactor ) {
                CertificateFactor cf = (CertificateFactor) factor;
                if ( cf.getHardware() != null && cf.getHardware() ) {
                    containsHardware = true;
                }
            }
            else if ( factor instanceof SSOFactor ) {
                extraFactors += ( (SSOFactor) factor ).getCountAsFactors();
            }
        }

        if ( types.contains(AuthFactorType.TOKEN) && types.size() > 1 ) {
            // token is only used if it is the only auth type present
            types.remove(AuthFactorType.TOKEN);
        }

        if ( types.size() + extraFactors < policyConfig.getMinAuthFactors() ) {
            return new PolicyViolation("auth.minFactors", types.size(), policyConfig.getMinAuthFactors()); //$NON-NLS-1$
        }

        if ( policyConfig.isRequireHardwareFactor() && !containsHardware ) {
            return new PolicyViolation("auth.missingHardwareFactor"); //$NON-NLS-1$
        }

        return checkPasswordFactors(policyConfig, ctx, types);
    }


    /**
     * @param policyConfig
     * @param ctx
     * @param types
     * @return
     */
    private static PolicyViolation checkPasswordFactors ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx, Set<AuthFactorType> types ) {
        if ( types.contains(AuthFactorType.PASSWORD) && ( types.size() == 1 || policyConfig.isAlwaysCheckPasswordPolicy() ) ) {
            Collection<PasswordFactor> passwordFactors = ctx.getAuthSubject().getPrincipals().byType(PasswordFactor.class);
            for ( PasswordFactor pwFactor : passwordFactors ) {
                PolicyViolation violation = checkPasswordFactor(policyConfig, pwFactor);
                if ( violation != null ) {
                    return violation;
                }
            }
        }

        return null;
    }


    /**
     * @param policyConfig
     * @param factor
     */
    private static PolicyViolation checkPasswordFactor ( PolicyConfiguration policyConfig, PasswordFactor factor ) {
        if ( policyConfig.getMaxPasswordAge() != null
                && ( factor.getAge() == null || policyConfig.getMaxPasswordAge().isShorterThan(factor.getAge()) ) ) {
            return new PolicyViolation("auth.password.age", factor.getAge().getStandardDays(), policyConfig.getMaxPasswordAge().getStandardDays()); //$NON-NLS-1$
        }

        if ( policyConfig.getMinimumPasswordEntropy() > 0
                && ( factor.getEntropy() == null || factor.getEntropy() < policyConfig.getMinimumPasswordEntropy() ) ) {
            return new PolicyViolation("auth.password.entropy", factor.getEntropy(), policyConfig.getMinimumPasswordEntropy()); //$NON-NLS-1$
        }

        return null;
    }
}

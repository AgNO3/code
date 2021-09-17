/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2016 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class AuthenticatorsConfigValidator implements ObjectValidator<AuthenticatorsConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<AuthenticatorsConfig> getObjectType () {
        return AuthenticatorsConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, AuthenticatorsConfig obj ) {
        Set<String> foundIds = new HashSet<>();
        for ( AuthenticatorConfig authenticatorConfig : obj.getAuthenticators() ) {
            if ( !foundIds.add(authenticatorConfig.getRealm()) ) {
                ctx.addViolation(
                    "auth.collections.duplicateRealm", //$NON-NLS-1$
                    "authenticators", //$NON-NLS-1$
                    ViolationLevel.ERROR,
                    authenticatorConfig.getRealm());
            }
        }
    }

}

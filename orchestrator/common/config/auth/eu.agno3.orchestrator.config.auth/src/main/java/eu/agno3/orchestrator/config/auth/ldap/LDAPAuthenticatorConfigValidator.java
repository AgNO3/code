/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.config.web.SSLClientMode;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class LDAPAuthenticatorConfigValidator implements ObjectValidator<LDAPAuthenticatorConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<LDAPAuthenticatorConfig> getObjectType () {
        return LDAPAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, LDAPAuthenticatorConfig obj ) {
        if ( obj.getConnectionConfig().getSslClientMode() == SSLClientMode.DISABLE ) {
            ctx.addViolation(
                "auth.authenticator.ldap.requireTransportSecurity", //$NON-NLS-1$
                "connectionConfig.sslClientMode", //$NON-NLS-1$
                ViolationLevel.WARNING);
        }
    }

}

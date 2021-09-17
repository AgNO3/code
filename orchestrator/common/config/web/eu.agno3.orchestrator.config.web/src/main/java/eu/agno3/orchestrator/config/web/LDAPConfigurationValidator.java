/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class LDAPConfigurationValidator implements ObjectValidator<LDAPConfiguration> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<LDAPConfiguration> getObjectType () {
        return LDAPConfiguration.class;
    }

    private static final Set<String> INSECURE_SASL_MECH = new HashSet<>(Arrays.asList(
        "LOGIN", //$NON-NLS-1$
        "PLAIN")); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, LDAPConfiguration obj ) {

        if ( StringUtils.isBlank(obj.getSrvDomain()) && ( obj.getServers() == null || obj.getServers().isEmpty() ) ) {
            ctx.addViolation(
                "web.ldap.noServers", //$NON-NLS-1$
                "servers", //$NON-NLS-1$
                ViolationLevel.ERROR);
        }

        if ( isInsecureAuth(obj) && ( obj.getSslClientMode() == SSLClientMode.DISABLE || obj.getSslClientMode() == SSLClientMode.TRY_STARTTLS ) ) {
            ctx.addViolation(
                "web.ldap.insecureAuth", //$NON-NLS-1$
                "sslClientMode", //$NON-NLS-1$
                ViolationLevel.WARNING);
        }
    }


    /**
     * @param obj
     * @return
     */
    private static boolean isInsecureAuth ( LDAPConfiguration obj ) {
        return obj.getAuthType() == LDAPAuthType.SIMPLE
                || ( obj.getAuthType() == LDAPAuthType.SASL && INSECURE_SASL_MECH.contains(obj.getSaslMechanism()) );
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class SMTPConfigValidator implements ObjectValidator<SMTPConfiguration> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<SMTPConfiguration> getObjectType () {
        return SMTPConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, SMTPConfiguration obj ) {
        if ( obj.getSslClientMode() == SSLClientMode.DISABLE && ( obj.getAuthEnabled() != null && obj.getAuthEnabled() ) ) {
            ctx.addViolation(
                "web.smtp.insecureAuth", //$NON-NLS-1$
                "sslClientMode", //$NON-NLS-1$
                ViolationLevel.WARNING);
        }
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing.validation;


import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfiguration;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.config.web.SMTPConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class MailingValidator implements ObjectValidator<MailingConfiguration> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<MailingConfiguration> getObjectType () {
        return MailingConfiguration.class;
    }


    @Override
    public void validate ( ObjectValidationContext ctx, MailingConfiguration obj ) {
        if ( obj.getMailingEnabled() ) {
            SMTPConfiguration smtpCfg = obj.getSmtpConfiguration();
            URI serverUri = smtpCfg.getServerUri();
            if ( serverUri == null || StringUtils.isBlank(serverUri.getHost()) || serverUri.getPort() <= 0 ) {
                ctx.addViolation("hostconfig.mailing.noServer", ViolationLevel.ERROR); //$NON-NLS-1$
            }
        }
    }

}

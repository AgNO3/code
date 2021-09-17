/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime.validation;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class DateTimeValidator implements ObjectValidator<DateTimeConfiguration> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<DateTimeConfiguration> getObjectType () {
        return DateTimeConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, DateTimeConfiguration obj ) {
        validateNTP(ctx, obj);
    }


    /**
     * @param ctx
     * @param obj
     */
    private static void validateNTP ( ObjectValidationContext ctx, DateTimeConfiguration obj ) {
        if ( ctx.isAbstract() || obj.getNtpEnabled() == null || !obj.getNtpEnabled() ) {
            return;
        }

        if ( obj.getNtpServers() == null || obj.getNtpServers().isEmpty() ) {
            ctx.addViolation("hostconfig.datetime.ntpServers.atLeastOne", //$NON-NLS-1$
                "ntpServers", //$NON-NLS-1$
                ViolationLevel.ERROR);
        }
    }

}

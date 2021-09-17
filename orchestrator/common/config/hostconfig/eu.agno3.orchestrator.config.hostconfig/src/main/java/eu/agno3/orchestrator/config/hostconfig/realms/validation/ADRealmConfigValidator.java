/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.realms.validation;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.realms.ADRealmConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class ADRealmConfigValidator implements ObjectValidator<ADRealmConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<ADRealmConfig> getObjectType () {
        return ADRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, ADRealmConfig obj ) {
        KRBRealmConfigValidator.checkRealmDomain(ctx, obj);
    }

}

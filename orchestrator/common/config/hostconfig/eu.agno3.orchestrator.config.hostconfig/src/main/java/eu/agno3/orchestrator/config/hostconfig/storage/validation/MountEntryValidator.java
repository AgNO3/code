/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage.validation;


import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class MountEntryValidator implements ObjectValidator<LocalMountEntry> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<LocalMountEntry> getObjectType () {
        return LocalMountEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, LocalMountEntry obj ) {

        boolean hasLabel = !StringUtils.isBlank(obj.getMatchLabel());
        boolean hasUUID = obj.getMatchUuid() != null;

        if ( !hasLabel && !hasUUID ) {
            ctx.addViolation("hostconfig.storage.mount.noMatcher", ViolationLevel.ERROR); //$NON-NLS-1$
        }

    }

}

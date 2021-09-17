/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareNotificationConfigObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<FileshareNotificationConfig, FileshareNotificationConfigImpl> {

    /**
     * 
     */
    public FileshareNotificationConfigObjectTypeDescriptor () {
        super(FileshareNotificationConfig.class, FileshareNotificationConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareNotificationConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareNotificationConfig getGlobalDefaults () {
        FileshareNotificationConfigImpl nc = new FileshareNotificationConfigImpl();
        nc.setNotificationDisabled(true);
        nc.setExpirationNotificationPeriod(Duration.standardDays(14));
        nc.setDefaultSenderName("AgNO3 FileShield"); //$NON-NLS-1$
        return nc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareNotificationConfigMutable emptyInstance () {
        return new FileshareNotificationConfigImpl();
    }

}

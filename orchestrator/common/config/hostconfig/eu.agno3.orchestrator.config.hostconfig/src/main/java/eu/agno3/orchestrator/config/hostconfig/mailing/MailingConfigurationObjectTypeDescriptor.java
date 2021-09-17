/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.SMTPConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class MailingConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<MailingConfiguration, MailingConfigurationImpl> {

    /**
     * 
     */
    public MailingConfigurationObjectTypeDescriptor () {
        super(MailingConfiguration.class, MailingConfigurationImpl.class, HostConfigurationMessages.BASE_PACKAGE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull MailingConfiguration getGlobalDefaults () {
        MailingConfigurationImpl mc = new MailingConfigurationImpl();
        mc.setMailingEnabled(false);
        return mc;
    }


    /**
     * @return an empty instance
     */
    public static @NonNull MailingConfigurationMutable emptyInstance () {
        MailingConfigurationImpl mc = new MailingConfigurationImpl();
        mc.setSmtpConfiguration(SMTPConfigurationObjectTypeDescriptor.emptyInstance());
        return mc;
    }

}

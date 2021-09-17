/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class InterfaceEntryObjectTypeDescriptor extends AbstractObjectTypeDescriptor<InterfaceEntry, InterfaceEntryImpl> {

    /**
     * 
     */
    public InterfaceEntryObjectTypeDescriptor () {
        super(
            InterfaceEntry.class,
            InterfaceEntryImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig:network:interfaces"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull InterfaceEntry getGlobalDefaults () {
        InterfaceEntryImpl defaults = new InterfaceEntryImpl();
        defaults.setMediaType(MediaType.AUTO);
        defaults.setV4AddressConfigurationType(AddressConfigurationTypeV4.DHCP);
        defaults.setV6AddressConfigurationType(AddressConfigurationTypeV6.NONE);
        defaults.setMtu(1500);
        return defaults;
    }


    /**
     * @return empty instance
     */
    public static InterfaceEntryMutable emptyInstance () {
        return new InterfaceEntryImpl();
    }
}

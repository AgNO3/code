/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class InterfaceConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<InterfaceConfiguration, InterfaceConfigurationImpl> {

    /**
     * 
     */
    public InterfaceConfigurationObjectTypeDescriptor () {
        super(
            InterfaceConfiguration.class,
            InterfaceConfigurationImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig:network"); //$NON-NLS-1$
    }


    /**
     * @return empty instance
     */
    public static InterfaceConfigurationMutable emptyInstance () {
        return new InterfaceConfigurationImpl();
    }

}

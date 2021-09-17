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
public class NetworkConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<NetworkConfiguration, NetworkConfigurationImpl> {

    /**
     * 
     */
    public NetworkConfigurationObjectTypeDescriptor () {
        super(NetworkConfiguration.class, NetworkConfigurationImpl.class, HostConfigurationMessages.BASE_PACKAGE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull NetworkConfiguration getGlobalDefaults () {
        NetworkConfigurationImpl defaults = new NetworkConfigurationImpl();
        defaults.setIpv6Enabled(false);
        return defaults;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull NetworkConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull NetworkConfigurationMutable emptyInstance () {
        NetworkConfigurationImpl nc = new NetworkConfigurationImpl();
        nc.setInterfaceConfiguration(new InterfaceConfigurationImpl());
        nc.setRoutingConfiguration(RoutingConfigurationObjectTypeDescriptor.emptyInstance());
        return nc;
    }
}

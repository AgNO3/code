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
public class RoutingConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<RoutingConfiguration, RoutingConfigurationImpl> {

    /**
     * 
     */
    public RoutingConfigurationObjectTypeDescriptor () {
        super(
            RoutingConfiguration.class,
            RoutingConfigurationImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig:network"); //$NON-NLS-1$
    }


    @Override
    public @NonNull RoutingConfiguration getGlobalDefaults () {
        RoutingConfigurationImpl def = new RoutingConfigurationImpl();
        def.setAutoconfigureV4Routes(true);
        def.setAutoconfigureV6Routes(true);
        return def;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull RoutingConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull RoutingConfigurationMutable emptyInstance () {
        RoutingConfigurationImpl rc = new RoutingConfigurationImpl();
        rc.setDefaultRouteV4(StaticRouteEntryObjectTypeDescriptor.emptyInstance());
        rc.setDefaultRouteV6(StaticRouteEntryObjectTypeDescriptor.emptyInstance());
        return rc;
    }

}

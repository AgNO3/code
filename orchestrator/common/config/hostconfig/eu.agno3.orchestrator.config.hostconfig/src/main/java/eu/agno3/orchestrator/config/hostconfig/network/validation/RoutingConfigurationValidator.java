/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network.validation;


import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.network.InterfaceConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class RoutingConfigurationValidator implements ObjectValidator<RoutingConfiguration> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<RoutingConfiguration> getObjectType () {
        return RoutingConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, RoutingConfiguration obj ) {
        if ( ctx.isAbstract() ) {
            return;
        }
        Optional<NetworkConfiguration> netConfig = ctx.findParent(NetworkConfiguration.class);
        if ( !netConfig.isPresent() ) {
            return;
        }
        NetworkConfiguration nc = netConfig.get();
        InterfaceConfiguration ifConfig = nc.getInterfaceConfiguration();

        if ( obj.getDefaultRouteV4() != null ) {
            checkRouteEntry(ctx, "defaultRouteV4", obj.getDefaultRouteV4(), ifConfig, true, false); //$NON-NLS-1$
        }

        if ( obj.getDefaultRouteV6() != null ) {
            checkRouteEntry(ctx, "defaultRouteV6", obj.getDefaultRouteV6(), ifConfig, false, true); //$NON-NLS-1$
        }

        boolean allowV6 = nc.getIpv6Enabled() != null && nc.getIpv6Enabled();

        if ( obj.getStaticRoutes() != null ) {
            for ( StaticRouteEntry e : obj.getStaticRoutes() ) {
                checkRouteEntry(ctx, "staticRoutes", e, ifConfig, true, allowV6); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param ctx
     * @param obj
     * @param ifConfig
     */
    private static void checkRouteEntry ( ObjectValidationContext ctx, String path, StaticRouteEntry obj, InterfaceConfiguration ifConfig,
            boolean allowV4, boolean allowV6 ) {

        if ( obj.getTarget() != null ) {
            checkTargetSpec(ctx, path, obj.getTarget(), allowV4, allowV6);
        }

        if ( obj.getGateway() != null ) {
            checkGateway(ctx, path, obj.getGateway(), allowV4, allowV6);
        }

        if ( obj.getTarget() != null && obj.getGateway() != null ) {
            checkGatewayVsTarget(ctx, path, obj.getTarget(), obj.getGateway());
        }

        checkDeviceAlias(ctx, path, obj, ifConfig);
    }


    /**
     * @param ctx
     * @param target
     * @param gateway
     */
    private static void checkGatewayVsTarget ( ObjectValidationContext ctx, String path, NetworkSpecification target, NetworkAddress gateway ) {
        NetworkAddress targetAddr = target.getAddress();
        if ( targetAddr.getBitSize() != gateway.getBitSize() ) {
            ctx.addViolation("hostconfig.network.routing.route.typeMismatch", //$NON-NLS-1$
                path,
                ViolationLevel.ERROR,
                targetAddr.toString(),
                gateway.toString());
        }
    }


    /**
     * @param gateway
     * @param allowV4
     * @param allowV6
     */
    private static void checkGateway ( ObjectValidationContext ctx, String path, NetworkAddress gateway, boolean allowV4, boolean allowV6 ) {
        NetworkValidationUtil.checkAddressType(ctx, "hostconfig.network.routing.route.illegalGatewayType", //$NON-NLS-1$
            ViolationLevel.WARNING,
            path + ".gateway", //$NON-NLS-1$
            gateway,
            allowV4,
            allowV6,
            gateway.toString());
    }


    /**
     * @param target
     * @param allowV4
     * @param allowV6
     */
    private static void checkTargetSpec ( ObjectValidationContext ctx, String path, NetworkSpecification target, boolean allowV4, boolean allowV6 ) {
        NetworkValidationUtil.checkAddressType(ctx, "hostconfig.network.routing.route.illegalTargetType", //$NON-NLS-1$
            ViolationLevel.WARNING,
            path + ".target", //$NON-NLS-1$
            target.getAddress(),
            allowV4,
            allowV6,
            target.toString());
    }


    /**
     * @param ctx
     * @param path
     * @param obj
     * @param ifConfig
     */
    private static void checkDeviceAlias ( ObjectValidationContext ctx, String path, StaticRouteEntry obj, InterfaceConfiguration ifConfig ) {
        String searchAlias = obj.getDevice();
        if ( StringUtils.isBlank(searchAlias) ) {
            return;
        }

        boolean found = false;

        for ( InterfaceEntry e : ifConfig.getInterfaces() ) {
            if ( searchAlias.equals(e.getAlias()) ) {
                found = true;
                break;
            }
        }

        if ( !found ) {
            ctx.addViolation("hostconfig.network.routing.route.illegalDevice", //$NON-NLS-1$
                path + ".device", //$NON-NLS-1$
                ViolationLevel.ERROR,
                searchAlias);
        }
    }
}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network.validation;


import java.util.Optional;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public final class NetworkValidationUtil {

    /**
     * 
     */
    private NetworkValidationUtil () {}


    /**
     * @param ctx
     * @param msgKey
     * @param level
     * @param path
     * @param address
     * @param allowV4
     * @param allowV6
     * @param args
     */
    public static void checkAddressType ( ObjectValidationContext ctx, String msgKey, ViolationLevel level, String path, NetworkAddress address,
            boolean allowV4, boolean allowV6, Object... args ) {
        if ( ( !allowV4 && address instanceof IPv4Address ) && ( !allowV6 && address instanceof IPv6Address ) ) {
            ctx.addViolation(msgKey, path, level, args);
        }
    }


    /**
     * 
     * @param ctx
     * @param path
     * @param address
     */
    public static void checkAddressType ( ObjectValidationContext ctx, String path, NetworkAddress address ) {
        Optional<HostConfiguration> hc = ctx.findParent(HostConfiguration.class);

        boolean allowV4 = true;
        boolean allowV6 = true;

        if ( hc.isPresent() && hc.get().getNetworkConfiguration() != null ) {
            allowV6 = hc.get().getNetworkConfiguration().getIpv6Enabled() != null && hc.get().getNetworkConfiguration().getIpv6Enabled();
        }

        ViolationLevel level = ViolationLevel.ERROR;

        if ( ctx.isAbstract() ) {
            level = ViolationLevel.WARNING;
        }

        checkAddressType(ctx, "hostconfig.network.wrongAddressType", level, path, address, allowV4, allowV6, address.toString()); //$NON-NLS-1$
    }

}

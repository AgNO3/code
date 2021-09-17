/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network.validation;


import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV4;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV6;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class InterfaceEntryValidator implements ObjectValidator<InterfaceEntry> {

    /**
     * 
     */
    private static final String STATIC_ADDRESSES = "staticAddresses"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<InterfaceEntry> getObjectType () {
        return InterfaceEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, InterfaceEntry obj ) {

        if ( obj.getHardwareAddress() == null && obj.getInterfaceIndex() == null ) {
            ctx.addViolation("hostconfig.network.interface.noMatcher", ViolationLevel.ERROR); //$NON-NLS-1$
        }

        checkIPv6(ctx, obj);
        checkAddresses(ctx, obj);
    }


    /**
     * @param ctx
     * @param obj
     */
    private static void checkIPv6 ( ObjectValidationContext ctx, InterfaceEntry obj ) {
        Optional<NetworkConfiguration> netConf = ctx.findParent(NetworkConfiguration.class);

        if ( netConf.isPresent() && netConf.get().getIpv6Enabled() != null ) {
            boolean v6enabled = netConf.get().getIpv6Enabled();
            if ( !v6enabled
                    && ( obj.getV6AddressConfigurationType() != null && obj.getV6AddressConfigurationType() != AddressConfigurationTypeV6.NONE ) ) {
                ctx.addViolation("hostconfig.network.interface.v6Disabled", //$NON-NLS-1$
                    "v6AddressConfigurationType", //$NON-NLS-1$ 
                    ViolationLevel.WARNING);
            }
        }
    }


    /**
     * @param ctx
     * @param obj
     */
    private static void checkAddresses ( ObjectValidationContext ctx, InterfaceEntry obj ) {
        boolean staticV4 = obj.getV4AddressConfigurationType() != null && obj.getV4AddressConfigurationType() == AddressConfigurationTypeV4.STATIC;
        boolean staticV6 = obj.getV6AddressConfigurationType() != null && obj.getV6AddressConfigurationType() == AddressConfigurationTypeV6.STATIC;
        boolean foundStaticV4 = false;
        boolean foundStaticV6 = false;

        if ( obj.getStaticAddresses() != null ) {
            for ( NetworkSpecification spec : obj.getStaticAddresses() ) {
                foundStaticV4 = checkV4Address(ctx, staticV4, foundStaticV4, spec);
                foundStaticV6 = checkV6Address(ctx, staticV6, foundStaticV6, spec);
            }
        }

        checkStaticAddressesPresent(ctx, staticV4, staticV6, foundStaticV4, foundStaticV6);
    }


    /**
     * @param ctx
     * @param staticV6
     * @param foundStaticV6
     * @param spec
     * @return
     */
    private static boolean checkV6Address ( ObjectValidationContext ctx, boolean staticV6, boolean foundStaticV6, NetworkSpecification spec ) {
        if ( !staticV6 && spec.getAddress() instanceof IPv6Address ) {
            ctx.addViolation("hostconfig.network.interface.noStaticV6", //$NON-NLS-1$
                STATIC_ADDRESSES,
                ViolationLevel.SUGGESTION,
                spec.toString());
        }
        else if ( spec.getAddress() instanceof IPv6Address ) {
            return true;
        }
        return foundStaticV6;
    }


    /**
     * @param ctx
     * @param staticV4
     * @param foundStaticV4
     * @param spec
     * @return
     */
    private static boolean checkV4Address ( ObjectValidationContext ctx, boolean staticV4, boolean foundStaticV4, NetworkSpecification spec ) {
        if ( !staticV4 && spec.getAddress() instanceof IPv4Address ) {
            ctx.addViolation("hostconfig.network.interface.noStaticV4", //$NON-NLS-1$
                STATIC_ADDRESSES,
                ViolationLevel.SUGGESTION,
                spec.toString());
        }
        else if ( spec.getAddress() instanceof IPv4Address ) {
            return true;
        }
        return foundStaticV4;
    }


    /**
     * @param ctx
     * @param staticV4
     * @param staticV6
     * @param foundStaticV4
     * @param foundStaticV6
     */
    private static void checkStaticAddressesPresent ( ObjectValidationContext ctx, boolean staticV4, boolean staticV6, boolean foundStaticV4,
            boolean foundStaticV6 ) {
        if ( !ctx.isAbstract() ) {
            if ( staticV4 && !foundStaticV4 ) {
                ctx.addViolation("hostconfig.network.interface.noV4Address", //$NON-NLS-1$
                    STATIC_ADDRESSES,
                    ViolationLevel.SUGGESTION);
            }

            if ( staticV6 && !foundStaticV6 ) {
                ctx.addViolation("hostconfig.network.interface.noV6Address", //$NON-NLS-1$
                    STATIC_ADDRESSES,
                    ViolationLevel.SUGGESTION);
            }
        }
    }
}

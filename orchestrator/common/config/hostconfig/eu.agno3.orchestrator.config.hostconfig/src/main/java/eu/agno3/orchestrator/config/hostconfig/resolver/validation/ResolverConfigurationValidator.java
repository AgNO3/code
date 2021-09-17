/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver.validation;


import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.validation.NetworkValidationUtil;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfiguration;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class ResolverConfigurationValidator implements ObjectValidator<ResolverConfiguration> {

    /**
     * 
     */
    private static final String NAMESERVERS = "nameservers"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<ResolverConfiguration> getObjectType () {
        return ResolverConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, ResolverConfiguration obj ) {

        Optional<HostConfiguration> hc = ctx.findParent(HostConfiguration.class);
        if ( hc.isPresent() ) {
            checkAddressType(ctx, obj);
        }

        checkNameserversPresent(ctx, obj);

    }


    /**
     * @param ctx
     * @param obj
     * @param hc
     */
    private static void checkAddressType ( ObjectValidationContext ctx, ResolverConfiguration obj ) {
        if ( obj.getNameservers() != null ) {
            for ( NetworkAddress nsAddress : obj.getNameservers() ) {
                NetworkValidationUtil.checkAddressType(ctx, NAMESERVERS, nsAddress);
            }
        }
    }


    /**
     * @param ctx
     * @param obj
     */
    private static void checkNameserversPresent ( ObjectValidationContext ctx, ResolverConfiguration obj ) {
        if ( !ctx.isAbstract() && isStaticDNSConfig(obj) && noDNSServersAvailable(obj) ) {
            ctx.addViolation("hostconfig.resolver.nameservers.missing", //$NON-NLS-1$
                NAMESERVERS,
                ViolationLevel.ERROR);
        }
    }


    private static boolean noDNSServersAvailable ( ResolverConfiguration obj ) {
        return obj.getNameservers() == null || obj.getNameservers().isEmpty();
    }


    private static boolean isStaticDNSConfig ( ResolverConfiguration obj ) {
        return obj.getAutoconfigureDns() != null && !obj.getAutoconfigureDns();
    }
}

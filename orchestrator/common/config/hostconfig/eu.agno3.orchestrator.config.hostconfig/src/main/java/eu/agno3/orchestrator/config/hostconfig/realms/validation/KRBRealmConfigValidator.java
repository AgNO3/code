/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.realms.validation;


import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.config.realms.KRBRealmConfig;
import eu.agno3.orchestrator.config.realms.RealmConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectValidator.class )
public class KRBRealmConfigValidator implements ObjectValidator<KRBRealmConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#getObjectType()
     */
    @Override
    public Class<KRBRealmConfig> getObjectType () {
        return KRBRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator#validate(eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void validate ( ObjectValidationContext ctx, KRBRealmConfig obj ) {
        checkRealmDomain(ctx, obj);
    }


    /**
     * @param ctx
     * @param obj
     */
    static void checkRealmDomain ( ObjectValidationContext ctx, RealmConfig obj ) {
        if ( StringUtils.isBlank(obj.getOverrideLocalHostname()) ) {
            Optional<HostIdentification> hi = getHostIdentification(ctx);
            if ( !hi.isPresent() ) {
                return;
            }
            HostIdentification hid = hi.get();

            if ( StringUtils.isBlank(hid.getDomainName()) || StringUtils.isBlank(obj.getRealmName()) ) {
                return;
            }

            String hostDomain = hid.getDomainName().toLowerCase(Locale.ROOT);
            String realmDomain = obj.getRealmName().toLowerCase(Locale.ROOT);

            if ( hostDomain.equals(realmDomain) ) {
                // ok, match
                return;
            }

            if ( hostDomain.endsWith("." + realmDomain) ) { //$NON-NLS-1$
                // ok, in most cases, host domain is subdomain of realm
                return;
            }

            ctx.addViolation(
                "realms.realmDomainMismatch", //$NON-NLS-1$
                "overrideLocalHostname", //$NON-NLS-1$
                ViolationLevel.WARNING,
                hostDomain,
                realmDomain);

        }
    }


    /**
     * @param ctx
     * @return
     */
    static Optional<HostIdentification> getHostIdentification ( ObjectValidationContext ctx ) {
        Optional<HostConfiguration> p = ctx.findParent(HostConfiguration.class);
        if ( p.isPresent() ) {
            return Optional.of(p.get().getHostIdentification());
        }
        Optional<HostConfiguration> hc = ctx.findContext(HostConfiguration.class, HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE);
        if ( hc.isPresent() ) {
            return Optional.of(hc.get().getHostIdentification());
        }
        return Optional.empty();
    }

}

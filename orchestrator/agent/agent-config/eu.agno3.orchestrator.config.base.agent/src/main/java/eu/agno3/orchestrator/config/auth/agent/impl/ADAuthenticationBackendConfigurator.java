/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent.impl;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
@Component ( service = AuthenticationBackendConfigurator.class )
public class ADAuthenticationBackendConfigurator extends BaseKerberosAuthenticatorConfigurator
        implements AuthenticationBackendConfigurator<ADAuthenticatorConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.impl.BaseKerberosAuthenticatorConfigurator#setKerberosConfigUtil(eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtilImpl)
     */
    @Override
    @Reference
    protected synchronized void setKerberosConfigUtil ( KerberosConfigUtil rm ) {
        super.setKerberosConfigUtil(rm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.impl.BaseKerberosAuthenticatorConfigurator#unsetKerberosConfigUtil(eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtilImpl)
     */
    @Override
    protected synchronized void unsetKerberosConfigUtil ( KerberosConfigUtil rm ) {
        super.unsetKerberosConfigUtil(rm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator#getConfigType()
     */
    @Override
    public Class<ADAuthenticatorConfig> getConfigType () {
        return ADAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobBuilderException
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator#configure(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void configure ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, ADAuthenticatorConfig ac )
            throws JobBuilderException, UnitInitializationFailedException, ServiceManagementException, InvalidParameterException {
        try {
            RealmManager rm = this.getKerberosConfigUtil().getRealmManager(ac.getDomain());
            if ( rm.getType() != RealmType.AD ) {
                throw new JobBuilderException("Realm is not an Active Directory domain"); //$NON-NLS-1$
            }
            getKerberosConfigUtil().checkJoin(rm);
            // both keytab principal null means check host credentials
            getKerberosConfigUtil().ensureInitiatorCredentials(b, ctx, rm, null, null);
            getKerberosConfigUtil().ensureAcceptorCredentials(b, ctx, rm, ac.getKeytab(), ac.getServiceName(), "HTTP");
        }
        catch (
            KerberosException |
            ADException |
            UnixAccountException e ) {
            throw new JobBuilderException("Failed to setup ad domain " + ac.getDomain(), e);
        }

        PropertyConfigBuilder cfg = PropertyConfigBuilder.get();
        cfg.p("KerberosRealm.target", FilterBuilder.get().eq("instanceId", ac.getDomain()).toString());

        cfg.p("acceptOnlyLocalDomains", ac.getAcceptOnlyLocal());
        cfg.p("requireDomainUserGroup", ac.getRequireDomainUserGroup());
        cfg.p("rejectNonAD", ac.getRejectNonADPrincipals());
        cfg.p("sendNTLMChallenge", ac.getSendNTLMChallenge());
        cfg.p("acceptNTLMFallback", ac.getAcceptNTLMFallback());
        cfg.p("disablePACs", ac.getDisablePACs());
        cfg.p("disablePACValidation", ac.getDisablePACValidation());

        cfg.p("acceptDomains", ac.getAcceptDomainSids());
        cfg.p("rejectDomains", ac.getRejectDomainSids());

        cfg.p("requireSid", ac.getRequiredSids());
        cfg.p("rejectSids", ac.getRejectSids());

        Map<String, Set<String>> sidRoles = new LinkedHashMap<>();
        for ( StaticRoleMapEntry staticRoleMapEntry : ac.getSidRoles() ) {
            sidRoles.put(staticRoleMapEntry.getInstance(), staticRoleMapEntry.getAddRoles());
        }
        cfg.pmultiValueMap("sidRoles", sidRoles);

        cfg.p("keytab", ac.getKeytab());
        cfg.p("rejectPrincipals", ac.getRejectPrincipalPatterns());
        cfg.p("acceptPrincipals", ac.getAcceptPrincipalPatterns());
        cfg.p("alwaysAddRoles", ac.getAlwaysAddRoles());
        cfg.p("service", ac.getServiceName());

        Map<String, Set<String>> princPatterns = new HashMap<>();
        for ( PatternRoleMapEntry pre : ac.getPrincipalAddRoles() ) {
            princPatterns.put(pre.getPattern(), pre.getAddRoles());
        }
        cfg.pmultiValueMap("roleAddPatterns", princPatterns);
        cfg.p("authRealmName", ac.getRealm());
        ctx.factory("auth.ad", ac.getRealm(), cfg);
    }

}

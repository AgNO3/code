/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.AuthenticatorsConfig;
import eu.agno3.orchestrator.config.auth.LoginRateLimitConfig;
import eu.agno3.orchestrator.config.auth.PasswordPolicyConfig;
import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfig;
import eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator;
import eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = AuthenticatorConfigurator.class )
public class AuthenticatorConfiguratorImpl implements AuthenticatorConfigurator {

    private Map<String, AuthenticationBackendConfigurator<?>> backends = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindAuthenticationBackendConfigurator ( AuthenticationBackendConfigurator<?> abc ) {
        this.backends.put(abc.getConfigType().getName(), abc);
    }


    protected synchronized void unbindAuthenticationBackendConfigurator ( AuthenticationBackendConfigurator<?> abc ) {
        this.backends.remove(abc.getConfigType().getName(), abc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator#setupAuthenticator(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    public <T extends AuthenticatorConfig> void setupAuthenticator ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, T ac )
            throws JobBuilderException, InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        @SuppressWarnings ( "unchecked" )
        AuthenticationBackendConfigurator<T> backend = (AuthenticationBackendConfigurator<T>) this.backends.get(ac.getType().getName());
        if ( backend == null ) {
            throw new JobBuilderException("Unknown authenticator type " + ac.getType().getName()); //$NON-NLS-1$
        }
        backend.configure(b, ctx, ac);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator#setupAuthenticators(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.config.auth.AuthenticatorsConfig)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void setupAuthenticators ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, AuthenticatorsConfig acs )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException {
        if ( acs.getEnableLocalAuth() ) {
            ctx.factory(
                "auth.db",
                "main",
                PropertyConfigBuilder.get().p("AuthDataSource.target", FilterBuilder.get().eq("dataSourceName", "auth").toString()));
        }

        ctx.instance("login", PropertyConfigBuilder.get().p("allowInsecureLogins", acs.getAllowInsecureAuth()));

        PasswordPolicyConfig pp = acs.getPasswordPolicy();

        ctx.instance(
            "password.policy",
            PropertyConfigBuilder.get().p("minEntropy", pp.getEntropyLowerLimit())
                    .p("maxPasswordAge", pp.getEnableAgeCheck() ? pp.getMaximumPasswordAge() : null).p("ignoreUnknownAge", pp.getIgnoreUnknownAge()));

        LoginRateLimitConfig lrl = acs.getLoginRateLimit();
        ctx.instance(
            "security.loginRate",
            PropertyConfigBuilder.get().p("interval", lrl.getCleanInterval()).p("laxSourceCheck", !lrl.getDisableLaxSourceCheck())
                    .p("enableGlobalDelay", !lrl.getDisableGlobalDelay()).p("disableUserLockout", lrl.getDisableUserLockout()));

        for ( AuthenticatorConfig ac : acs.getAuthenticators() ) {
            setupAuthenticator(b, ctx, ac);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticatorConfigurator#setupRoles(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.config.auth.StaticRolesConfig)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void setupRoles ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, StaticRolesConfig rcs )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        Set<String> allRoles = new HashSet<>();
        for ( RoleConfig rc : rcs.getRoles() ) {

            p.p(rc.getRoleId() + "_perms", rc.getPermissions());
            p.p(rc.getRoleId() + "_list", rc.getHidden() == null || !rc.getHidden());

            for ( Entry<Locale, String> t : rc.getTitles().entrySet() ) {
                if ( Locale.ROOT.equals(t.getKey()) ) {
                    p.p(rc.getRoleId() + "_title", t.getValue());
                }
                else {
                    p.p(rc.getRoleId() + "_title_" + t.getKey().toLanguageTag(), t.getValue());
                }
            }

            for ( Entry<Locale, String> t : rc.getTitles().entrySet() ) {
                if ( Locale.ROOT.equals(t.getKey()) ) {
                    p.p(rc.getRoleId() + "_description", t.getValue());
                }
                else {
                    p.p(rc.getRoleId() + "_description_" + t.getKey().toLanguageTag(), t.getValue());
                }
            }
            allRoles.add(rc.getRoleId());
        }

        p.p("roles", allRoles);

        ctx.instance("roles", p);

    }

}

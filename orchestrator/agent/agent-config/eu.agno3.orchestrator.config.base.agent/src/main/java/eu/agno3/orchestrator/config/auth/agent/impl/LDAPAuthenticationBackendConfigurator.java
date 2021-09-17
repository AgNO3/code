/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.web.LDAPServerType;
import eu.agno3.orchestrator.config.web.agent.LDAPConfigUtil;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
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
@Component ( service = AuthenticationBackendConfigurator.class )
public class LDAPAuthenticationBackendConfigurator implements AuthenticationBackendConfigurator<LDAPAuthenticatorConfig> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator#getConfigType()
     */
    @Override
    public Class<LDAPAuthenticatorConfig> getConfigType () {
        return LDAPAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     * 
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
    public void configure ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, LDAPAuthenticatorConfig config )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        String ldapConnName = "auth-" + config.getRealm();
        LDAPConfigUtil.configureConnection(b, ctx, ldapConnName, config.getConnectionConfig());
        PropertyConfigBuilder cfg = PropertyConfigBuilder.get();
        cfg.p("LDAPClientFactory.target", FilterBuilder.get().eq("instanceId", ldapConnName).toString());

        if ( config.getConnectionConfig().getServerType() == LDAPServerType.AD ) {
            LDAPConfigUtil.makeADAuthSchemaConfig(b, ctx, config.getSchemaConfig(), cfg);
        }
        else {
            LDAPConfigUtil.makeLDAPAuthSchemaConfig(b, ctx, config.getSchemaConfig(), cfg);
        }

        cfg.p("enforcePasswordPolicy", config.getEnforcePasswordPolicy());
        cfg.p("enforcePasswordPolicyOnChange", config.getEnforcePasswordPolicyOnChange());

        cfg.p("addGroupNameAsRole", config.getAddGroupNameAsRole());
        cfg.p("alwaysAddRoles", config.getAlwaysAddRoles());

        Map<String, Set<String>> staticMap = new HashMap<>();
        for ( StaticRoleMapEntry me : config.getStaticRoleMappings() ) {
            staticMap.put(me.getInstance(), me.getAddRoles());
        }
        cfg.pmultiValueMap("staticRoleMappings", staticMap);

        Map<String, Set<String>> patternMap = new HashMap<>();
        for ( PatternRoleMapEntry me : config.getPatternRoleMappings() ) {
            patternMap.put(me.getPattern(), me.getAddRoles());
        }
        cfg.pmultiValueMap("patternRoleMappings", patternMap);

        if ( !config.getEnableSynchronization() ) {
            cfg.p("provideUserDetails", true);
        }

        ctx.factory("auth.ldap", config.getRealm(), cfg);

    }
}

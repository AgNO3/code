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
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator;
import eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfig;
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


/**
 * @author mbechler
 *
 */
@Component ( service = AuthenticationBackendConfigurator.class )
public class KRBAuthenticationBackendConfigurator extends BaseKerberosAuthenticatorConfigurator
        implements AuthenticationBackendConfigurator<KerberosAuthenticatorConfig> {

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
    public Class<KerberosAuthenticatorConfig> getConfigType () {
        return KerberosAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     *
     * @see eu.agno3.orchestrator.config.auth.agent.AuthenticationBackendConfigurator#configure(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void configure ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, KerberosAuthenticatorConfig ac )
            throws UnitInitializationFailedException, InvalidParameterException, ServiceManagementException, JobBuilderException {

        try {
            RealmManager rm = getKerberosConfigUtil().getRealmManager(ac.getKerberosRealm());
            getKerberosConfigUtil().ensureAcceptorCredentials(b, ctx, rm, ac.getKeytabAlias(), ac.getServiceName(), "HTTP");
        }
        catch (
            KerberosException |
            UnixAccountException |
            ADException e ) {
            throw new JobBuilderException("Failed to setup kerberos realm " + ac.getKerberosRealm(), e);
        }

        PropertyConfigBuilder cfg = PropertyConfigBuilder.get();
        cfg.p("authRealmName", ac.getRealm());

        cfg.p("KerberosRealm.target", FilterBuilder.get().eq("instanceId", ac.getKerberosRealm()).toString());

        cfg.p("allowPasswordFallback", ac.getAllowPasswordFallback());
        cfg.p("keytab", ac.getKeytabAlias());
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
        ctx.factory("auth.spnego", ac.getRealm(), cfg);
    }
}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp;


import javax.inject.Inject;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.mgt.AuthorizingSecurityManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.security.DynamicModularRealmAuthenticator;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.web.SecurityHeadersFilterConfig;
import eu.agno3.runtime.security.web.filter.SecurityHeadersFilter;
import eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
public class SecurityInitializerImpl extends AbstractModularRealmSecurityInitializerImpl {

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private DynamicModularRealmAuthenticator modAuth;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private DynamicModularRealmAuthorizer modAuthz;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private SecurityHeadersFilterConfig hdrConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private UADetector uaDetector;

    @Inject
    private WebLoginConfig webLoginConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#setupFilterChains(eu.agno3.runtime.security.web.login.WebLoginConfig,
     *      org.apache.shiro.web.filter.mgt.FilterChainManager, org.apache.shiro.mgt.AuthorizingSecurityManager)
     */
    @Override
    protected void setupFilterChains ( WebLoginConfig config, FilterChainManager fcm, AuthorizingSecurityManager asm ) {
        super.setupFilterChains(config, fcm, asm);

        SecurityHeadersFilter hdrFilter = new SecurityHeadersFilter(this.uaDetector, this.hdrConfig);
        for ( String chainName : fcm.getChainNames() ) {
            NamedFilterList chain = fcm.getChain(chainName);
            hdrFilter.processPathConfig(chainName, null);
            chain.add(0, hdrFilter);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getAuthRealm()
     */
    @Override
    protected Authenticator getAuthRealm () {
        return this.modAuth;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getAuthzRealm()
     */
    @Override
    protected Authorizer getAuthzRealm () {
        return this.modAuthz;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getWebLoginConfig()
     */
    @Override
    protected WebLoginConfig getWebLoginConfig () {
        return this.webLoginConfig;
    }

}

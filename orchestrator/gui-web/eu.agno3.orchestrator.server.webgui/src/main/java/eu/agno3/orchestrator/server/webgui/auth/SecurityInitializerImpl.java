/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.runtime.http.service.HttpServiceInfo;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.security.cas.client.AbstractCasRealm;
import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;
import eu.agno3.runtime.security.cas.client.CasAuthenticationFilter;
import eu.agno3.runtime.security.cas.client.CasSingleLogoutFilter;
import eu.agno3.runtime.security.web.SecurityHeadersFilterConfig;
import eu.agno3.runtime.security.web.SecurityInitializer;
import eu.agno3.runtime.security.web.filter.SecurityHeadersFilter;
import eu.agno3.runtime.xml.XmlParserFactory;


/**
 * @author mbechler
 *
 */
public class SecurityInitializerImpl implements SecurityInitializer {

    private static final String LOGIN_RETURN_PATTERN = "/login-return"; //$NON-NLS-1$
    private static final String ALL_PATTERN = "/**"; //$NON-NLS-1$
    private static final String PUSH_PATTERN = "/primepush/**"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SecurityInitializerImpl.class);

    @Inject
    private CasRealmFactory casRealm;

    @Inject
    @OsgiService ( timeout = 400, dynamic = true )
    private XmlParserFactory xmlParserFactory;

    @Inject
    @OsgiService ( timeout = 400, dynamic = true )
    private GuiConfig guiConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private SecurityHeadersFilterConfig hdrConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private UADetector uaDetector;

    @Inject
    @OsgiService ( timeout = 400, dynamic = true )
    private HttpServiceInfo httpServiceInfo;


    @Override
    public void initWebEnvironment ( WebEnvironment environment ) {
        org.apache.shiro.mgt.SecurityManager manager = environment.getSecurityManager();
        log.debug("Setting up webgui security realm"); //$NON-NLS-1$

        if ( ! ( manager instanceof DefaultSecurityManager ) ) {
            log.error("Security manager is not a AuthenticatingSecurityManager"); //$NON-NLS-1$
            return;
        }

        FilterChainManager fcm = getFilterChainManager(environment);

        CasAuthConfiguration authConfig;
        try {
            authConfig = this.makeAuthConfig(this.guiConfig, environment.getServletContext());
        }
        catch (
            ServletException |
            URISyntaxException e ) {
            log.error("Failed to configure authentication", e); //$NON-NLS-1$
            NamedFilterList defaultChain = fcm.getChain(ALL_PATTERN);
            if ( defaultChain == null ) {
                log.warn("default chain is null"); //$NON-NLS-1$
                return;
            }

            PermissionsAuthorizationFilter perm = new PermissionsAuthorizationFilter();
            perm.setName("invalidConfigFilter"); //$NON-NLS-1$
            perm.processPathConfig(ALL_PATTERN, "invalid"); //$NON-NLS-1$
            perm.setLoginUrl("/error/error.xhtml"); //$NON-NLS-1$
            defaultChain.add(0, perm);
            return;
        }

        AbstractCasRealm realm = setupSecurityManager(manager, authConfig);

        setupCasProxyFilter(realm, fcm);
        setupLoginFilters(realm, fcm);
        setupPushFilters(fcm);
        setupDefaultAuthFilters(realm, fcm, authConfig);

        SecurityHeadersFilter hdrFilter = new SecurityHeadersFilter(this.uaDetector, this.hdrConfig);
        for ( String chainName : fcm.getChainNames() ) {
            NamedFilterList chain = fcm.getChain(chainName);
            hdrFilter.processPathConfig(chainName, null);
            chain.add(0, hdrFilter);
        }
    }


    /**
     * @param manager
     * @param servletContext
     * @return
     * @throws ServletException
     */
    protected AbstractCasRealm setupSecurityManager ( org.apache.shiro.mgt.SecurityManager manager, CasAuthConfiguration authConfig ) {
        DefaultSecurityManager sm = (DefaultSecurityManager) manager;
        AbstractCasRealm realm = this.casRealm.createRealm(authConfig);
        sm.setRealm(realm);
        return realm;
    }


    /**
     * @param servletContext
     * @param cfg
     * @return
     * @throws ServletException
     * @throws URISyntaxException
     */
    private CasAuthConfiguration makeAuthConfig ( GuiConfig cfg, ServletContext servletContext ) throws ServletException, URISyntaxException {
        if ( cfg.getAuthServerURL() == null ) {
            throw new ServletException("No authentication server configured"); //$NON-NLS-1$
        }

        URL contextBaseUrl = this.httpServiceInfo.getContextBaseUrl("web", servletContext, null); //$NON-NLS-1$
        return new WebGuiAuthConfigurationImpl(cfg, contextBaseUrl);
    }


    /**
     * @param environment
     * @return
     */
    protected FilterChainManager getFilterChainManager ( WebEnvironment environment ) {
        PathMatchingFilterChainResolver fcr = (PathMatchingFilterChainResolver) environment.getFilterChainResolver();
        return fcr.getFilterChainManager();
    }


    /**
     * @param realm
     * @param fcm
     */
    protected void setupCasProxyFilter ( AbstractCasRealm realm, FilterChainManager fcm ) {
        NamedFilterList proxyChain = fcm.getChain("/login-proxy"); //$NON-NLS-1$
        if ( proxyChain == null ) {
            log.warn("login proxy chain is null"); //$NON-NLS-1$
            return;
        }
        proxyChain.add(realm.getProxyReturnFilter());
    }


    /**
     * @param realm
     * @param fcm
     */
    private static void setupPushFilters ( FilterChainManager fcm ) {
        NamedFilterList pushChain = fcm.getChain(PUSH_PATTERN);
        if ( pushChain == null ) {
            log.warn("push chain is null"); //$NON-NLS-1$
            return;
        }

        PushAuthorizationFilter pushFilter = new PushAuthorizationFilter();
        pushFilter.setName("pushFilter"); //$NON-NLS-1$
        pushFilter.processPathConfig(PUSH_PATTERN, null);
        pushChain.add(pushFilter);
    }


    /**
     * @param fcm
     */
    protected void setupDefaultAuthFilters ( AbstractCasRealm realm, FilterChainManager fcm, CasAuthConfiguration authConfig ) {
        NamedFilterList defaultChain = fcm.getChain(ALL_PATTERN);
        if ( defaultChain == null ) {
            log.warn("default chain is null"); //$NON-NLS-1$
            return;
        }

        PassThruAuthenticationFilter defaultFilter = new CustomPassThruAuthenticationFilter(authConfig);
        defaultFilter.setName("authFilter"); //$NON-NLS-1$
        defaultFilter.processPathConfig(ALL_PATTERN, null);
        defaultChain.add(0, defaultFilter);
    }


    /**
     * @param fcm
     */
    protected void setupLoginFilters ( AbstractCasRealm realm, FilterChainManager fcm ) {
        NamedFilterList loginChain = fcm.getChain(LOGIN_RETURN_PATTERN);
        if ( loginChain == null ) {
            log.warn("login return chain is null"); //$NON-NLS-1$
            return;
        }
        CasAuthenticationFilter casFilter = new CasAuthenticationFilter();
        casFilter.setFailureUrl("/error/access.xhtml"); //$NON-NLS-1$
        casFilter.setSuccessUrl("/index.xhtml"); //$NON-NLS-1$
        casFilter.setName("casFilter"); //$NON-NLS-1$
        casFilter.processPathConfig(LOGIN_RETURN_PATTERN, null);
        loginChain.add(0, casFilter);
        loginChain.add(0, new CasSingleLogoutFilter(this.xmlParserFactory, realm));
    }
}

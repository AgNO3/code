/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.web.gui.init;


import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.mgt.AuthorizingSecurityManager;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

import eu.agno3.runtime.security.terms.TermsFilter;
import eu.agno3.runtime.security.web.SecurityInitializer;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
public abstract class AbstractModularRealmSecurityInitializerImpl implements SecurityInitializer {

    private static final Logger log = Logger.getLogger(AbstractModularRealmSecurityInitializerImpl.class);

    @Inject
    private DelegatingAuthFilter authFilter;

    @Inject
    private TermsFilter termsFilter;


    @Override
    public void initWebEnvironment ( WebEnvironment environment ) {
        org.apache.shiro.mgt.SecurityManager manager = environment.getSecurityManager();
        log.debug("Setting up security realm"); //$NON-NLS-1$

        if ( ! ( manager instanceof AuthorizingSecurityManager ) ) {
            log.error("Security manager is not a AuthenticatingSecurityManager"); //$NON-NLS-1$
            return;
        }

        AuthorizingSecurityManager asm = (AuthorizingSecurityManager) manager;
        this.setupSecurityManager(asm);

        FilterChainManager fcm = getFilterChainManager(environment);
        this.setupFilterChains(getWebLoginConfig(), fcm, asm);
    }


    /**
     * @param fcm
     * @param asm
     */
    protected void setupFilterChains ( WebLoginConfig config, FilterChainManager fcm, AuthorizingSecurityManager asm ) {
        String authPattern = config.getAuthBasePath() + "**"; //$NON-NLS-1$
        NamedFilterList chain = fcm.getChain(authPattern);

        if ( log.isDebugEnabled() ) {
            log.debug("Binding auth filter to " + authPattern); //$NON-NLS-1$
        }

        if ( chain == null ) {
            throw new IllegalStateException("Auth chain does not exist: " + authPattern); //$NON-NLS-1$
        }

        DelegatingAuthFilter f = this.authFilter;
        f.setName("Delegating external authentication filter"); //$NON-NLS-1$
        f.processPathConfig(authPattern, null);
        chain.add(0, f);

        TermsFilter tf = getTermsFilter();
        if ( tf != null ) {
            String termsPath = "/terms/**"; //$NON-NLS-1$
            NamedFilterList termsChain = fcm.getChain(termsPath);
            if ( termsChain != null ) {
                termsChain.add(tf);
            }
        }
    }


    /**
     * @return the termsFilter
     */
    protected TermsFilter getTermsFilter () {
        return this.termsFilter;
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
     * @param asm
     */
    protected void setupSecurityManager ( AuthorizingSecurityManager asm ) {
        asm.setAuthenticator(this.getAuthRealm());
        asm.setAuthorizer(this.getAuthzRealm());
    }


    /**
     * @return the authorization realm to use
     */
    protected abstract Authorizer getAuthzRealm ();


    /**
     * @return the authentication realm to use
     */
    protected abstract Authenticator getAuthRealm ();


    /**
     * @return the web login config
     */
    protected abstract WebLoginConfig getWebLoginConfig ();

}

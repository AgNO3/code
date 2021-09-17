/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp.login;


import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
public class WebLoginConfigImpl implements WebLoginConfig {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#getSuccessUrl()
     */
    @Override
    public String getSuccessUrl () {
        return "/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#getPreferredRealmCookieName()
     */
    @Override
    public String getPreferredRealmCookieName () {
        return "preferred_auth_realm"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#getSavedUsernameCookieName()
     */
    @Override
    public String getSavedUsernameCookieName () {
        return "saved_username"; //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#getAuthBasePath()
     */
    @Override
    public String getAuthBasePath () {
        return "/loginFlow/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#isDoRedirectToOrigUrl()
     */
    @Override
    public boolean isDoRedirectToOrigUrl () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginConfig#getLogOutUrl()
     */
    @Override
    public String getLogOutUrl () {
        return "/logout.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }

}

/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import java.net.URISyntaxException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;


/**
 * @author mbechler
 *
 */
public class WebGuiAuthConfigurationImpl implements CasAuthConfiguration {

    private final GuiConfig guiConfig;
    private final String contextUrl;


    /**
     * @param guiConfig
     * @param contextBaseUrl
     * @throws URISyntaxException
     * 
     */
    public WebGuiAuthConfigurationImpl ( GuiConfig guiConfig, URL contextBaseUrl ) throws URISyntaxException {
        this.guiConfig = guiConfig;
        this.contextUrl = contextBaseUrl.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getAuthServerBase()
     */
    @Override
    public String getAuthServerBase () {
        return this.guiConfig.getAuthServerURL().toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getAuthServerBase(java.lang.String)
     */
    @Override
    public String getAuthServerBase ( String overrideServerName ) {
        return this.guiConfig.getAuthServerURL(overrideServerName).toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getAuthServerPubKey()
     */
    @Override
    public PublicKey getAuthServerPubKey () {
        return this.guiConfig.getAuthServerPubKey();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getLocalService()
     */
    @Override
    public String getLocalService () {
        return getLocalContextUrl() + "/login-return"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getLocalProxyCallbackAddress()
     */
    @Override
    public String getLocalProxyCallbackAddress () {
        return getLocalContextUrl() + "/login-proxy"; //$NON-NLS-1$
    }


    /**
     * @return
     */
    protected String getLocalContextUrl () {
        return this.contextUrl;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getDefaultRoles()
     */
    @Override
    public Collection<String> getDefaultRoles () {
        return Arrays.asList();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getDefaultPermissions()
     */
    @Override
    public Collection<String> getDefaultPermissions () {
        return Arrays.asList();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getRoleAttribute()
     */
    @Override
    public String getRoleAttribute () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getPermissionAttribute()
     */
    @Override
    public String getPermissionAttribute () {
        return null;
    }
}

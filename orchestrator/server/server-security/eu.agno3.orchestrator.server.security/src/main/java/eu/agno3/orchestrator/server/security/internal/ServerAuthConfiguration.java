/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.net.URI;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.config.ServerConfiguration;
import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;
import eu.agno3.runtime.ws.server.WebserviceEndpointInfo;


/**
 * @author mbechler
 *
 */
@Component ( service = ServerAuthConfiguration.class )
public class ServerAuthConfiguration implements CasAuthConfiguration {

    private WebserviceEndpointInfo wsEndpointInfo;
    private ServerConfiguration serverConfig;


    @Reference
    protected synchronized void setWSEndpointInfo ( WebserviceEndpointInfo wsei ) {
        this.wsEndpointInfo = wsei;
    }


    protected synchronized void unsetWSEndpointInfo ( WebserviceEndpointInfo wsei ) {
        if ( this.wsEndpointInfo == wsei ) {
            this.wsEndpointInfo = null;
        }
    }


    @Reference
    protected synchronized void setServerConfiguration ( ServerConfiguration sc ) {
        this.serverConfig = sc;
    }


    protected synchronized void unsetServerConfiguration ( ServerConfiguration sc ) {
        if ( this.serverConfig == sc ) {
            this.serverConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getAuthServerBase()
     */
    @Override
    public String getAuthServerBase () {
        URI authServerUrl = this.serverConfig.getAuthServerUrl();
        if ( authServerUrl == null ) {
            return null;
        }
        return authServerUrl.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getAuthServerPubKey()
     */
    @Override
    public PublicKey getAuthServerPubKey () {
        return this.serverConfig.getAuthServerPubKey();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getLocalService()
     */
    @Override
    public String getLocalService () {
        try {
            return this.wsEndpointInfo.getBaseAddress().toString();
        }
        catch ( ServletException e ) {
            throw new IllegalStateException("Could not determine endpoint address", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getLocalProxyCallbackAddress()
     */
    @Override
    public String getLocalProxyCallbackAddress () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getDefaultRoles()
     */
    @Override
    public Collection<String> getDefaultRoles () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getDefaultPermissions()
     */
    @Override
    public Collection<String> getDefaultPermissions () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.CasAuthConfiguration#getRoleAttribute()
     */
    @Override
    public String getRoleAttribute () {
        return "roles"; //$NON-NLS-1$
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

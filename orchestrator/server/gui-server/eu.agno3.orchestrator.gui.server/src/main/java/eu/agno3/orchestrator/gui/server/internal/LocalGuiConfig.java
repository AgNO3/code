/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.server.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.server.EagerServicesActive;
import eu.agno3.orchestrator.server.config.ServerConfiguration;
import eu.agno3.runtime.ws.server.WebserviceEndpointInfo;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    GuiConfig.class, LocalGuiConfig.class
} )
public class LocalGuiConfig implements GuiConfig {

    private static final Logger log = Logger.getLogger(LocalGuiConfig.class);

    private WebserviceEndpointInfo webserviceInfo;

    private ServerConfiguration serverConfig;


    @Reference
    protected synchronized void setWebServiceInfo ( WebserviceEndpointInfo wsInfo ) {
        this.webserviceInfo = wsInfo;
    }


    protected synchronized void unsetWebServiceInfo ( WebserviceEndpointInfo wsInfo ) {
        if ( this.webserviceInfo == wsInfo ) {
            this.webserviceInfo = null;
        }
    }


    @Reference
    protected synchronized void setServerConfiguration ( ServerConfiguration cfg ) {
        this.serverConfig = cfg;
    }


    protected synchronized void unsetServerConfiguration ( ServerConfiguration cfg ) {
        if ( this.serverConfig == cfg ) {
            this.serverConfig = null;
        }
    }


    // dep only
    @Reference
    protected synchronized void bindEagerServicesActive ( EagerServicesActive esa ) {}


    protected synchronized void unbindEagerServicesActive ( EagerServicesActive esa ) {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getId()
     */
    @Override
    public @NonNull UUID getId () {
        return this.serverConfig.getServerId();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getPingTimeout()
     */
    @Override
    public int getPingTimeout () {
        return Integer.MAX_VALUE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getWebServiceBaseAddress()
     */
    @Override
    public URI getWebServiceBaseAddress () {
        try {
            return this.webserviceInfo.getBaseAddress();
        }
        catch ( ServletException e ) {
            log.warn("Could not determine webservice base URL:", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getAuthServerURL()
     */
    @Override
    public URI getAuthServerURL () {
        return this.serverConfig.getAuthServerUrl();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getAuthServerURL(java.lang.String)
     */
    @Override
    public URI getAuthServerURL ( String overrideServerName ) {
        if ( !this.serverConfig.isLocalAuthServer() || StringUtils.isBlank(overrideServerName) ) {
            return getAuthServerURL();
        }

        if ( !this.serverConfig.getAllowedAuthServerNames().contains(overrideServerName) ) {
            log.warn("Trying to use an disallowed auth server " + overrideServerName); //$NON-NLS-1$
            return getAuthServerURL();
        }

        URI asu = this.serverConfig.getAuthServerUrl();
        try {
            return new URI(asu.getScheme(), asu.getUserInfo(), overrideServerName, asu.getPort(), asu.getPath(), asu.getQuery(), asu.getFragment());
        }
        catch ( URISyntaxException e ) {
            log.error("Failed to build URI", e); //$NON-NLS-1$
        }
        return getAuthServerURL();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-guis"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentConfig#getEventOutQueue()
     */
    @Override
    public String getEventOutQueue () {
        throw new UnsupportedOperationException("Out queue is not available for local GUIs"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getAuthServerPubKey()
     */
    @Override
    public PublicKey getAuthServerPubKey () {
        return this.serverConfig.getAuthServerPubKey();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getSessionCookieName()
     */
    @Override
    public String getSessionCookieName () {
        return this.serverConfig.getSessionCookieName();
    }
}

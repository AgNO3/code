/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.config;


import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * 
 */
public class GuiConfigImpl implements GuiConfig {

    private UUID guiId;
    private int pingTimeout;
    private URI webServiceBaseAddress;
    private URI authServerURL;
    private String eventTopic;
    private String eventOutQueue;
    private String sessionCookieName;
    private RSAPublicKey authServerPubKey;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getId()
     */
    @Override
    public UUID getId () {
        return this.guiId;
    }


    /**
     * @param guiId
     *            the guiId to set
     */
    public void setGuiId ( @NonNull UUID guiId ) {
        this.guiId = guiId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getPingTimeout()
     */
    @Override
    public int getPingTimeout () {
        return this.pingTimeout;
    }


    /**
     * @param pingTimeout
     *            the pingTimeout to set
     */
    public void setPingTimeout ( int pingTimeout ) {
        this.pingTimeout = pingTimeout;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getWebServiceBaseAddress()
     */
    @Override
    public URI getWebServiceBaseAddress () {
        return this.webServiceBaseAddress;
    }


    /**
     * @param webServiceBaseAddress
     *            the webServiceBaseAddress to set
     */
    public void setWebServiceBaseAddress ( URI webServiceBaseAddress ) {
        this.webServiceBaseAddress = webServiceBaseAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getSessionCookieName()
     */
    @Override
    public String getSessionCookieName () {
        return this.sessionCookieName;
    }


    /**
     * @param sessionCookieName
     *            the sessionCookieName to set
     */
    public void setSessionCookieName ( String sessionCookieName ) {
        this.sessionCookieName = sessionCookieName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return this.eventTopic;
    }


    /**
     * @param eventTopic
     *            the eventTopic to set
     */
    public void setEventTopic ( String eventTopic ) {
        this.eventTopic = eventTopic;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentConfig#getEventOutQueue()
     */
    @Override
    public String getEventOutQueue () {
        return this.eventOutQueue;
    }


    /**
     * @param eventOutQueue
     *            the eventOutQueue to set
     */
    public void setEventOutQueue ( String eventOutQueue ) {
        this.eventOutQueue = eventOutQueue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getAuthServerURL()
     */
    @Override
    public URI getAuthServerURL () {
        return this.authServerURL;
    }


    /**
     * @param authServerURL
     *            the authServerURL to set
     */
    public void setAuthServerURL ( URI authServerURL ) {
        this.authServerURL = authServerURL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.config.GuiConfig#getAuthServerPubKey()
     */
    @Override
    public RSAPublicKey getAuthServerPubKey () {
        return this.authServerPubKey;
    }


    /**
     * @param authServerPubKey
     *            the authServerPubKey to set
     */
    public void setAuthServerPubKey ( RSAPublicKey authServerPubKey ) {
        this.authServerPubKey = authServerPubKey;
    }

}

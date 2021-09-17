/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( WebReverseProxyConfiguration.class )
@Entity
@Table ( name = "config_web_revproxy" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_revp" )
public class WebReverseProxyConfigurationImpl extends AbstractConfigurationObject<WebReverseProxyConfiguration> implements
        WebReverseProxyConfiguration, WebReverseProxyConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 7343061055925383656L;

    private URI overrideURI;

    private Set<NetworkSpecification> trustedProxies = new HashSet<>();

    private WebReverseProxyType proxyType;

    private String forwardedHostHeader;
    private String forwardedPortHeader;

    private String forwardedRemoteAddrHeader;

    private String forwardedSSLMatchHeader;
    private String forwardedSSLMatchValue;

    private WebReverseProxySSLType forwardedSSLCiphersType;
    private String forwardedSSLCiphersHeader;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<WebReverseProxyConfiguration> getType () {
        return WebReverseProxyConfiguration.class;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebReverseProxyConfiguration#getOverrideURI()
     */
    @Override
    public URI getOverrideURI () {
        return this.overrideURI;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebReverseProxyConfigurationMutable#setOverrideURI(java.net.URI)
     */
    @Override
    public void setOverrideURI ( URI overrideUri ) {
        this.overrideURI = overrideUri;
    }


    /**
     * @return the trustedProxies
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @Column ( name = "addr", nullable = false )
    @CollectionTable ( name = "config_web_revproxy_trusted" )
    public Set<NetworkSpecification> getTrustedProxies () {
        return this.trustedProxies;
    }


    /**
     * @param trustedProxies
     *            the trustedProxies to set
     */
    @Override
    public void setTrustedProxies ( Set<NetworkSpecification> trustedProxies ) {
        this.trustedProxies = trustedProxies;
    }


    /**
     * @return the proxyType
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public WebReverseProxyType getProxyType () {
        return this.proxyType;
    }


    /**
     * @param proxyType
     *            the proxyType to set
     */
    @Override
    public void setProxyType ( WebReverseProxyType proxyType ) {
        this.proxyType = proxyType;
    }


    /**
     * @return the forwardedHostHeader
     */
    @Override
    public String getForwardedHostHeader () {
        return this.forwardedHostHeader;
    }


    /**
     * @param forwardedHostHeader
     *            the forwardedHostHeader to set
     */
    @Override
    public void setForwardedHostHeader ( String forwardedHostHeader ) {
        this.forwardedHostHeader = forwardedHostHeader;
    }


    /**
     * @return the forwardedPortHeader
     */
    @Override
    public String getForwardedPortHeader () {
        return this.forwardedPortHeader;
    }


    /**
     * @param forwardedPortHeader
     *            the forwardedPortHeader to set
     */
    @Override
    public void setForwardedPortHeader ( String forwardedPortHeader ) {
        this.forwardedPortHeader = forwardedPortHeader;
    }


    /**
     * @return the forwardedRemoteAddrHeader
     */
    @Override
    public String getForwardedRemoteAddrHeader () {
        return this.forwardedRemoteAddrHeader;
    }


    /**
     * @param forwardedRemoteAddrHeader
     *            the forwardedRemoteAddrHeader to set
     */
    @Override
    public void setForwardedRemoteAddrHeader ( String forwardedRemoteAddrHeader ) {
        this.forwardedRemoteAddrHeader = forwardedRemoteAddrHeader;
    }


    /**
     * @return the forwardedSSLMatchHeader
     */
    @Override
    public String getForwardedSSLMatchHeader () {
        return this.forwardedSSLMatchHeader;
    }


    /**
     * @param forwardedSSLMatchHeader
     *            the forwardedSSLMatchHeader to set
     */
    @Override
    public void setForwardedSSLMatchHeader ( String forwardedSSLMatchHeader ) {
        this.forwardedSSLMatchHeader = forwardedSSLMatchHeader;
    }


    /**
     * @return the forwardedSSLMatchValue
     */
    @Override
    public String getForwardedSSLMatchValue () {
        return this.forwardedSSLMatchValue;
    }


    /**
     * @param forwardedSSLMatchValue
     *            the forwardedSSLMatchValue to set
     */
    @Override
    public void setForwardedSSLMatchValue ( String forwardedSSLMatchValue ) {
        this.forwardedSSLMatchValue = forwardedSSLMatchValue;
    }


    /**
     * @return the forwardedSSLCiphersType
     */
    @Override
    public WebReverseProxySSLType getForwardedSSLCiphersType () {
        return this.forwardedSSLCiphersType;
    }


    /**
     * @param forwardedSSLCiphersType
     *            the forwardedSSLCiphersType to set
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public void setForwardedSSLCiphersType ( WebReverseProxySSLType forwardedSSLCiphersType ) {
        this.forwardedSSLCiphersType = forwardedSSLCiphersType;
    }


    /**
     * @return the forwardedSSLCiphersHeader
     */
    @Override
    public String getForwardedSSLCiphersHeader () {
        return this.forwardedSSLCiphersHeader;
    }


    /**
     * @param forwardedSSLCiphersHeader
     *            the forwardedSSLCiphersHeader to set
     */
    @Override
    public void setForwardedSSLCiphersHeader ( String forwardedSSLCiphersHeader ) {
        this.forwardedSSLCiphersHeader = forwardedSSLCiphersHeader;
    }

}

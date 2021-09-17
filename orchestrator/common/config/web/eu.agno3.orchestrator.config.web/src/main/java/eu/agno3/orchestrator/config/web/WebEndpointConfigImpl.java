/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( WebEndpointConfig.class )
@Entity
@Table ( name = "config_web_endpoint" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_endp" )
public class WebEndpointConfigImpl extends AbstractConfigurationObject<WebEndpointConfig> implements WebEndpointConfig, WebEndpointConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 3091378329887956155L;

    // basic networking
    private Set<NetworkAddress> bindAddresses;
    private String bindInterface;
    private Integer bindPort;

    private String contextPath;

    private Boolean behindReverseProxy;

    private WebReverseProxyConfigurationMutable reverseProxyConfig;

    private Duration sessionTimeout;

    // ssl
    private Boolean disableSSL;

    // hpkp
    private Boolean enableHPKP;
    private Set<PublicKeyEntry> hpkpPinnedCerts = new HashSet<>();
    private Boolean hpkpIncludeSubdomains;
    private Duration hpkpPinningTimeout;
    private URI hpkpReportUri;
    private Boolean hpkpReportOnly;

    // hsts
    private Boolean enableHSTS;
    private Duration hstsTimeout;
    private Boolean hstsIncludeSubdomains;
    private Boolean hstsAcceptPreload;

    private SSLEndpointConfigurationMutable sslEndpointConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<WebEndpointConfig> getType () {
        return WebEndpointConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getBindAddresses()
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @Column ( name = "addr", nullable = false )
    @CollectionTable ( name = "config_web_endpoint_bind" )
    public Set<NetworkAddress> getBindAddresses () {
        return this.bindAddresses;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setBindAddresses(java.util.Set)
     */
    @Override
    public void setBindAddresses ( Set<NetworkAddress> bindAddresses ) {
        this.bindAddresses = bindAddresses;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getBindInterface()
     */
    @Override
    public String getBindInterface () {
        return this.bindInterface;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setBindInterface(java.lang.String)
     */
    @Override
    public void setBindInterface ( String bindInterface ) {
        this.bindInterface = bindInterface;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getBindPort()
     */
    @Override
    public Integer getBindPort () {
        return this.bindPort;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setBindPort(java.lang.Integer)
     */
    @Override
    public void setBindPort ( Integer bindPort ) {
        this.bindPort = bindPort;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getContextPath()
     */
    @Override
    public String getContextPath () {
        return this.contextPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setContextPath(java.lang.String)
     */
    @Override
    public void setContextPath ( String contextPath ) {
        this.contextPath = contextPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getBehindReverseProxy()
     */
    @Override
    public Boolean getBehindReverseProxy () {
        return this.behindReverseProxy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setBehindReverseProxy(java.lang.Boolean)
     */
    @Override
    public void setBehindReverseProxy ( Boolean behindReverseProxy ) {
        this.behindReverseProxy = behindReverseProxy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getDisableSSL()
     */
    @Override
    public Boolean getDisableSSL () {
        return this.disableSSL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setDisableSSL(java.lang.Boolean)
     */
    @Override
    public void setDisableSSL ( Boolean disableSSL ) {
        this.disableSSL = disableSSL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getSessionInactiveTimeout()
     */
    @Override
    public Duration getSessionInactiveTimeout () {
        return this.sessionTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfigMutable#setSessionInactiveTimeout(org.joda.time.Duration)
     */
    @Override
    public void setSessionInactiveTimeout ( Duration sessionTimeout ) {
        this.sessionTimeout = sessionTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getEnableHPKP()
     */
    @Override
    public Boolean getEnableHPKP () {
        return this.enableHPKP;
    }


    /**
     * @param enableHPKP
     *            the enableHPKP to set
     */
    @Override
    public void setEnableHPKP ( Boolean enableHPKP ) {
        this.enableHPKP = enableHPKP;
    }


    /**
     * @return the hpkpReportOnly
     */
    @Override
    public Boolean getHpkpReportOnly () {
        return this.hpkpReportOnly;
    }


    /**
     * @param hpkpReportOnly
     *            the hpkpReportOnly to set
     */
    @Override
    public void setHpkpReportOnly ( Boolean hpkpReportOnly ) {
        this.hpkpReportOnly = hpkpReportOnly;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHpkpPinnedKeys()
     */
    @Override
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @ManyToMany ( cascade = {
        CascadeType.DETACH, CascadeType.MERGE
    } )
    @JoinTable ( name = "config_web_hpkp_pinned" )
    public Set<PublicKeyEntry> getHpkpPinnedKeys () {
        return this.hpkpPinnedCerts;
    }


    /**
     * @param hpkpPinnedCerts
     *            the hpkpPinnedCerts to set
     */
    @Override
    public void setHpkpPinnedKeys ( Set<PublicKeyEntry> hpkpPinnedCerts ) {
        this.hpkpPinnedCerts = hpkpPinnedCerts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHpkpIncludeSubdomains()
     */
    @Override
    public Boolean getHpkpIncludeSubdomains () {
        return this.hpkpIncludeSubdomains;
    }


    /**
     * @param hpkpIncludeSubdomains
     *            the hpkpIncludeSubdomains to set
     */
    @Override
    public void setHpkpIncludeSubdomains ( Boolean hpkpIncludeSubdomains ) {
        this.hpkpIncludeSubdomains = hpkpIncludeSubdomains;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHpkpPinningTimeout()
     */
    @Override
    public Duration getHpkpPinningTimeout () {
        return this.hpkpPinningTimeout;
    }


    /**
     * @param hpkpPinningTimeout
     *            the hpkpPinningTimeout to set
     */
    @Override
    public void setHpkpPinningTimeout ( Duration hpkpPinningTimeout ) {
        this.hpkpPinningTimeout = hpkpPinningTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHpkpReportUri()
     */
    @Override
    public URI getHpkpReportUri () {
        return this.hpkpReportUri;
    }


    /**
     * @param hpkpReportUri
     *            the hpkpReportUri to set
     */
    @Override
    public void setHpkpReportUri ( URI hpkpReportUri ) {
        this.hpkpReportUri = hpkpReportUri;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getEnableHSTS()
     */
    @Override
    public Boolean getEnableHSTS () {
        return this.enableHSTS;
    }


    /**
     * @param enableHSTS
     *            the enableHSTS to set
     */
    @Override
    public void setEnableHSTS ( Boolean enableHSTS ) {
        this.enableHSTS = enableHSTS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHstsAcceptPreload()
     */
    @Override
    public Boolean getHstsAcceptPreload () {
        return this.hstsAcceptPreload;
    }


    /**
     * @param hstsAcceptPreload
     *            the hstsAcceptPreload to set
     */
    @Override
    public void setHstsAcceptPreload ( Boolean hstsAcceptPreload ) {
        this.hstsAcceptPreload = hstsAcceptPreload;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHstsIncludeSubdomains()
     */
    @Override
    public Boolean getHstsIncludeSubdomains () {
        return this.hstsIncludeSubdomains;
    }


    /**
     * @param hstsIncludeSubdomains
     *            the hstsIncludeSubdomains to set
     */
    @Override
    public void setHstsIncludeSubdomains ( Boolean hstsIncludeSubdomains ) {
        this.hstsIncludeSubdomains = hstsIncludeSubdomains;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.WebEndpointConfig#getHstsTimeout()
     */
    @Override
    public Duration getHstsTimeout () {
        return this.hstsTimeout;
    }


    /**
     * @param hstsTimeout
     *            the hstsTimeout to set
     */
    @Override
    public void setHstsTimeout ( Duration hstsTimeout ) {
        this.hstsTimeout = hstsTimeout;
    }


    /**
     * @return the sslEndpointConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = SSLEndpointConfigurationImpl.class )
    public SSLEndpointConfigurationMutable getSslEndpointConfiguration () {
        return this.sslEndpointConfig;
    }


    /**
     * @param sslEndpointConfig
     */
    @Override
    public void setSslEndpointConfiguration ( SSLEndpointConfigurationMutable sslEndpointConfig ) {
        this.sslEndpointConfig = sslEndpointConfig;
    }


    /**
     * @return the reverseProxyConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = WebReverseProxyConfigurationImpl.class )
    public WebReverseProxyConfigurationMutable getReverseProxyConfig () {
        return this.reverseProxyConfig;
    }


    /**
     * @param reverseProxyConfig
     *            the reverseProxyConfig to set
     */
    @Override
    public void setReverseProxyConfig ( WebReverseProxyConfigurationMutable reverseProxyConfig ) {
        this.reverseProxyConfig = reverseProxyConfig;
    }
}

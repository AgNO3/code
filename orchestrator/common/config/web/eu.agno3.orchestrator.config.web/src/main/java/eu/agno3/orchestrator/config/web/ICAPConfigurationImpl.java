/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( ICAPConfiguration.class )
@Entity
@Table ( name = "config_web_icap" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_icap" )
public class ICAPConfigurationImpl extends AbstractConfigurationObject<ICAPConfiguration> implements ICAPConfiguration, ICAPConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2392531232076314418L;
    private SSLClientMode sslClientMode;
    private SSLClientConfigurationImpl sslClientConfiguration;
    private Duration socketTimeout;
    private Boolean sendICAPSInRequestUri;
    private String overrideRequestURI;
    private List<URI> servers = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ICAPConfiguration> getType () {
        return ICAPConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.ICAPConfiguration#getSslClientMode()
     */
    @Override
    public SSLClientMode getSslClientMode () {
        return this.sslClientMode;
    }


    /**
     * @param sslClientMode
     *            the sslClientMode to set
     */
    @Override
    public void setSslClientMode ( SSLClientMode sslClientMode ) {
        this.sslClientMode = sslClientMode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.ICAPConfiguration#getSslClientConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = SSLClientConfigurationImpl.class )
    public SSLClientConfiguration getSslClientConfiguration () {
        return this.sslClientConfiguration;
    }


    /**
     * @param sslClientConfiguration
     *            the sslClientConfiguration to set
     */
    @Override
    public void setSslClientConfiguration ( SSLClientConfiguration sslClientConfiguration ) {
        this.sslClientConfiguration = (SSLClientConfigurationImpl) sslClientConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.ICAPConfiguration#getSocketTimeout()
     */
    @Override
    public Duration getSocketTimeout () {
        return this.socketTimeout;
    }


    /**
     * @param socketTimeout
     *            the socketTimeout to set
     */
    @Override
    public void setSocketTimeout ( Duration socketTimeout ) {
        this.socketTimeout = socketTimeout;
    }


    /**
     * @return the sendICAPSInRequestUri
     */
    @Override
    public Boolean getSendICAPSInRequestUri () {
        return this.sendICAPSInRequestUri;
    }


    /**
     * @param sendICAPSInRequestUri
     *            the sendICAPSInRequestUri to set
     */
    @Override
    public void setSendICAPSInRequestUri ( Boolean sendICAPSInRequestUri ) {
        this.sendICAPSInRequestUri = sendICAPSInRequestUri;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.ICAPConfiguration#getOverrideRequestURI()
     */
    @Override
    public String getOverrideRequestURI () {
        return this.overrideRequestURI;
    }


    /**
     * @param overrideRequestURI
     *            the overrideRequestURI to set
     */
    @Override
    public void setOverrideRequestURI ( String overrideRequestURI ) {
        this.overrideRequestURI = overrideRequestURI;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.ICAPConfiguration#getServers()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_web_icap_servers" )
    @OrderColumn ( name = "idx" )
    public List<URI> getServers () {
        return this.servers;
    }


    /**
     * @param servers
     *            the servers to set
     */
    @Override
    public void setServers ( List<URI> servers ) {
        this.servers = servers;
    }

}

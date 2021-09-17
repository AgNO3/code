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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import eu.agno3.orchestrator.types.validation.ValidDN;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPConfiguration.class )
@Entity
@Table ( name = "config_web_ldap" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_ldap" )
public class LDAPConfigurationImpl extends AbstractConfigurationObject<LDAPConfiguration> implements LDAPConfiguration, LDAPConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4212911138760542387L;

    private LDAPServerType serverType;
    private List<URI> servers = new ArrayList<>();
    private String srvDomain;
    private String baseDN;
    private LDAPAuthType authType;
    private String bindDN;
    private String saslUsername;
    private String saslMechanism;
    private String password;
    private SSLClientConfigurationImpl sslClientConfiguration;
    private SSLClientMode sslClientMode;

    private Duration socketTimeout;

    private String saslRealm;

    private SASLQOP saslQOP;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPConfiguration> getType () {
        return LDAPConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getServerType()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public LDAPServerType getServerType () {
        return this.serverType;
    }


    /**
     * @param serverType
     *            the serverType to set
     */
    @Override
    public void setServerType ( LDAPServerType serverType ) {
        this.serverType = serverType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getServers()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_web_ldap_servers" )
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSrvDomain()
     */
    @Override
    public String getSrvDomain () {
        return this.srvDomain;
    }


    /**
     * @param srvDomain
     *            the srvDomain to set
     */
    @Override
    public void setSrvDomain ( String srvDomain ) {
        this.srvDomain = srvDomain;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getBaseDN()
     */
    @Override
    @ValidDN
    public String getBaseDN () {
        return this.baseDN;
    }


    /**
     * @param baseDN
     *            the baseDN to set
     */
    @Override
    public void setBaseDN ( String baseDN ) {
        this.baseDN = baseDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getAuthType()
     */
    @Override
    public LDAPAuthType getAuthType () {
        return this.authType;
    }


    /**
     * @param authType
     *            the authType to set
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public void setAuthType ( LDAPAuthType authType ) {
        this.authType = authType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getBindDN()
     */
    @Override
    @ValidDN
    public String getBindDN () {
        return this.bindDN;
    }


    /**
     * @param bindDN
     *            the bindDN to set
     */
    @Override
    public void setBindDN ( String bindDN ) {
        this.bindDN = bindDN;
    }


    /**
     * @return the saslMechanism
     */
    @Override
    public String getSaslMechanism () {
        return this.saslMechanism;
    }


    /**
     * @param saslMechanism
     *            the saslMechanism to set
     */
    @Override
    public void setSaslMechanism ( String saslMechanism ) {
        this.saslMechanism = saslMechanism;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSaslRealm()
     */
    @Override
    public String getSaslRealm () {
        return this.saslRealm;
    }


    /**
     * @param saslRealm
     *            the saslRealm to set
     */
    @Override
    public void setSaslRealm ( String saslRealm ) {
        this.saslRealm = saslRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSaslUsername()
     */
    @Override
    public String getSaslUsername () {
        return this.saslUsername;
    }


    /**
     * @param saslUsername
     *            the saslUsername to set
     */
    @Override
    public void setSaslUsername ( String saslUsername ) {
        this.saslUsername = saslUsername;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSaslQOP()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public SASLQOP getSaslQOP () {
        return this.saslQOP;
    }


    /**
     * @param saslQOP
     *            the saslQOP to set
     */
    public void setSaslQOP ( SASLQOP saslQOP ) {
        this.saslQOP = saslQOP;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getPassword()
     */
    @Override
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    @Override
    public void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSslClientConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = SSLClientConfigurationImpl.class )
    public SSLClientConfigurationMutable getSslClientConfiguration () {
        return this.sslClientConfiguration;
    }


    /**
     * @param sslClientConfiguration
     *            the sslClientConfiguration to set
     */
    @Override
    public void setSslClientConfiguration ( SSLClientConfigurationMutable sslClientConfiguration ) {
        this.sslClientConfiguration = (SSLClientConfigurationImpl) sslClientConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.LDAPConfiguration#getSslClientMode()
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
     * @return the socketTimeout
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
     * @param e
     * @return cloned object
     */
    public static LDAPConfigurationImpl clone ( LDAPConfiguration e ) {
        LDAPConfigurationImpl cloned = new LDAPConfigurationImpl();

        cloned.serverType = e.getServerType();
        cloned.servers = new ArrayList<>(e.getServers());
        cloned.srvDomain = e.getSrvDomain();
        cloned.baseDN = e.getBaseDN();
        cloned.authType = e.getAuthType();
        cloned.bindDN = e.getBindDN();
        cloned.saslUsername = e.getSaslUsername();
        cloned.password = e.getPassword();
        cloned.sslClientMode = e.getSslClientMode();
        cloned.saslMechanism = e.getSaslMechanism();
        cloned.socketTimeout = e.getSocketTimeout();

        cloned.sslClientConfiguration = SSLClientConfigurationImpl.clone(e.getSslClientConfiguration());
        return cloned;
    }
}

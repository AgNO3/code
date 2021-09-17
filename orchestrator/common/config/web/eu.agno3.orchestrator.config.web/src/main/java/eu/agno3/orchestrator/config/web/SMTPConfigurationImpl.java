/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
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
@MapAs ( SMTPConfiguration.class )
@Entity
@Table ( name = "config_web_smtp" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_smtp" )
public class SMTPConfigurationImpl extends AbstractConfigurationObject<SMTPConfiguration> implements SMTPConfiguration, SMTPConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1684527424487052437L;

    private URI serverUri;

    private SSLClientMode sslClientMode;
    private SSLClientConfigurationImpl sslClientConfiguration;

    private Duration socketTimeout;

    private String overrideEhloHostName;
    private String overrideDefaultFromAddress;
    private String overrideDefaultFromName;

    private Boolean authEnabled;
    private String smtpUser;
    private String smtpPassword;

    private Set<String> authMechanisms = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<SMTPConfiguration> getType () {
        return SMTPConfiguration.class;
    }


    /**
     * @return the serverUri
     */
    @Override
    public URI getServerUri () {
        return this.serverUri;
    }


    /**
     * @param serverUri
     *            the serverUri to set
     */
    @Override
    public void setServerUri ( URI serverUri ) {
        this.serverUri = serverUri;
    }


    /**
     * @return the sslClientMode
     */
    @Override
    @Enumerated(EnumType.STRING)
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
     * @return the sslClientConfiguration
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
     * @return the overrideEhloHostName
     */
    @Override
    public String getOverrideEhloHostName () {
        return this.overrideEhloHostName;
    }


    /**
     * @param overrideEhloHostName
     *            the overrideEhloHostName to set
     */
    @Override
    public void setOverrideEhloHostName ( String overrideEhloHostName ) {
        this.overrideEhloHostName = overrideEhloHostName;
    }


    /**
     * @return the overrideDefaultFromAddress
     */
    @Override
    public String getOverrideDefaultFromAddress () {
        return this.overrideDefaultFromAddress;
    }


    /**
     * @param overrideDefaultFromAddress
     *            the overrideDefaultFromAddress to set
     */
    @Override
    public void setOverrideDefaultFromAddress ( String overrideDefaultFromAddress ) {
        this.overrideDefaultFromAddress = overrideDefaultFromAddress;
    }


    /**
     * @return the overrideDefaultFromName
     */
    @Override
    public String getOverrideDefaultFromName () {
        return this.overrideDefaultFromName;
    }


    /**
     * @param overrideDefaultFromName
     *            the overrideDefaultFromName to set
     */
    @Override
    public void setOverrideDefaultFromName ( String overrideDefaultFromName ) {
        this.overrideDefaultFromName = overrideDefaultFromName;
    }


    /**
     * @return the authEnabled
     */
    @Override
    public Boolean getAuthEnabled () {
        return this.authEnabled;
    }


    /**
     * @param authEnabled
     *            the authEnabled to set
     */
    @Override
    public void setAuthEnabled ( Boolean authEnabled ) {
        this.authEnabled = authEnabled;
    }


    /**
     * @return the smtpUser
     */
    @Override
    public String getSmtpUser () {
        return this.smtpUser;
    }


    /**
     * @param smtpUser
     *            the smtpUser to set
     */
    @Override
    public void setSmtpUser ( String smtpUser ) {
        this.smtpUser = smtpUser;
    }


    /**
     * @return the smtpPassword
     */
    @Override
    public String getSmtpPassword () {
        return this.smtpPassword;
    }


    /**
     * @param smtpPassword
     *            the smtpPassword to set
     */
    @Override
    public void setSmtpPassword ( String smtpPassword ) {
        this.smtpPassword = smtpPassword;
    }


    /**
     * @return the authMechanisms
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_web_smtp_auth" )
    public Set<String> getAuthMechanisms () {
        return this.authMechanisms;
    }


    /**
     * @param authMechanisms
     *            the authMechanisms to set
     */
    @Override
    public void setAuthMechanisms ( Set<String> authMechanisms ) {
        this.authMechanisms = authMechanisms;
    }

}

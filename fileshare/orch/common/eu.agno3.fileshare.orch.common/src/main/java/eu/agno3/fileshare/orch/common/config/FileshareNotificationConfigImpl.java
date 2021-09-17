/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Lob;
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
@MapAs ( FileshareNotificationConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_notify" )
@Audited
@DiscriminatorValue ( "filesh_notify" )
public class FileshareNotificationConfigImpl extends AbstractConfigurationObject<FileshareNotificationConfig> implements FileshareNotificationConfig,
        FileshareNotificationConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 8819956769357671690L;

    private Boolean notificationDisabled;

    private String templateLibrary;

    private Set<String> sendAsUserNotificationDomains = new HashSet<>();

    private Locale defaultNotificationLocale;

    private String defaultSenderName;
    private String defaultSenderAddress;

    private String adminContactAddress;

    private String footer;

    private Duration expirationNotificationPeriod;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareNotificationConfig> getType () {
        return FileshareNotificationConfig.class;
    }


    /**
     * @return the notificationDisabled
     */
    @Override
    public Boolean getNotificationDisabled () {
        return this.notificationDisabled;
    }


    /**
     * @param notificationDisabled
     *            the notificationDisabled to set
     */
    @Override
    public void setNotificationDisabled ( Boolean notificationDisabled ) {
        this.notificationDisabled = notificationDisabled;
    }


    /**
     * @return the templateLibrary
     */
    @Override
    public String getTemplateLibrary () {
        return this.templateLibrary;
    }


    /**
     * @param templateLibrary
     *            the templateLibrary to set
     */
    @Override
    public void setTemplateLibrary ( String templateLibrary ) {
        this.templateLibrary = templateLibrary;
    }


    /**
     * @return the defaultNotificationLocale
     */
    @Override
    public Locale getDefaultNotificationLocale () {
        return this.defaultNotificationLocale;
    }


    /**
     * @param defaultNotificationLocale
     *            the defaultNotificationLocale to set
     */
    @Override
    public void setDefaultNotificationLocale ( Locale defaultNotificationLocale ) {
        this.defaultNotificationLocale = defaultNotificationLocale;
    }


    /**
     * @return the sendAsUserNotificationDomains
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_notify_sendasuserdom" )
    public Set<String> getSendAsUserNotificationDomains () {
        return this.sendAsUserNotificationDomains;
    }


    /**
     * @param sendAsUserNotificationDomains
     *            the sendAsUserNotificationDomains to set
     */
    @Override
    public void setSendAsUserNotificationDomains ( Set<String> sendAsUserNotificationDomains ) {
        this.sendAsUserNotificationDomains = sendAsUserNotificationDomains;
    }


    /**
     * @return the defaultSenderName
     */
    @Override
    public String getDefaultSenderName () {
        return this.defaultSenderName;
    }


    /**
     * @param defaultSenderName
     *            the defaultSenderName to set
     */
    @Override
    public void setDefaultSenderName ( String defaultSenderName ) {
        this.defaultSenderName = defaultSenderName;
    }


    /**
     * @return the defaultSenderAddress
     */
    @Override
    public String getDefaultSenderAddress () {
        return this.defaultSenderAddress;
    }


    /**
     * @param defaultSenderAddress
     *            the defaultSenderAddress to set
     */
    @Override
    public void setDefaultSenderAddress ( String defaultSenderAddress ) {
        this.defaultSenderAddress = defaultSenderAddress;
    }


    /**
     * @return the adminContactAddress
     */
    @Override
    public String getAdminContactAddress () {
        return this.adminContactAddress;
    }


    /**
     * @param adminContactAddress
     *            the adminContactAddress to set
     */
    @Override
    public void setAdminContactAddress ( String adminContactAddress ) {
        this.adminContactAddress = adminContactAddress;
    }


    /**
     * @return the footer
     */
    @Override
    @Lob ( )
    public String getFooter () {
        return this.footer;
    }


    /**
     * @param footer
     *            the footer to set
     */
    @Override
    public void setFooter ( String footer ) {
        this.footer = footer;
    }


    /**
     * @return the expirationNotificationPeriod
     */
    @Override
    public Duration getExpirationNotificationPeriod () {
        return this.expirationNotificationPeriod;
    }


    /**
     * @param expirationNotificationPeriod
     *            the expirationNotificationPeriod to set
     */
    @Override
    public void setExpirationNotificationPeriod ( Duration expirationNotificationPeriod ) {
        this.expirationNotificationPeriod = expirationNotificationPeriod;
    }

}

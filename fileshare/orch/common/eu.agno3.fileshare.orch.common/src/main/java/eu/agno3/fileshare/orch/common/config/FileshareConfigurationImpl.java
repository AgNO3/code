/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare" )
@Audited
@DiscriminatorValue ( "filesh" )
public class FileshareConfigurationImpl extends AbstractConfigurationInstance<FileshareConfiguration>
        implements FileshareConfiguration, FileshareConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2316874406902537841L;
    private FileshareWebConfigImpl webConfiguration;
    private FileshareUserConfigImpl userConfiguration;
    private FileshareAuthConfigImpl authConfiguration;
    private FileshareContentConfigImpl contentConfiguration;
    private FileshareNotificationConfigImpl notificationConfiguration;
    private FileshareSecurityPolicyConfigImpl securityPolicyConfiguration;
    private FileshareStorageConfigImpl storageConfiguration;
    private FileshareAdvancedConfigImpl advancedConfiguration;
    private FileshareLoggerConfigImpl loggerConfiguration;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareConfiguration> getType () {
        return FileshareConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareConfiguration#getWebConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareWebConfigImpl.class )
    public FileshareWebConfigMutable getWebConfiguration () {
        return this.webConfiguration;
    }


    /**
     * @param webConfiguration
     *            the webConfiguration to set
     */
    @Override
    public void setWebConfiguration ( FileshareWebConfigMutable webConfiguration ) {
        this.webConfiguration = (FileshareWebConfigImpl) webConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareConfiguration#getUserConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareUserConfigImpl.class )
    public FileshareUserConfigMutable getUserConfiguration () {
        return this.userConfiguration;
    }


    /**
     * @param userConfiguration
     *            the userConfiguration to set
     */
    @Override
    public void setUserConfiguration ( FileshareUserConfigMutable userConfiguration ) {
        this.userConfiguration = (FileshareUserConfigImpl) userConfiguration;
    }


    /**
     * @return the authConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareAuthConfigImpl.class )
    public FileshareAuthConfigMutable getAuthConfiguration () {
        return this.authConfiguration;
    }


    /**
     * @param authConfiguration
     *            the authConfiguration to set
     */
    @Override
    public void setAuthConfiguration ( FileshareAuthConfigMutable authConfiguration ) {
        this.authConfiguration = (FileshareAuthConfigImpl) authConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareConfiguration#getContentConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareContentConfigImpl.class )
    public FileshareContentConfigMutable getContentConfiguration () {
        return this.contentConfiguration;
    }


    /**
     * @param contentConfiguration
     *            the contentConfiguration to set
     */
    @Override
    public void setContentConfiguration ( FileshareContentConfigMutable contentConfiguration ) {
        this.contentConfiguration = (FileshareContentConfigImpl) contentConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareConfiguration#getNotificationConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareNotificationConfigImpl.class )
    public FileshareNotificationConfigMutable getNotificationConfiguration () {
        return this.notificationConfiguration;
    }


    /**
     * @param notificationConfiguration
     *            the notificationConfiguration to set
     */
    @Override
    public void setNotificationConfiguration ( FileshareNotificationConfigMutable notificationConfiguration ) {
        this.notificationConfiguration = (FileshareNotificationConfigImpl) notificationConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareConfiguration#getSecurityPolicyConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareSecurityPolicyConfigImpl.class )
    public FileshareSecurityPolicyConfigMutable getSecurityPolicyConfiguration () {
        return this.securityPolicyConfiguration;
    }


    /**
     * @param securityPolicyConfiguration
     *            the securityPolicyConfiguration to set
     */
    @Override
    public void setSecurityPolicyConfiguration ( FileshareSecurityPolicyConfigMutable securityPolicyConfiguration ) {
        this.securityPolicyConfiguration = (FileshareSecurityPolicyConfigImpl) securityPolicyConfiguration;
    }


    /**
     * @return the storageConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareStorageConfigImpl.class )
    public FileshareStorageConfigMutable getStorageConfiguration () {
        return this.storageConfiguration;
    }


    /**
     * @param storageConfiguration
     *            the storageConfiguration to set
     */
    @Override
    public void setStorageConfiguration ( FileshareStorageConfigMutable storageConfiguration ) {
        this.storageConfiguration = (FileshareStorageConfigImpl) storageConfiguration;
    }


    /**
     * @return the advancedConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareAdvancedConfigImpl.class )
    public FileshareAdvancedConfigMutable getAdvancedConfiguration () {
        return this.advancedConfiguration;
    }


    /**
     * @param advancedConfiguration
     *            the advancedConfiguration to set
     */
    @Override
    public void setAdvancedConfiguration ( FileshareAdvancedConfigMutable advancedConfiguration ) {
        this.advancedConfiguration = (FileshareAdvancedConfigImpl) advancedConfiguration;
    }


    /**
     * @return the loggerConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareLoggerConfigImpl.class )
    public FileshareLoggerConfigMutable getLoggerConfiguration () {
        return this.loggerConfiguration;
    }


    /**
     * @param loggerConfiguration
     *            the loggerConfiguration to set
     */
    @Override
    public void setLoggerConfiguration ( FileshareLoggerConfigMutable loggerConfiguration ) {
        this.loggerConfiguration = (FileshareLoggerConfigImpl) loggerConfiguration;
    }
}

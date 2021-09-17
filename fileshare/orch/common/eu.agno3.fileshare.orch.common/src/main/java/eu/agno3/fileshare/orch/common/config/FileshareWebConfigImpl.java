/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.net.URI;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.WebEndpointConfigImpl;
import eu.agno3.orchestrator.config.web.WebEndpointConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareWebConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_web" )
@Audited
@DiscriminatorValue ( "filesh_web" )
public class FileshareWebConfigImpl extends AbstractConfigurationObject<FileshareWebConfig> implements FileshareWebConfig, FileshareWebConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1063745951993065410L;

    private WebEndpointConfigImpl webEndpointConfig;
    private Boolean enableWebDAV;
    private Duration intentTimeout;
    private String themeLibrary;

    private Boolean webDAVAllowSetModificationTime;

    private URI overrideBaseURI;

    private Duration sessionIncompleteExpireDuration;
    private Duration userIncompleteExpireDuration;

    private Boolean perUserIncompleteSizeLimitEnabled;
    private Long perUserIncompleteSizeLimitMB;

    private Boolean perSessionIncompleteSizeLimitEnabled;
    private Long perSessionIncompleteSizeLimitMB;

    private Long defaultUploadChunkSize;
    private Long maximumUploadChunkSize;
    private Integer optimalUploadChunkCount;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareWebConfig> getType () {
        return FileshareWebConfig.class;
    }


    /**
     * @return the webEndpointConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = WebEndpointConfigImpl.class )
    public WebEndpointConfigMutable getWebEndpointConfig () {
        return this.webEndpointConfig;
    }


    /**
     * @param webEndpointConfig
     *            the webEndpointConfig to set
     */
    @Override
    public void setWebEndpointConfig ( WebEndpointConfigMutable webEndpointConfig ) {
        this.webEndpointConfig = (WebEndpointConfigImpl) webEndpointConfig;
    }


    /**
     * @return the enableWebDAV
     */
    @Override
    public Boolean getEnableWebDAV () {
        return this.enableWebDAV;
    }


    /**
     * @param enableWebDAV
     *            the enableWebDAV to set
     */
    @Override
    public void setEnableWebDAV ( Boolean enableWebDAV ) {
        this.enableWebDAV = enableWebDAV;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getWebDAVAllowSetModificationTime()
     */
    @Override
    public Boolean getWebDAVAllowSetModificationTime () {
        return this.webDAVAllowSetModificationTime;
    }


    /**
     * @param webDAVAllowSetModificationTime
     *            the webDAVAllowSetModificationTime to set
     */
    @Override
    public void setWebDAVAllowSetModificationTime ( Boolean webDAVAllowSetModificationTime ) {
        this.webDAVAllowSetModificationTime = webDAVAllowSetModificationTime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getOverrideBaseURI()
     */
    @Override
    public URI getOverrideBaseURI () {
        return this.overrideBaseURI;
    }


    /**
     * @param overrideBaseURI
     *            the overrideBaseURI to set
     */
    @Override
    public void setOverrideBaseURI ( URI overrideBaseURI ) {
        this.overrideBaseURI = overrideBaseURI;
    }


    /**
     * @return the intentTimeout
     */
    @Override
    public Duration getIntentTimeout () {
        return this.intentTimeout;
    }


    /**
     * @param intentTimeout
     *            the intentTimeout to set
     */
    @Override
    public void setIntentTimeout ( Duration intentTimeout ) {
        this.intentTimeout = intentTimeout;
    }


    /**
     * @return the themeLibrary
     */
    @Override
    public String getThemeLibrary () {
        return this.themeLibrary;
    }


    /**
     * @param themeLibrary
     *            the themeLibrary to set
     */
    @Override
    public void setThemeLibrary ( String themeLibrary ) {
        this.themeLibrary = themeLibrary;
    }


    /**
     * @return the perUserIncompleteSizeLimitMB
     */
    @Override
    public Long getPerUserIncompleteSizeLimit () {
        return this.perUserIncompleteSizeLimitMB;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getPerUserIncompleteSizeLimitEnabled()
     */
    @Override
    public Boolean getPerUserIncompleteSizeLimitEnabled () {
        return this.perUserIncompleteSizeLimitEnabled;
    }


    /**
     * @param perUserIncompleteSizeLimitEnabled
     *            the perUserIncompleteSizeLimitEnabled to set
     */
    @Override
    public void setPerUserIncompleteSizeLimitEnabled ( Boolean perUserIncompleteSizeLimitEnabled ) {
        this.perUserIncompleteSizeLimitEnabled = perUserIncompleteSizeLimitEnabled;
    }


    /**
     * @param perUserIncompleteSizeLimit
     *            the perUserIncompleteSizeLimitMB to set
     */
    @Override
    public void setPerUserIncompleteSizeLimit ( Long perUserIncompleteSizeLimit ) {
        this.perUserIncompleteSizeLimitMB = perUserIncompleteSizeLimit;
    }


    /**
     * @return the userIncompleteExpireDuration
     */
    @Override
    public Duration getUserIncompleteExpireDuration () {
        return this.userIncompleteExpireDuration;
    }


    /**
     * @param userIncompleteExpireDuration
     *            the userIncompleteExpireDuration to set
     */
    @Override
    public void setUserIncompleteExpireDuration ( Duration userIncompleteExpireDuration ) {
        this.userIncompleteExpireDuration = userIncompleteExpireDuration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getPerSessionIncompleteSizeLimitEnabled()
     */
    @Override
    public Boolean getPerSessionIncompleteSizeLimitEnabled () {
        return this.perSessionIncompleteSizeLimitEnabled;
    }


    /**
     * @param perSessionIncompleteSizeLimitEnabled
     *            the perSessionIncompleteSizeLimitEnabled to set
     */
    @Override
    public void setPerSessionIncompleteSizeLimitEnabled ( Boolean perSessionIncompleteSizeLimitEnabled ) {
        this.perSessionIncompleteSizeLimitEnabled = perSessionIncompleteSizeLimitEnabled;
    }


    /**
     * @return the perSessionIncompleteSizeLimitMB
     */
    @Override
    public Long getPerSessionIncompleteSizeLimit () {
        return this.perSessionIncompleteSizeLimitMB;
    }


    /**
     * @param perSessionIncompleteSizeLimit
     *            the perSessionIncompleteSizeLimitMB to set
     */
    @Override
    public void setPerSessionIncompleteSizeLimit ( Long perSessionIncompleteSizeLimit ) {
        this.perSessionIncompleteSizeLimitMB = perSessionIncompleteSizeLimit;
    }


    /**
     * @return the sessionIncompleteExpireDuration
     */
    @Override
    public Duration getSessionIncompleteExpireDuration () {
        return this.sessionIncompleteExpireDuration;
    }


    /**
     * @param sessionIncompleteExpireDuration
     *            the sessionIncompleteExpireDuration to set
     */
    @Override
    public void setSessionIncompleteExpireDuration ( Duration sessionIncompleteExpireDuration ) {
        this.sessionIncompleteExpireDuration = sessionIncompleteExpireDuration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getDefaultUploadChunkSize()
     */
    @Override
    public Long getDefaultUploadChunkSize () {
        return this.defaultUploadChunkSize;
    }


    /**
     * @param defaultUploadChunkSize
     *            the defaultUploadChunkSize to set
     */
    @Override
    public void setDefaultUploadChunkSize ( Long defaultUploadChunkSize ) {
        this.defaultUploadChunkSize = defaultUploadChunkSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getMaximumUploadChunkSize()
     */
    @Override
    public Long getMaximumUploadChunkSize () {
        return this.maximumUploadChunkSize;
    }


    /**
     * @param maximumUploadChunkSize
     *            the maximumUploadChunkSize to set
     */
    @Override
    public void setMaximumUploadChunkSize ( Long maximumUploadChunkSize ) {
        this.maximumUploadChunkSize = maximumUploadChunkSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareWebConfig#getOptimalUploadChunkCount()
     */
    @Override
    public Integer getOptimalUploadChunkCount () {
        return this.optimalUploadChunkCount;
    }


    /**
     * @param optimalUploadChunkCount
     *            the optimalUploadChunkCount to set
     */
    @Override
    public void setOptimalUploadChunkCount ( Integer optimalUploadChunkCount ) {
        this.optimalUploadChunkCount = optimalUploadChunkCount;
    }
}

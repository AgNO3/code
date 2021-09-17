/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareContentConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_content" )
@Audited
@DiscriminatorValue ( "filesh_cont" )
public class FileshareContentConfigImpl extends AbstractConfigurationObject<FileshareContentConfig> implements FileshareContentConfig,
        FileshareContentConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4334190954964593786L;

    private Boolean allowMimeTypeChanges;

    private Set<String> whitelistMimeTypes = new HashSet<>();
    private Set<String> blacklistMimeTypes = new HashSet<>();

    private String fallbackMimeType;

    private Boolean useUserSuppliedTypeInfo;

    private FileshareContentPreviewConfigImpl previewConfig;
    private FileshareContentSearchConfigImpl searchConfig;
    private FileshareContentScanConfigImpl scanConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareContentConfig> getType () {
        return FileshareContentConfig.class;
    }


    /**
     * @return the previewConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareContentPreviewConfigImpl.class )
    public FileshareContentPreviewConfigMutable getPreviewConfig () {
        return this.previewConfig;
    }


    /**
     * @param previewConfig
     *            the previewConfig to set
     */
    @Override
    public void setPreviewConfig ( FileshareContentPreviewConfigMutable previewConfig ) {
        this.previewConfig = (FileshareContentPreviewConfigImpl) previewConfig;
    }


    /**
     * @return the searchConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareContentSearchConfigImpl.class )
    public FileshareContentSearchConfigMutable getSearchConfig () {
        return this.searchConfig;
    }


    /**
     * @param searchConfig
     *            the searchConfig to set
     */
    @Override
    public void setSearchConfig ( FileshareContentSearchConfigMutable searchConfig ) {
        this.searchConfig = (FileshareContentSearchConfigImpl) searchConfig;
    }


    /**
     * @return the scanConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareContentScanConfigImpl.class )
    public FileshareContentScanConfigMutable getScanConfig () {
        return this.scanConfig;
    }


    /**
     * @param scanConfig
     *            the scanConfig to set
     */
    @Override
    public void setScanConfig ( FileshareContentScanConfigMutable scanConfig ) {
        this.scanConfig = (FileshareContentScanConfigImpl) scanConfig;
    }


    /**
     * @return the allowMimeTypeChanges
     */
    @Override
    public Boolean getAllowMimeTypeChanges () {
        return this.allowMimeTypeChanges;
    }


    /**
     * @param allowMimeTypeChanges
     *            the allowMimeTypeChanges to set
     */
    @Override
    public void setAllowMimeTypeChanges ( Boolean allowMimeTypeChanges ) {
        this.allowMimeTypeChanges = allowMimeTypeChanges;
    }


    /**
     * @return the whitelistMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_whitelist" )
    public Set<String> getWhitelistMimeTypes () {
        return this.whitelistMimeTypes;
    }


    /**
     * @param whitelistMimeTypes
     *            the whitelistMimeTypes to set
     */

    @Override
    public void setWhitelistMimeTypes ( Set<String> whitelistMimeTypes ) {
        this.whitelistMimeTypes = whitelistMimeTypes;
    }


    /**
     * @return the blacklistMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_blacklist" )
    public Set<String> getBlacklistMimeTypes () {
        return this.blacklistMimeTypes;
    }


    /**
     * @param blacklistMimeTypes
     *            the blacklistMimeTypes to set
     */
    @Override
    public void setBlacklistMimeTypes ( Set<String> blacklistMimeTypes ) {
        this.blacklistMimeTypes = blacklistMimeTypes;
    }


    /**
     * @return the fallbackMimeType
     */
    @Override
    public String getFallbackMimeType () {
        return this.fallbackMimeType;
    }


    /**
     * @param fallbackMimeType
     *            the fallbackMimeType to set
     */
    @Override
    public void setFallbackMimeType ( String fallbackMimeType ) {
        this.fallbackMimeType = fallbackMimeType;
    }


    /**
     * @return the useUserSuppliedTypeInfo
     */
    @Override
    public Boolean getUseUserSuppliedTypeInfo () {
        return this.useUserSuppliedTypeInfo;
    }


    /**
     * @param useUserSuppliedTypeInfo
     *            the useUserSuppliedTypeInfo to set
     */
    @Override
    public void setUseUserSuppliedTypeInfo ( Boolean useUserSuppliedTypeInfo ) {
        this.useUserSuppliedTypeInfo = useUserSuppliedTypeInfo;
    }

}

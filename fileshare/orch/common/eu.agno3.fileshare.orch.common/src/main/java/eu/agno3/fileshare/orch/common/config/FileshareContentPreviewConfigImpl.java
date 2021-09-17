/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
@MapAs ( FileshareContentPreviewConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_content_preview" )
@Audited
@DiscriminatorValue ( "filesh_cont_prev" )
public class FileshareContentPreviewConfigImpl extends AbstractConfigurationObject<FileshareContentPreviewConfig> implements
        FileshareContentPreviewConfig, FileshareContentPreviewConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -5425981103000441541L;
    private Set<String> previewMimeTypes = new HashSet<>();
    private Set<String> previewsafeMimeTypes = new HashSet<>();
    private Set<String> previewNoSandboxMimeTypes = new HashSet<>();
    private Set<String> previewRelaxedCSPMimeTypes = new HashSet<>();

    private Boolean limitPreviewFileSize;
    private Long maxPreviewFileSize;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareContentPreviewConfig> getType () {
        return FileshareContentPreviewConfig.class;
    }


    /**
     * @return the viewableMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_view" )
    public Set<String> getPreviewMimeTypes () {
        return this.previewMimeTypes;
    }


    /**
     * @param viewableMimeTypes
     *            the viewableMimeTypes to set
     */
    @Override
    public void setPreviewMimeTypes ( Set<String> viewableMimeTypes ) {
        this.previewMimeTypes = viewableMimeTypes;
    }


    /**
     * @return the safeMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_safe" )
    public Set<String> getPreviewSafeMimeTypes () {
        return this.previewsafeMimeTypes;
    }


    /**
     * @param safeMimeTypes
     *            the safeMimeTypes to set
     */
    @Override
    public void setPreviewSafeMimeTypes ( Set<String> safeMimeTypes ) {
        this.previewsafeMimeTypes = safeMimeTypes;
    }


    /**
     * @return the noSandboxMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_nosandbox" )
    public Set<String> getPreviewNoSandboxMimeTypes () {
        return this.previewNoSandboxMimeTypes;
    }


    /**
     * @param noSandboxMimeTypes
     *            the noSandboxMimeTypes to set
     */
    @Override
    public void setPreviewNoSandboxMimeTypes ( Set<String> noSandboxMimeTypes ) {
        this.previewNoSandboxMimeTypes = noSandboxMimeTypes;
    }


    /**
     * @return the relaxedCSPMimeTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_content_types_relaxed_csp" )
    public Set<String> getPreviewRelaxedCSPMimeTypes () {
        return this.previewRelaxedCSPMimeTypes;
    }


    /**
     * @param relaxedCSPMimeTypes
     *            the relaxedCSPMimeTypes to set
     */
    @Override
    public void setPreviewRelaxedCSPMimeTypes ( Set<String> relaxedCSPMimeTypes ) {
        this.previewRelaxedCSPMimeTypes = relaxedCSPMimeTypes;
    }


    /**
     * @return the limitPreviewFileSize
     */
    @Override
    public Boolean getLimitPreviewFileSize () {
        return this.limitPreviewFileSize;
    }


    /**
     * @param limitPreviewFileSize
     *            the limitPreviewFileSize to set
     */
    @Override
    public void setLimitPreviewFileSize ( Boolean limitPreviewFileSize ) {
        this.limitPreviewFileSize = limitPreviewFileSize;
    }


    /**
     * @return the maxPreviewFileSize
     */
    @Override
    public Long getMaxPreviewFileSize () {
        return this.maxPreviewFileSize;
    }


    /**
     * @param maxPreviewFileSize
     *            the maxPreviewFileSize to set
     */
    @Override
    public void setMaxPreviewFileSize ( Long maxPreviewFileSize ) {
        this.maxPreviewFileSize = maxPreviewFileSize;
    }

}

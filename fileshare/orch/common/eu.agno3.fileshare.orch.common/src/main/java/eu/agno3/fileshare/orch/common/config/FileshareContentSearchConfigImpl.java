/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
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
@MapAs ( FileshareContentSearchConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_content_search" )
@Audited
@DiscriminatorValue ( "filesh_cont_srch" )
public class FileshareContentSearchConfigImpl extends AbstractConfigurationObject<FileshareContentSearchConfig> implements
        FileshareContentSearchConfig, FileshareContentSearchConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 3338172731355672529L;
    private Boolean searchDisabled;
    private Boolean searchAllowPaging;
    private Integer searchPageSize;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareContentSearchConfig> getType () {
        return FileshareContentSearchConfig.class;
    }


    /**
     * @return the searchDisabled
     */
    @Override
    public Boolean getSearchDisabled () {
        return this.searchDisabled;
    }


    /**
     * @param searchDisabled
     *            the searchDisabled to set
     */
    @Override
    public void setSearchDisabled ( Boolean searchDisabled ) {
        this.searchDisabled = searchDisabled;
    }


    /**
     * @return the searchAllowPaging
     */
    @Override
    public Boolean getSearchAllowPaging () {
        return this.searchAllowPaging;
    }


    /**
     * @param searchAllowPaging
     *            the searchAllowPaging to set
     */
    @Override
    public void setSearchAllowPaging ( Boolean searchAllowPaging ) {
        this.searchAllowPaging = searchAllowPaging;
    }


    /**
     * @return the searchPageSize
     */
    @Override
    public Integer getSearchPageSize () {
        return this.searchPageSize;
    }


    /**
     * @param searchPageSize
     *            the searchPageSize to set
     */
    @Override
    public void setSearchPageSize ( Integer searchPageSize ) {
        this.searchPageSize = searchPageSize;
    }

}

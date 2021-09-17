/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
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

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.ICAPConfigurationImpl;
import eu.agno3.orchestrator.config.web.ICAPConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareContentScanConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_content_scan" )
@Audited
@DiscriminatorValue ( "filesh_cont_scan" )
public class FileshareContentScanConfigImpl extends AbstractConfigurationObject<FileshareContentScanConfig> implements FileshareContentScanConfig,
        FileshareContentScanConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 4036950696803199456L;

    private Boolean enableICAP;

    private ICAPConfigurationImpl icapConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareContentScanConfig> getType () {
        return FileshareContentScanConfig.class;
    }


    /**
     * @return the enableICAP
     */
    @Override
    public Boolean getEnableICAP () {
        return this.enableICAP;
    }


    /**
     * @param enableICAP
     *            the enableICAP to set
     */
    @Override
    public void setEnableICAP ( Boolean enableICAP ) {
        this.enableICAP = enableICAP;
    }


    /**
     * @return the icapConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = ICAPConfigurationImpl.class )
    public ICAPConfigurationMutable getIcapConfig () {
        return this.icapConfig;
    }


    /**
     * @param icapConfig
     *            the icapConfig to set
     */
    @Override
    public void setIcapConfig ( ICAPConfigurationMutable icapConfig ) {
        this.icapConfig = (ICAPConfigurationImpl) icapConfig;
    }

}

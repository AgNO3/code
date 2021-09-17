/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
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
import eu.agno3.orchestrator.config.web.RuntimeConfigurationImpl;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareAdvancedConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_advanced" )
@Audited
@DiscriminatorValue ( "filesh_adv" )
public class FileshareAdvancedConfigImpl extends AbstractConfigurationObject<FileshareAdvancedConfig> implements FileshareAdvancedConfig,
        FileshareAdvancedConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -6287243431788242442L;

    private RuntimeConfigurationImpl runtimeConfiguration;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareAdvancedConfig> getType () {
        return FileshareAdvancedConfig.class;
    }


    /**
     * @return the runtimeConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = RuntimeConfigurationImpl.class )
    public RuntimeConfigurationMutable getRuntimeConfiguration () {
        return this.runtimeConfiguration;
    }


    /**
     * @param runtimeConfiguration
     *            the runtimeConfiguration to set
     */
    @Override
    public void setRuntimeConfiguration ( RuntimeConfigurationMutable runtimeConfiguration ) {
        this.runtimeConfiguration = (RuntimeConfigurationImpl) runtimeConfiguration;
    }

}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


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
@MapAs ( OrchestratorAdvancedConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_orchestrator_advc" )
@Audited
@DiscriminatorValue ( "orchadv" )
public class OrchestratorAdvancedConfigurationImpl extends AbstractConfigurationObject<OrchestratorAdvancedConfiguration>
        implements OrchestratorAdvancedConfiguration, OrchestratorAdvancedConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5359357364267578777L;

    private RuntimeConfigurationImpl runtimeConfig;

    private String dataStorage;
    private String tempStorage;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<OrchestratorAdvancedConfiguration> getType () {
        return OrchestratorAdvancedConfiguration.class;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorAdvancedConfiguration#getRuntimeConfig()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = RuntimeConfigurationImpl.class )
    public RuntimeConfigurationMutable getRuntimeConfig () {
        return this.runtimeConfig;
    }


    /**
     * @param runtimeConfig
     *            the runtimeConfig to set
     */
    @Override
    public void setRuntimeConfig ( RuntimeConfigurationMutable runtimeConfig ) {
        this.runtimeConfig = (RuntimeConfigurationImpl) runtimeConfig;
    }


    /**
     * @return the dataStorage
     */
    @Override
    public String getDataStorage () {
        return this.dataStorage;
    }


    /**
     * @param dataStorage
     *            the dataStorage to set
     */
    @Override
    public void setDataStorage ( String dataStorage ) {
        this.dataStorage = dataStorage;
    }


    /**
     * @return the tempStorage
     */
    @Override
    public String getTempStorage () {
        return this.tempStorage;
    }


    /**
     * @param tempStorage
     *            the tempStorage to set
     */
    @Override
    public void setTempStorage ( String tempStorage ) {
        this.tempStorage = tempStorage;
    }
}

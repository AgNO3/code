/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


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
@MapAs ( OrchestratorEventLogConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_orchestrator_eventlog" )
@Audited
@DiscriminatorValue ( "orchevnt" )
public class OrchestratorEventLogConfigurationImpl extends AbstractConfigurationObject<OrchestratorEventLogConfiguration>
        implements OrchestratorEventLogConfiguration, OrchestratorEventLogConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 754759547857660110L;

    private String eventStorage;
    private Boolean writeLogFiles;

    private Boolean disableLogExpiration;
    private Long retainDays;
    private Long retainIndexedDays;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<OrchestratorEventLogConfiguration> getType () {
        return OrchestratorEventLogConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorEventLogConfiguration#getEventStorage()
     */
    @Override
    public String getEventStorage () {
        return this.eventStorage;
    }


    /**
     * @param eventStorage
     *            the eventStorage to set
     */
    @Override
    public void setEventStorage ( String eventStorage ) {
        this.eventStorage = eventStorage;
    }


    /**
     * @return the writeLogFiles
     */
    @Override
    public Boolean getWriteLogFiles () {
        return this.writeLogFiles;
    }


    /**
     * @param writeLogFiles
     *            the writeLogFiles to set
     */
    @Override
    public void setWriteLogFiles ( Boolean writeLogFiles ) {
        this.writeLogFiles = writeLogFiles;
    }


    /**
     * @return the disableLogExpiration
     */
    @Override
    public Boolean getDisableLogExpiration () {
        return this.disableLogExpiration;
    }


    /**
     * @param disableLogExpiration
     *            the disableLogExpiration to set
     */
    @Override
    public void setDisableLogExpiration ( Boolean disableLogExpiration ) {
        this.disableLogExpiration = disableLogExpiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorEventLogConfiguration#getRetainDays()
     */
    @Override
    public Long getRetainDays () {
        return this.retainDays;
    }


    /**
     * @param retainDays
     *            the retainDays to set
     */
    @Override
    public void setRetainDays ( Long retainDays ) {
        this.retainDays = retainDays;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorEventLogConfiguration#getRetainIndexedDays()
     */
    @Override
    public Long getRetainIndexedDays () {
        return this.retainIndexedDays;
    }


    /**
     * @param retainIndexedDays
     *            the retainIndexedDays to set
     */
    @Override
    public void setRetainIndexedDays ( Long retainIndexedDays ) {
        this.retainIndexedDays = retainIndexedDays;
    }
}

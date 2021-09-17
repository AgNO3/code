/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.12.2014 by mbechler
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

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_orchestrator" )
@Audited
@DiscriminatorValue ( "orchc" )
public class OrchestratorConfigurationImpl extends AbstractConfigurationInstance<OrchestratorConfiguration>
        implements OrchestratorConfiguration, OrchestratorConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1777253938087961010L;

    private OrchestratorWebConfigurationImpl webEndpointConfig;
    private OrchestratorAuthenticationConfigurationImpl authenticationConfig;
    private OrchestratorEventLogConfigurationImpl eventLogConfig;
    private OrchestratorAdvancedConfigurationImpl advancedConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<OrchestratorConfiguration> getType () {
        return OrchestratorConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration#getWebConfig()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = OrchestratorWebConfigurationImpl.class )
    public OrchestratorWebConfigurationMutable getWebConfig () {
        return this.webEndpointConfig;
    }


    /**
     * @param webEndpointConfig
     *            the webEndpointConfig to set
     */
    @Override
    public void setWebConfig ( OrchestratorWebConfigurationMutable webEndpointConfig ) {
        this.webEndpointConfig = (OrchestratorWebConfigurationImpl) webEndpointConfig;
    }


    /**
     * @return the authenticationConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = OrchestratorAuthenticationConfigurationImpl.class )
    public OrchestratorAuthenticationConfigurationMutable getAuthenticationConfig () {
        return this.authenticationConfig;
    }


    /**
     * @param authenticationConfig
     *            the authenticationConfig to set
     */
    @Override
    public void setAuthenticationConfig ( OrchestratorAuthenticationConfigurationMutable authenticationConfig ) {
        this.authenticationConfig = (OrchestratorAuthenticationConfigurationImpl) authenticationConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration#getEventLogConfig()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = OrchestratorEventLogConfigurationImpl.class )
    public OrchestratorEventLogConfigurationMutable getEventLogConfig () {
        return this.eventLogConfig;
    }


    /**
     * @param eventLogConfig
     *            the eventLogConfig to set
     */
    @Override
    public void setEventLogConfig ( OrchestratorEventLogConfigurationMutable eventLogConfig ) {
        this.eventLogConfig = (OrchestratorEventLogConfigurationImpl) eventLogConfig;
    }


    /**
     * @return the advancedConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = OrchestratorAdvancedConfigurationImpl.class )
    public OrchestratorAdvancedConfigurationMutable getAdvancedConfig () {
        return this.advancedConfig;
    }


    /**
     * @param advancedConfig
     *            the advancedConfig to set
     */
    @Override
    public void setAdvancedConfig ( OrchestratorAdvancedConfigurationMutable advancedConfig ) {
        this.advancedConfig = (OrchestratorAdvancedConfigurationImpl) advancedConfig;
    }

}

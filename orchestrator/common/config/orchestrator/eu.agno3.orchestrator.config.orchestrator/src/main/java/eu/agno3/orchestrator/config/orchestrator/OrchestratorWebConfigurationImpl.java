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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.WebEndpointConfigImpl;
import eu.agno3.orchestrator.config.web.WebEndpointConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorWebConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_orchestrator_webc" )
@Audited
@DiscriminatorValue ( "orchweb" )
public class OrchestratorWebConfigurationImpl extends AbstractConfigurationObject<OrchestratorWebConfiguration>
        implements OrchestratorWebConfiguration, OrchestratorWebConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2214619263664426825L;
    private String themeLibrary;
    private WebEndpointConfigImpl webEndpointConfig;
    private WebEndpointConfigImpl apiEndpointConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<OrchestratorWebConfiguration> getType () {
        return OrchestratorWebConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorWebConfiguration#getThemeLibrary()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorWebConfiguration#getWebEndpointConfig()
     */
    @Override
    @JoinColumn ( name = "webendpc" )
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorWebConfiguration#getApiEndpointConfig()
     */
    @Override
    @JoinColumn ( name = "apiendpc" )
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = WebEndpointConfigImpl.class )
    public WebEndpointConfigMutable getApiEndpointConfig () {
        return this.apiEndpointConfig;
    }


    /**
     * @param apiEndpointConfig
     *            the apiEndpointConfig to set
     */
    @Override
    public void setApiEndpointConfig ( WebEndpointConfigMutable apiEndpointConfig ) {
        this.apiEndpointConfig = (WebEndpointConfigImpl) apiEndpointConfig;
    }

}

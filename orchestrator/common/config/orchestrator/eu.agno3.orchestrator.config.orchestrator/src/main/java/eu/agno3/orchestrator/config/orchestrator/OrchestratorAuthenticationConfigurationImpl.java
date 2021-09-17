/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2016 by mbechler
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

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigImpl;
import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigMutable;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigImpl;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigMutable;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( OrchestratorAuthenticationConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_orchestrator_authc" )
@Audited
@DiscriminatorValue ( "orchauth" )
public class OrchestratorAuthenticationConfigurationImpl extends AbstractConfigurationObject<OrchestratorAuthenticationConfiguration>
        implements OrchestratorAuthenticationConfiguration, OrchestratorAuthenticationConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2644576613273925902L;

    private AuthenticatorsConfigImpl authenticatorsConfig;
    private StaticRolesConfigImpl roleConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<OrchestratorAuthenticationConfiguration> getType () {
        return OrchestratorAuthenticationConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.orchestrator.OrchestratorAuthenticationConfiguration#getAuthenticatorsConfig()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = AuthenticatorsConfigImpl.class )
    public AuthenticatorsConfigMutable getAuthenticatorsConfig () {
        return this.authenticatorsConfig;
    }


    /**
     * @param authenticatorsConfig
     *            the authenticatorsConfig to set
     */
    public void setAuthenticatorsConfig ( AuthenticatorsConfigMutable authenticatorsConfig ) {
        this.authenticatorsConfig = (AuthenticatorsConfigImpl) authenticatorsConfig;
    }


    /**
     * @return the roleConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = StaticRolesConfigImpl.class )
    public StaticRolesConfigMutable getRoleConfig () {
        return this.roleConfig;
    }


    /**
     * @param roleConfig
     *            the roleConfig to set
     */
    @Override
    public void setRoleConfig ( StaticRolesConfigMutable roleConfig ) {
        this.roleConfig = (StaticRolesConfigImpl) roleConfig;
    }
}

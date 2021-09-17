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
@MapAs ( FileshareAuthConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_auth" )
@Audited
@DiscriminatorValue ( "filesh_auth" )
public class FileshareAuthConfigImpl extends AbstractConfigurationObject<FileshareAuthConfig> implements FileshareAuthConfig,
        FileshareAuthConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -8926961152118896965L;

    private AuthenticatorsConfigImpl authenticators;

    private StaticRolesConfigImpl roleConfig;

    private Set<String> noSynchronizationRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareAuthConfig> getType () {
        return FileshareAuthConfig.class;
    }


    /**
     * @return the authenticators
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = AuthenticatorsConfigImpl.class )
    public AuthenticatorsConfigMutable getAuthenticators () {
        return this.authenticators;
    }


    /**
     * @param authenticators
     *            the authenticators to set
     */
    @Override
    public void setAuthenticators ( AuthenticatorsConfigMutable authenticators ) {
        this.authenticators = (AuthenticatorsConfigImpl) authenticators;
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareAuthConfig#getNoSynchronizationRoles()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_auth_role_nosync" )
    public Set<String> getNoSynchronizationRoles () {
        return this.noSynchronizationRoles;
    }


    /**
     * @param noSynchronizationRoles
     *            the noSynchronizationRoles to set
     */
    @Override
    public void setNoSynchronizationRoles ( Set<String> noSynchronizationRoles ) {
        this.noSynchronizationRoles = noSynchronizationRoles;
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
@MapAs ( StaticRolesConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_roles_static" )
@Audited
@DiscriminatorValue ( "auth_roless" )
public class StaticRolesConfigImpl extends AbstractConfigurationObject<StaticRolesConfig> implements StaticRolesConfig, StaticRolesConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private Set<RoleConfig> roles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<StaticRolesConfig> getType () {
        return StaticRolesConfig.class;
    }


    /**
     * @return the roles
     */
    @Override
    @ManyToMany ( fetch = FetchType.EAGER, targetEntity = RoleConfigImpl.class )
    public Set<RoleConfig> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    @Override
    public void setRoles ( Set<RoleConfig> roles ) {
        this.roles = roles;
    }

}

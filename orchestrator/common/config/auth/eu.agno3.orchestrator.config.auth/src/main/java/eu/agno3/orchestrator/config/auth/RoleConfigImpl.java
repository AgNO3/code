/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
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
@MapAs ( RoleConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_roles_role" )
@Audited
@DiscriminatorValue ( "auth_role" )
public class RoleConfigImpl extends AbstractConfigurationObject<RoleConfig> implements RoleConfig, RoleConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String roleId;

    private Set<String> permissions = new HashSet<>();

    private Boolean hidden;

    private Map<Locale, String> titles = new HashMap<>();
    private Map<Locale, String> descriptions = new HashMap<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RoleConfig> getType () {
        return RoleConfig.class;
    }


    /**
     * @return the roleId
     */
    @Override
    public String getRoleId () {
        return this.roleId;
    }


    /**
     * @param roleId
     *            the roleId to set
     */
    @Override
    public void setRoleId ( String roleId ) {
        this.roleId = roleId;
    }


    /**
     * @return the hidden
     */
    @Override
    public Boolean getHidden () {
        return this.hidden;
    }


    /**
     * @param hidden
     *            the hidden to set
     */
    @Override
    public void setHidden ( Boolean hidden ) {
        this.hidden = hidden;
    }


    /**
     * @return the permissions
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_roles_role_permissions" )
    public Set<String> getPermissions () {
        return this.permissions;
    }


    /**
     * @param permissions
     *            the permissions to set
     */
    @Override
    public void setPermissions ( Set<String> permissions ) {
        this.permissions = permissions;
    }


    /**
     * @return the titles
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @MapKeyColumn ( name = "locale" )
    @Column ( name = "msg" )
    @CollectionTable ( name = "config_auth_roles_role_title" )
    public Map<Locale, String> getTitles () {
        return this.titles;
    }


    /**
     * @param titles
     *            the titles to set
     */
    @Override
    public void setTitles ( Map<Locale, String> titles ) {
        this.titles = titles;
    }


    /**
     * @return the descriptions
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @MapKeyColumn ( name = "locale" )
    @Column ( name = "msg" )
    @CollectionTable ( name = "config_auth_roles_role_desc" )
    public Map<Locale, String> getDescriptions () {
        return this.descriptions;
    }


    /**
     * @param descriptions
     *            the descriptions to set
     */
    @Override
    public void setDescriptions ( Map<Locale, String> descriptions ) {
        this.descriptions = descriptions;
    }
}

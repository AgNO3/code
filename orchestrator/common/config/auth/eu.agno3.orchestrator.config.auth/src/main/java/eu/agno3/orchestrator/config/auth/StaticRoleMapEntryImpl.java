/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
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
@MapAs ( StaticRoleMapEntry.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_rolemap_static" )
@Audited
@DiscriminatorValue ( "auth_rmstat" )
public class StaticRoleMapEntryImpl extends AbstractConfigurationObject<StaticRoleMapEntry> implements StaticRoleMapEntry, StaticRoleMapEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String match;

    private Set<String> addRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<StaticRoleMapEntry> getType () {
        return StaticRoleMapEntry.class;
    }


    /**
     * @return the match
     */
    @Override
    public String getInstance () {
        return this.match;
    }


    /**
     * @param match
     *            the match to set
     */
    @Override
    public void setInstance ( String match ) {
        this.match = match;
    }


    /**
     * @return the addRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_rolemap_static_roles" )
    public Set<String> getAddRoles () {
        return this.addRoles;
    }


    /**
     * @param addRoles
     *            the addRoles to set
     */
    @Override
    public void setAddRoles ( Set<String> addRoles ) {
        this.addRoles = addRoles;
    }


    /**
     * @param sidRoles
     * @return cloned set
     */
    public static Set<StaticRoleMapEntry> clone ( Set<StaticRoleMapEntry> sidRoles ) {
        HashSet<StaticRoleMapEntry> cloned = new HashSet<>();
        for ( StaticRoleMapEntry e : sidRoles ) {
            cloned.add(clone(e));
        }
        return cloned;
    }


    /**
     * @param e
     * @return cloned object
     */
    public static StaticRoleMapEntry clone ( StaticRoleMapEntry e ) {
        StaticRoleMapEntryImpl cloned = new StaticRoleMapEntryImpl();
        cloned.setInstance(e.getInstance());
        cloned.setAddRoles(new HashSet<>(e.getAddRoles()));
        return cloned;
    }
}

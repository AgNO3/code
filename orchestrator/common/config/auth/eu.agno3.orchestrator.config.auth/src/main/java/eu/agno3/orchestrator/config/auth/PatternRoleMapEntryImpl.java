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
@MapAs ( PatternRoleMapEntry.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_rolemap_pattern" )
@Audited
@DiscriminatorValue ( "auth_rmpat" )
public class PatternRoleMapEntryImpl extends AbstractConfigurationObject<PatternRoleMapEntry> implements PatternRoleMapEntry,
        PatternRoleMapEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String pattern;
    private Set<String> addRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<PatternRoleMapEntry> getType () {
        return PatternRoleMapEntry.class;
    }


    /**
     * @return the pattern
     */
    @Override
    public String getPattern () {
        return this.pattern;
    }


    /**
     * @param pattern
     *            the pattern to set
     */
    @Override
    public void setPattern ( String pattern ) {
        this.pattern = pattern;
    }


    /**
     * @return the addRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_rolemap_pattern_roles" )
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
    public static Set<PatternRoleMapEntry> clone ( Set<PatternRoleMapEntry> sidRoles ) {
        HashSet<PatternRoleMapEntry> cloned = new HashSet<>();
        for ( PatternRoleMapEntry e : sidRoles ) {
            cloned.add(clone(e));
        }
        return cloned;
    }


    /**
     * @param e
     * @return cloned object
     */
    public static PatternRoleMapEntry clone ( PatternRoleMapEntry e ) {
        PatternRoleMapEntryImpl cloned = new PatternRoleMapEntryImpl();
        cloned.setPattern(e.getPattern());
        cloned.setAddRoles(new HashSet<>(e.getAddRoles()));
        return cloned;
    }
}

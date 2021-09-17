/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntryImpl;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntryImpl;


/**
 * @author mbechler
 *
 */
@Named ( "authRoleMappingBean" )
@ApplicationScoped
public class AuthRoleMappingBean {

    public Comparator<StaticRoleMapEntry> getStaticMappingComparator () {
        return new StaticMappingComparator();
    }


    public Comparator<PatternRoleMapEntry> getPatternMappingComparator () {
        return new PatternMappingComparator();
    }


    public StaticRoleMapEntry makeStaticMappingEntry () {
        return new StaticRoleMapEntryImpl();
    }


    public PatternRoleMapEntry makePatternMappingEntry () {
        return new PatternRoleMapEntryImpl();
    }


    public String mapStaticMapping ( Object o ) {
        if ( ! ( o instanceof StaticRoleMapEntry ) ) {
            return null;
        }

        StaticRoleMapEntry e = (StaticRoleMapEntry) o;
        return String.format("%s -> %s", e.getInstance(), e.getAddRoles()); //$NON-NLS-1$
    }


    public StaticRoleMapEntry cloneStaticMapping ( Object o ) {
        if ( ! ( o instanceof StaticRoleMapEntry ) ) {
            throw new FacesException("Incompatible type"); //$NON-NLS-1$
        }

        StaticRoleMapEntry e = (StaticRoleMapEntry) o;
        return StaticRoleMapEntryImpl.clone(e);
    }


    public String mapPatternMapping ( Object o ) {
        if ( ! ( o instanceof PatternRoleMapEntry ) ) {
            return null;
        }

        PatternRoleMapEntry e = (PatternRoleMapEntry) o;
        return String.format("%s -> %s", e.getPattern(), e.getAddRoles()); //$NON-NLS-1$
    }


    public PatternRoleMapEntry clonePatternMapping ( Object o ) {
        if ( ! ( o instanceof PatternRoleMapEntry ) ) {
            throw new FacesException("Incompatible type"); //$NON-NLS-1$
        }

        PatternRoleMapEntry e = (PatternRoleMapEntry) o;
        return PatternRoleMapEntryImpl.clone(e);
    }
}

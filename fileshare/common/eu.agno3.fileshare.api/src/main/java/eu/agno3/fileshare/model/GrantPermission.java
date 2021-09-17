/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.EnumSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public enum GrantPermission {

    /**
     * Browse share
     */
    BROWSE(1),

    /**
     * Access file contents
     */
    READ(1 + 2),

    /**
     * Create new files
     */
    UPLOAD(4),

    /**
     * 
     */
    EDIT_SELF(4 + 8),

    /**
     * Modifiy, delete existing files/directories
     */
    EDIT(4 + 8 + 16);

    private static final Logger log = Logger.getLogger(GrantPermission.class);
    private int stableId;


    /**
     * 
     */
    private GrantPermission ( int stableId ) {
        this.stableId = stableId;
    }


    /**
     * @return the stableId
     */
    public int getStableId () {
        return this.stableId;
    }


    /**
     * 
     * @param perms
     * @return an ordinal value for a set
     */
    public static int toInt ( Set<GrantPermission> perms ) {
        int res = 0;

        for ( GrantPermission perm : perms ) {
            res |= perm.getStableId();
        }

        return res;
    }


    /**
     * 
     * @param permsOrd
     * @return a set of permissions from a ordinal value
     */
    public static Set<GrantPermission> fromInt ( int permsOrd ) {
        Set<GrantPermission> perms = EnumSet.noneOf(GrantPermission.class);

        for ( GrantPermission perm : GrantPermission.values() ) {
            if ( ( ( permsOrd & perm.getStableId() ) ^ perm.stableId ) == 0 ) {
                perms.add(perm);
            }
        }

        return perms;
    }


    /**
     * 
     * @param a
     * @param b
     * @return true iff the permissions set A implies (contains) permissions set B
     */
    public static boolean implies ( Set<GrantPermission> a, Set<GrantPermission> b ) {
        int ordA = toInt(a);
        int ordB = toInt(b);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("%d vs %d : %d : %d", ordA, ordB, ( ordA & ordB ), ( ordB ^ ( ordA & ordB ) ))); //$NON-NLS-1$
        }
        return ( ordB ^ ( ordA & ordB ) ) == 0;
    }
}

/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 10, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.util.EnumSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public enum RouteFlags {

    /**
     * Route is active
     */
    UP,

    /**
     * Route with gateway
     */
    GATEWAY,

    /**
     * Is a host route
     */
    HOST,

    /**
     * Route was set up dynamically
     */
    DYNAMIC,

    /**
     * Route was modified dynamically
     */
    DYNAMIC_MODIFIED,

    /**
     * Reject route
     */
    REJECT,

    /**
     * Anycast route
     */
    ANYCAST,

    /**
     * Fallback route
     */
    FALLBACK,

    /**
     * Route learned via neighborhood discovery
     */
    V6_ND,

    /**
     * Route learned via RA addrconf
     */
    V6_RA_ADDRCONF,

    /**
     * 
     */
    V6_RA_PREFIX_RT,

    /**
     * 
     */
    V6_RA_ROUTEINFO,

    /**
     * V6 link local
     */
    LINKLOCAL;

    /**
     * @param flags
     * @return set of flags
     */
    public static Set<RouteFlags> parseFlags ( long flags ) {
        EnumSet<RouteFlags> set = EnumSet.noneOf(RouteFlags.class);

        if ( ( flags & 0x1 ) > 0 ) {
            set.add(UP);
        }
        if ( ( flags & 0x2 ) > 0 ) {
            set.add(GATEWAY);
        }
        if ( ( flags & 0x4 ) > 0 ) {
            set.add(HOST);
        }
        if ( ( flags & 0x10 ) > 0 ) {
            set.add(DYNAMIC);
        }
        if ( ( flags & 0x20 ) > 0 ) {
            set.add(DYNAMIC_MODIFIED);
        }
        if ( ( flags & 0x200 ) > 0 ) {
            set.add(REJECT);
        }

        if ( ( flags & 0x20000 ) > 0 ) {
            set.add(FALLBACK);
        }
        if ( ( flags & 0x40000 ) > 0 ) {
            set.add(V6_RA_ADDRCONF);
        }

        if ( ( flags & 0x80000 ) > 0 ) {
            set.add(V6_RA_PREFIX_RT);
        }
        if ( ( flags & 0x100000 ) > 0 ) {
            set.add(ANYCAST);
        }
        if ( ( flags & 0x800000 ) > 0 ) {
            set.add(V6_RA_ROUTEINFO);
        }

        return set;
    }

}

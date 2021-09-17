/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 * 
 */
public class RouteEntryComparator implements Comparator<StaticRouteEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8375865444634693003L;


    /**
     * 
     */
    public RouteEntryComparator () {}


    @Override
    public int compare ( StaticRouteEntry o1, StaticRouteEntry o2 ) {

        if ( o1.getTarget() == null && o2.getTarget() == null ) {
            return 0;
        }
        else if ( o1.getTarget() == null ) {
            return -1;
        }
        else if ( o2.getTarget() == null ) {
            return 1;
        }

        return comparePrefixLengthAndAddress(o1, o2);
    }


    /**
     * @param o1
     * @param o2
     * @return
     */
    protected int comparePrefixLengthAndAddress ( StaticRouteEntry o1, StaticRouteEntry o2 ) {
        int res = Integer.compare(o1.getTarget().getPrefixLength(), o2.getTarget().getPrefixLength());

        if ( res != 0 ) {
            return res;
        }

        return compareAddresses(o1, o2);
    }


    /**
     * @param o1
     * @param o2
     * @return
     */
    protected int compareAddresses ( StaticRouteEntry o1, StaticRouteEntry o2 ) {
        int res;
        NetworkAddress a1 = o1.getTarget().getAddress();
        NetworkAddress a2 = o2.getTarget().getAddress();

        res = Integer.compare(a1.getAddress().length, a2.getAddress().length);

        if ( res != 0 ) {
            return res;
        }

        return compareAddressBytes(a1, a2);
    }


    /**
     * @param a1
     * @param a2
     * @return
     */
    protected int compareAddressBytes ( NetworkAddress a1, NetworkAddress a2 ) {
        int res;
        for ( int i = 0; i < a1.getAddress().length; i++ ) {
            res = Short.compare(a1.getAddress()[ i ], a2.getAddress()[ i ]);
            if ( res != 0 ) {
                return res;
            }
        }

        return 0;
    }
}
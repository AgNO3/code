/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 10, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.route;


import java.util.Comparator;

import eu.agno3.orchestrator.system.info.network.RouteEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public class RouteEntryComparator implements Comparator<RouteEntry> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( RouteEntry o1, RouteEntry o2 ) {

        int res = Integer.compare(o1.getNetwork().getPrefixLength(), o2.getNetwork().getPrefixLength());

        if ( res != 0 ) {
            // shorter prefixes first
            return res;
        }

        res = Integer.compare(o1.getNetwork().getAddress().getBitSize(), o2.getNetwork().getAddress().getBitSize());
        if ( res != 0 ) {
            // V4 before V6
            return res;
        }

        return compareAddressBytes(o1.getNetwork().getAddress(), o2.getNetwork().getAddress());
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

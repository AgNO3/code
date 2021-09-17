/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
final class StaticAddressComparator implements Comparator<NetworkSpecification>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6746702858378251034L;


    /**
     * 
     */
    public StaticAddressComparator () {}


    @Override
    public int compare ( NetworkSpecification a, NetworkSpecification b ) {

        if ( ( a == null || a.getAddress() == null ) && ( b == null || b.getAddress() == null ) ) {
            return 0;
        }
        else if ( a == null || a.getAddress() == null ) {
            return -1;
        }
        else if ( b == null || b.getAddress() == null ) {
            return 1;
        }

        return compareAddresses(a, b);
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int compareAddresses ( NetworkSpecification a, NetworkSpecification b ) {
        int res = Integer.compare(a.getAddress().getBitSize(), b.getAddress().getBitSize());

        if ( res != 0 ) {
            return res;
        }

        return a.getAddress().getCanonicalForm().compareTo(b.getAddress().getCanonicalForm());
    }
}
/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2015 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import java.io.Serializable;
import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */

@ApplicationScoped
@Named ( "networkTypeUtil" )
public class NetworkTypeUtil {

    /**
     * 
     * @return a comparator
     */
    public static Comparator<NetworkSpecification> getNetworkSpecificationComparator () {
        return new NetworkSpecificationComparator();
    }


    /**
     * @return a comparator
     */
    public static Comparator<NetworkAddress> getNetworkAddressComparator () {
        return new NetworkAddressComparator();
    }

    /**
     * 
     * @author mbechler
     *
     */
    public static class NetworkSpecificationComparator implements Comparator<NetworkSpecification>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -396161831303732950L;


        /**
         * {@inheritDoc}
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare ( NetworkSpecification o1, NetworkSpecification o2 ) {

            if ( o1 == null && o2 == null ) {
                return 0;
            }
            else if ( o1 == null ) {
                return -1;
            }
            else if ( o2 == null ) {
                return 0;
            }

            NetworkAddress a1 = o1.getAddress();
            NetworkAddress a2 = o2.getAddress();

            int res = getNetworkAddressComparator().compare(a1, a2);

            if ( res != 0 ) {
                return res;
            }

            return Integer.compare(o1.getPrefixLength(), o2.getPrefixLength());
        }
    }

    /**
     * 
     * @author mbechler
     *
     */
    public static class NetworkAddressComparator implements Comparator<NetworkAddress>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 6550370248908400789L;


        /**
         * {@inheritDoc}
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare ( NetworkAddress o1, NetworkAddress o2 ) {

            if ( o1 == null && o2 == null ) {
                return 0;
            }
            else if ( o1 == null ) {
                return -1;
            }
            else if ( o2 == null ) {
                return 0;
            }

            int res = Integer.compare(o1.getBitSize(), o2.getBitSize());
            if ( res != 0 ) {
                return res;
            }

            for ( int i = 0; i < o1.getBitSize() / 8; i++ ) {
                res = Integer.compare(o1.getAddress()[ i ], o2.getAddress()[ i ]);
                if ( res != 0 ) {
                    return res;
                }
            }

            return 0;
        }

    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.util.Arrays;

import eu.agno3.runtime.util.sid.SID;

import jcifs.dcerpc.rpc.sid_t;


/**
 * @author mbechler
 *
 */
public final class JCIFSSID {

    /**
     * 
     */
    private JCIFSSID () {}


    /**
     * @param sid
     * @return a SID from a JCIFS sid
     */
    public static SID fromJCIFS ( sid_t sid ) {
        long idAuthority = ( sid.identifier_authority[ 0 ] & (long) 0xFF ) << 40L | ( sid.identifier_authority[ 1 ] & (long) 0xFF ) << 32L
                | ( sid.identifier_authority[ 2 ] & (long) 0xFF ) << 24L | ( sid.identifier_authority[ 3 ] & (long) 0xFF ) << 16L
                | ( sid.identifier_authority[ 4 ] & (long) 0xFF ) << 8L | ( sid.identifier_authority[ 5 ] & 0xFF );

        return new SID(sid.revision, idAuthority, Arrays.copyOf(sid.sub_authority, sid.sub_authority_count));
    }

}

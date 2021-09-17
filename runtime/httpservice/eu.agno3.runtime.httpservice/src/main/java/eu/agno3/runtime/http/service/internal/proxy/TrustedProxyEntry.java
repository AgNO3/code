/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal.proxy;


import java.util.Arrays;

import eu.agno3.runtime.util.ip.IpMatcher;
import eu.agno3.runtime.util.ip.IpUtil;


class TrustedProxyEntry {

    private short[] addr;
    private int prefixLength;


    /**
     * @param addr
     * @param prefixLength
     * 
     */
    public TrustedProxyEntry ( short[] addr, int prefixLength ) {
        if ( addr != null ) {
            this.addr = Arrays.copyOf(addr, addr.length);
        }
        this.prefixLength = prefixLength;
    }


    /**
     * 
     * @param toMatch
     * @return whether the entry matches
     */
    public boolean match ( short[] toMatch ) {
        return IpMatcher.match(toMatch, this.addr, this.prefixLength);
    }


    /**
     * @param trustedProxy
     * @return a instance from string
     */
    public static TrustedProxyEntry fromString ( String trustedProxy ) {
        int prefixSep = trustedProxy.indexOf('/');
        String addr = trustedProxy;
        int prefixLength;

        if ( prefixSep < 0 ) {
            prefixLength = 32;
        }
        else {
            prefixLength = Integer.parseInt(addr.substring(prefixSep + 1));
            addr = addr.substring(0, prefixSep);
        }
        return new TrustedProxyEntry(IpUtil.parse(addr), prefixLength);
    }
}
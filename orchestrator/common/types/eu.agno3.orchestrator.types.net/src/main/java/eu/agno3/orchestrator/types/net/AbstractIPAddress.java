/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.io.Serializable;

import eu.agno3.runtime.util.ip.IpUtil;


/**
 * IP Address storage base
 * 
 * Unfortunately Java's InetAddress does name lookups, this is not appropriate for storage.
 * 
 * @author mbechler
 * 
 */
public abstract class AbstractIPAddress implements NetworkAddress, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9018889527198468273L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getReadableForm()
     */
    @Override
    public String getReadableForm () {
        return this.getCanonicalForm();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.getReadableForm();
    }


    /**
     * 
     * @param bytes
     * @return the address
     */
    public static AbstractIPAddress fromBytes ( short[] bytes ) {
        if ( bytes.length == 16 ) {
            IPv6Address addr = new IPv6Address();
            addr.setAddress(bytes);
            return addr;
        }
        else if ( bytes.length == 4 ) {
            IPv4Address addr = new IPv4Address();
            addr.setAddress(bytes);
            return addr;
        }
        throw new IllegalArgumentException("Address must have either 16 (v6) or 4 (v4) byte length"); //$NON-NLS-1$
    }


    /**
     * 
     * @param address
     * @return the parsed address
     */
    public static AbstractIPAddress parse ( String address ) {
        if ( IpUtil.isV4Address(address) ) {
            return IPv4Address.parseV4Address(address);
        }

        if ( IpUtil.isV6Address(address) ) {
            return IPv6Address.parseV6Address(address);
        }

        throw new IllegalArgumentException("Could not dermine address type for " + address); //$NON-NLS-1$
    }


    /**
     * 
     * @param address
     * @return whether the given address is parseable as an ip address
     */
    public static boolean isIPAddress ( String address ) {
        return IpUtil.isV4Address(address) || IpUtil.isV6Address(address);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isUnicast()
     */
    @Override
    public boolean isUnicast () {
        return !this.isUnspecified() && !this.isBroadcast() && !this.isMulticast() && !this.isAnycast();
    }

}

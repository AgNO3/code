/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.util.net;


import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class LocalHostUtil {

    private static final Logger log = Logger.getLogger(LocalHostUtil.class);


    /**
     * 
     */
    private LocalHostUtil () {}


    /**
     * @return the primary network address for this host
     */
    public static InetAddress guessPrimaryAddress () {
        NetworkInterface found = selectInterface();

        if ( found == null ) {
            log.warn("Could not find any interfaces, cannot determine canonical serverName"); //$NON-NLS-1$
            return null;
        }

        InetAddress addr = selectAddress(found);
        if ( addr == null ) {
            log.warn("No primary address found"); //$NON-NLS-1$
            return null;
        }

        return addr;
    }


    /**
     * @return the primary hostname for this host (might return the address if there is no hostname)
     */
    public static String guessPrimaryHostName () {
        InetAddress addr = guessPrimaryAddress();

        if ( addr == null ) {
            return null;
        }

        String canonicalHostName = addr.getCanonicalHostName();

        if ( canonicalHostName.indexOf('.') < 0 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Hostname does not contain a domain name, returning address instead " + canonicalHostName); //$NON-NLS-1$
            }
            return addr.getHostAddress();
        }

        return canonicalHostName;
    }


    /**
     * @param found
     */
    private static InetAddress selectAddress ( NetworkInterface found ) {
        List<InterfaceAddress> addrs = found.getInterfaceAddresses();

        if ( addrs.size() == 1 ) {
            return addrs.get(0).getAddress();
        }

        InetAddress bestV4 = null;
        InetAddress bestV6 = null;

        for ( InterfaceAddress ifAddr : addrs ) {
            InetAddress ifAddrReal = ifAddr.getAddress();

            if ( !isGlobalOrSiteUnicastAddress(ifAddrReal) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Not a routable unicast address " + ifAddrReal.getHostAddress()); //$NON-NLS-1$
                }
                continue;
            }

            if ( ifAddrReal.getAddress().length == 4 ) {
                // ipv4 address
                bestV4 = ifAddrReal;
            }
            else if ( ifAddrReal.getAddress().length == 16 ) {
                // ipv6 address
                bestV6 = ifAddrReal;
            }
            else if ( log.isDebugEnabled() ) {
                // unknown address type, ignore
                log.debug("Unknown address type " + ifAddrReal.getHostAddress()); //$NON-NLS-1$
            }
        }

        if ( bestV4 != null ) {
            // prefer V4 as this might be used user facing
            return bestV4;
        }

        return bestV6;
    }


    /**
     * @param ifAddrReal
     * @return
     */
    private static boolean isGlobalOrSiteUnicastAddress ( InetAddress addr ) {
        return ! ( addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isLoopbackAddress() || addr.isMCGlobal() || addr.isMCLinkLocal()
                || addr.isMCNodeLocal() || addr.isMCOrgLocal() || addr.isMCSiteLocal() || addr.isMulticastAddress() );
    }


    /**
     * @return
     */
    private static NetworkInterface selectInterface () {
        NetworkInterface found = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while ( networkInterfaces.hasMoreElements() ) {
                NetworkInterface iface = networkInterfaces.nextElement();

                if ( iface.isLoopback() || !iface.isUp() || iface.isPointToPoint() || iface.getInterfaceAddresses().isEmpty() ) {
                    continue;
                }

                if ( iface.getInterfaceAddresses().size() == 1 ) {
                    InetAddress onlyAddr = iface.getInterfaceAddresses().get(0).getAddress();
                    if ( onlyAddr instanceof Inet6Address && onlyAddr.isLinkLocalAddress() ) {
                        continue;
                    }
                }

                if ( found != null ) {
                    log.warn("Multiple usable interfaces found, selecting first one found: " + found.getDisplayName()); //$NON-NLS-1$
                    break;
                }
                found = iface;
            }
        }
        catch ( SocketException e ) {
            log.warn("Failed to enumerate interfaces", e); //$NON-NLS-1$
            return null;
        }
        return found;
    }
}

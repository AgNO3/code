/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl;


import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.info.network.DHCPAssociationType;
import eu.agno3.orchestrator.system.info.network.DHCPLeaseStatus;
import eu.agno3.orchestrator.system.info.network.DHCPOption;
import eu.agno3.orchestrator.system.info.network.DHCPOptions;
import eu.agno3.orchestrator.system.info.network.LeaseEntry;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;
import eu.agno3.orchestrator.system.info.network.RouteEntry;
import eu.agno3.orchestrator.system.info.network.V4ConfigurationType;
import eu.agno3.orchestrator.system.info.network.V6ConfigurationType;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DHCPAddressEntry;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DHCPAssociation;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DHCPInterfaceStatus;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DhclientLeaseParser;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DhclientLeaseParserException;
import eu.agno3.orchestrator.system.info.network.java.impl.dns.DnsServerParser;
import eu.agno3.orchestrator.system.info.network.java.impl.route.RouteInfoProvider;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( NetworkInformation.class )
public class JavaNetworkInformation implements NetworkInformation {

    /**
     * 
     */
    private static final long serialVersionUID = -5792535789143044023L;
    private static final Logger log = Logger.getLogger(JavaNetworkInformation.class);

    private static final RouteInfoProvider ROUTE = new RouteInfoProvider();
    private static final DhclientLeaseParser DHCP_LEASE = new DhclientLeaseParser();
    private static final DnsServerParser DNS = new DnsServerParser();

    private Map<String, String> aliasMap = new HashMap<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformation#getNetworkInterfaces()
     */
    @Override
    public List<NetworkInterface> getNetworkInterfaces () {
        List<NetworkInterface> res = new ArrayList<>();
        Enumeration<java.net.NetworkInterface> it;
        try {
            it = java.net.NetworkInterface.getNetworkInterfaces();
        }
        catch ( SocketException e ) {
            log.debug("Exception in getNetworkInterfaces():", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

        while ( it.hasMoreElements() ) {
            java.net.NetworkInterface iface = it.nextElement();

            String ifName = iface.getName();
            V4ConfigurationType v4type = checkV4Autoconf(ifName, iface.getInterfaceAddresses().isEmpty());
            V6ConfigurationType v6type = checkV6Autoconf(ifName);

            JavaNetworkInterfaceProxy proxy = new JavaNetworkInterfaceProxy(iface, this.aliasMap);
            proxy.setV4ConfigurationType(v4type);
            proxy.setV6ConfigurationType(v6type);

            if ( v4type == V4ConfigurationType.DHCP ) {
                addV4Leases(proxy, ifName);
            }

            if ( v6type == V6ConfigurationType.DHCP ) {
                addV6Leases(proxy, ifName);
            }

            res.add(proxy);
        }

        return res;
    }


    /**
     * @param proxy
     * @param ifName
     */
    private static void addV6Leases ( JavaNetworkInterfaceProxy proxy, String ifName ) {
        Path leasePath = Paths.get(String.format("/var/lib/dhcp/dhclient6.%s.leases", ifName)); //$NON-NLS-1$
        parseLeases(proxy, ifName, leasePath);
    }


    /**
     * @param ifName
     * @param proxy
     * 
     */
    private static void addV4Leases ( JavaNetworkInterfaceProxy proxy, String ifName ) {
        Path leasePath = Paths.get(String.format("/var/lib/dhcp/dhclient.%s.leases", ifName)); //$NON-NLS-1$
        parseLeases(proxy, ifName, leasePath);
    }


    /**
     * @param ifName
     * @param leasePath
     */
    private static void parseLeases ( JavaNetworkInterfaceProxy proxy, String ifName, Path leasePath ) {
        try {
            if ( !Files.exists(leasePath) ) {
                return;
            }
            Map<String, DHCPInterfaceStatus> parse = DHCP_LEASE.parse(leasePath);
            DHCPInterfaceStatus ifs = parse.get(ifName);
            if ( ifs == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("No lease found for " + ifName); //$NON-NLS-1$
                }
                return;
            }

            proxy.getDhcpLeases().addAll(transformLeases(ifs));
        }
        catch (
            IOException |
            DhclientLeaseParserException e ) {
            log.warn("Failed to parse leases " + leasePath, e); //$NON-NLS-1$
        }
    }


    /**
     * @param ifs
     * @return
     */
    private static Collection<? extends LeaseEntry> transformLeases ( DHCPInterfaceStatus ifs ) {
        List<LeaseEntry> leases = new ArrayList<>();

        for ( DHCPAssociation assoc : ifs.getAssociations() ) {
            for ( DHCPAddressEntry ae : assoc.getAddresses() ) {

                LeaseEntry le = new LeaseEntry();

                le.setNetworkSpecification(
                    new NetworkSpecification(
                        ae.getNetworkAddress(),
                        (short) ( ae.getPrefixLength() >= 0 ? ae.getPrefixLength() : ae.getNetworkAddress().getBitSize() )));

                le.setExpiresTime(ae.getExpiresTime() != null ? ae.getExpiresTime() : assoc.getExpireTime());
                le.setRebindTime(assoc.getRebindTime());
                le.setRenewTime(assoc.getRenewTime());

                DHCPLeaseStatus status = ifs.getStatus();
                if ( le.getExpiresTime() != null && le.getExpiresTime().isBeforeNow() ) {
                    status = DHCPLeaseStatus.EXPIRED;
                }

                le.setStatus(status);
                le.setAssociationType(assoc.getAssociationType());

                DHCPOptions opts = new DHCPOptions();
                opts.addAll(ae.getOptions());
                opts.addAll(assoc.getOptions());
                opts.addAll(ifs.getOptions());
                le.setOptions(opts);

                if ( le.getAssociationType() == DHCPAssociationType.V4 ) {
                    DHCPOption mask = le.getOptions().get("subnet-mask"); //$NON-NLS-1$
                    if ( mask != null ) {
                        le.getNetworkSpecification().setPrefixLength(toPrefixLength(mask.getSingleValue()));
                    }
                }

                leases.add(le);
            }
        }

        return leases;
    }


    /**
     * @param singleValue
     * @return
     */
    private static short toPrefixLength ( String singleValue ) {
        IPv4Address parseV4Address = IPv4Address.parseV4Address(singleValue);
        short[] address = parseV4Address.getAddress();
        for ( int o = 0; o < 4; o++ ) {
            short aoct = address[ o ];
            for ( int i = 7; i >= 0; i-- ) {
                if ( ( aoct & ( 1 << i ) ) == 0 ) {
                    return (short) ( 8 * o + ( 8 - ( i + 1 ) ) );
                }
            }
        }
        return 32;
    }


    /**
     * 
     */
    private V4ConfigurationType checkV4Autoconf ( String ifName, boolean noAddr ) {
        String pidFile = String.format("/run/dhclient.%s.pid", ifName); //$NON-NLS-1$
        if ( checkRunning(pidFile) ) {
            return V4ConfigurationType.DHCP;
        }

        if ( noAddr ) {
            return V4ConfigurationType.DISABLED;
        }

        return V4ConfigurationType.STATIC;
    }


    private V6ConfigurationType checkV6Autoconf ( String ifName ) {
        String disabledFile = String.format("/proc/sys/net/ipv6/conf/%s/disable_ipv6", ifName); //$NON-NLS-1$
        if ( getInt(Paths.get(disabledFile)) != 0 ) {
            // disabled
            return V6ConfigurationType.DISABLED;
        }

        String pidFile = String.format("/run/dhclient6.%s.pid", ifName); //$NON-NLS-1$
        if ( checkRunning(pidFile) ) {
            // DHCPv6
            return V6ConfigurationType.DHCP;
        }

        String raFile = String.format("/proc/sys/net/ipv6/conf/%s/accept_ra", ifName); //$NON-NLS-1$
        if ( getInt(Paths.get(raFile)) == 1 ) {
            // Stateless autoconfig
            return V6ConfigurationType.STATELESS;
        }

        return V6ConfigurationType.STATIC;
    }


    /**
     * @param pidFile
     */
    private boolean checkRunning ( String pidFile ) {
        Path pf = Paths.get(pidFile);
        if ( !Files.exists(pf) ) {
            return false;
        }
        int pid = getInt(pf);
        if ( pid < 0 ) {
            log.debug("Failed to find PID"); //$NON-NLS-1$
            return false;
        }
        return Files.exists(Paths.get("/proc/", String.valueOf(pid))); //$NON-NLS-1$
    }


    /**
     * @param pf
     * @param pid
     * @return
     * @throws IOException
     */
    int getInt ( Path pf ) {
        try {
            List<String> lines = Files.readAllLines(pf);
            for ( String line : lines ) {
                line = line.trim();
                if ( StringUtils.isEmpty(line) ) {
                    continue;
                }
                return Integer.parseUnsignedInt(line);
            }
            return -1;
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            log.debug("Failed to parse file", e); //$NON-NLS-1$
            return -1;
        }
    }


    @Override
    public List<RouteEntry> getRoutes () {
        return ROUTE.getRoutes();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformation#getDnsServers()
     */
    @Override
    public List<NetworkAddress> getDnsServers () {
        try {
            return DNS.parse();
        }
        catch ( IOException e ) {
            log.warn("Failed to get dns servers", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @param name
     * @param alias
     */
    public void addAlias ( String name, String alias ) {
        this.aliasMap.put(name, alias);
    }

}

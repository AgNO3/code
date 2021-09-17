/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry;
import eu.agno3.orchestrator.config.web.agent.NetworkConfigUtil;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.DebugOutputHandler;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.exec.Kill;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.remove.Remove;
import eu.agno3.orchestrator.system.base.units.service.RestartService;
import eu.agno3.orchestrator.system.info.SystemInformationException;


/**
 * @author mbechler
 *
 */
public final class NetworkConfigJobBuilder {

    private static final String DHCLIENT_STOP = "-x"; //$NON-NLS-1$
    private static final String DHCLIENT_PF = "-pf"; //$NON-NLS-1$
    private static final String DHCLIENT_LF = "-lf"; //$NON-NLS-1$
    private static final String DHCLIENT_BOOTSTRAP_CONFIG = "/etc/dhcp/dhclient.conf"; //$NON-NLS-1$
    private static final String DHCLIENT_CF = "-cf"; //$NON-NLS-1$
    private static final String DHCLIENT = "/sbin/dhclient"; //$NON-NLS-1$
    private static final String IFUP = "/sbin/ifup"; //$NON-NLS-1$
    private static final String IFDOWN = "/sbin/ifdown"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(NetworkConfigJobBuilder.class);

    /**
     * 
     */
    private static final String NETWORK_INTERFACE = "network-interface"; //$NON-NLS-1$
    private static final String INTERFACES = "/etc/network/interfaces.d/interfaces.cfg"; //$NON-NLS-1$
    private static final String ETC_SYSCTL_NET_CONF = "/etc/sysctl.d/60-netdev.conf"; //$NON-NLS-1$
    private static final String DHCP_V4_CONF = "/etc/dhcp/dhclient-v4.conf"; //$NON-NLS-1$
    private static final String DHCP_V6_CONF = "/etc/dhcp/dhclient-v6.conf"; //$NON-NLS-1$
    private static final String BOOTSTRAP_INTERFACES = "/etc/network/interfaces.d/bootstrap.cfg"; //$NON-NLS-1$

    private static final int CHANGE_ADD = 1;
    private static final int CHANGE_REMOVE = 2;
    private static final int IF_MATCHER_CHANGED = 4;
    private static final int IF_SETTINGS_CHANGED = 8;
    private static final int IF_CONFTYPE_V4_CHANGED = 16;
    private static final int IF_CONFTYPE_V6_CHANGED = 32;
    private static final int IF_STATICADDR_CHANGED = 64;
    private static final int ROUTE_ADDED = 128;
    private static final int ROUTE_REMOVED = 256;
    private static final int ROUTE_MODIFIED = 512;
    private static final int IF_DHCP_SETTINGS_CHANGED = 1024;


    /**
     * 
     */
    private NetworkConfigJobBuilder () {}


    /**
     * @param b
     * @param ctx
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     * @throws SystemInformationException
     * @throws MatcherException
     */
    public static void build ( JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws InvalidParameterException, UnitInitializationFailedException, IOException, MatcherException, SystemInformationException,
            JobBuilderException {

        Map<String, Integer> modifiedAliases = getInterfacesOrInterfaceRoutesChanges(ctx);
        boolean changedNetworkSettings = !modifiedAliases.isEmpty();

        // disable removed interfaces
        for ( Entry<String, Integer> e : modifiedAliases.entrySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Network device %s change %d", e.getKey(), e.getValue())); //$NON-NLS-1$
            }
            if ( ( e.getValue() & CHANGE_REMOVE ) != 0 ) {
                b.add(Exec.class).cmd(IFDOWN).args(e.getKey());
                // b.add(StopService.class).service(NETWORK_INTERFACE).instance(e.getKey());
            }
        }

        Map<String, String> interfaceAliasMap = makeInterfaceAliasMap(ctx);

        changedNetworkSettings |= configureDHCP(b, ctx, modifiedAliases, interfaceAliasMap);
        configureSysctl(b, ctx, modifiedAliases);

        // remove bootstrap interface file
        boolean fromBootstrap = Files.exists(Paths.get(BOOTSTRAP_INTERFACES));
        if ( fromBootstrap ) {
            b.add(Remove.class).file(BOOTSTRAP_INTERFACES).getExecutionUnit();
        }

        Set<String> stopped = new HashSet<>();

        // need to stop the old interfaces before changing
        for ( Entry<String, Integer> e : modifiedAliases.entrySet() ) {
            if ( fromBootstrap || ( e.getValue() & ( IF_CONFTYPE_V4_CHANGED | IF_CONFTYPE_V6_CHANGED ) ) != 0 ) {
                String hwDev = interfaceAliasMap.get(e.getKey());
                killBootstrapDHCPV4(b, hwDev);
                killBootstrapDHCPV6(b, hwDev);
                b.add(Exec.class).cmd(IFDOWN).args(e.getKey());
                stopped.add(e.getKey());
            }
        }

        // write interface configuration
        b.add(Contents.class).file(INTERFACES).content(ctx.tpl(INTERFACES, makeInterfaceConfigExtraContext(ctx)))
                .perms(HostConfigJobBuilder.WORLD_READABLE_CONFIG).runIf(changedNetworkSettings || ctx.job().getApplyInfo().isForce());

        // restart changed interfaces and start new interfaces
        for ( Entry<String, Integer> e : modifiedAliases.entrySet() ) {
            if ( ( e.getValue() & CHANGE_ADD ) != 0 || fromBootstrap
                    || ( e.getValue() & ( IF_CONFTYPE_V4_CHANGED | IF_CONFTYPE_V6_CHANGED ) ) != 0 ) {
                b.add(Exec.class).cmd(IFUP).args(e.getKey());
            }
            else if ( e.getValue() == IF_DHCP_SETTINGS_CHANGED ) {
                if ( reloadDhclient(b, ctx, e.getKey(), interfaceAliasMap.get(e.getKey())) ) {
                    continue;
                }
            }

            b.add(RestartService.class).service(NETWORK_INTERFACE).instance(e.getKey());
            if ( !stopped.contains(e.getKey()) ) {
                b.add(Exec.class).cmd(IFDOWN).args(e.getKey());
            }
            b.add(Exec.class).cmd(IFUP).args(e.getKey());
        }
    }


    /**
     * @param b
     * @param ctx
     * @param intf
     * @param hwIntf
     * @return
     * @throws UnitInitializationFailedException
     */
    @SuppressWarnings ( "nls" )
    private static boolean reloadDhclient ( @NonNull JobBuilder b,
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, String intf, String hwIntf )
                    throws UnitInitializationFailedException {
        // only need to reload the dhcp, but this is not as easy as it sounds
        String v4lease = String.format("/var/lib/dhcp/dhclient.%s.leases", hwIntf);
        String v6lease = String.format("/var/lib/dhcp/dhclient6.%s.leases", hwIntf);
        String v4pid = String.format("/run/dhclient.%s.pid", hwIntf);
        String v6pid = String.format("/run/dhclient6.%s.pid", hwIntf);
        int v4p = getDhclientPid(Paths.get(v4pid));
        int v6p = getDhclientPid(Paths.get(v6pid));
        String[] extraOpts;

        if ( ctx.cfg().getResolverConfiguration().getAutoconfigureDns() ) {
            extraOpts = new String[0];
        }
        else {
            extraOpts = new String[] {
                "-e", "DISABLE_RESOLVCONF=true"
            };
        }

        if ( v4p > 0 ) {
            b.add(Kill.class).signal(Kill.HUP).pid(v4p).waitForExit();
            b.add(Exec.class).cmd(DHCLIENT).args(ArrayUtils.addAll(extraOpts, "-1", "-4", "-cf", DHCP_V4_CONF, "-pf", v4pid, "-lf", v4lease, hwIntf));
        }
        if ( v6p > 0 ) {
            b.add(Kill.class).signal(Kill.HUP).pid(v6p).waitForExit();
            b.add(Exec.class).cmd(DHCLIENT).args(ArrayUtils.addAll(extraOpts, "-1", "-6", "-cf", DHCP_V6_CONF, "-pf", v6pid, "-lf", v6lease, hwIntf));
        }
        return v4p > 0 || v6p > 0;
    }


    /**
     * @param pidfile
     * @return
     */
    private static int getDhclientPid ( Path pidfile ) {
        int pid = -1;
        try {
            List<String> lines = Files.readAllLines(pidfile);
            if ( lines != null && lines.size() > 0 ) {
                pid = Integer.parseInt(lines.get(0).trim());
            }
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            log.debug("Invalid pid file", e); //$NON-NLS-1$
        }
        return pid;
    }


    /**
     * @param b
     * @param hwDev
     * @throws UnitInitializationFailedException
     */
    private static void killBootstrapDHCPV6 ( JobBuilder b, String hwDev ) throws UnitInitializationFailedException {
        String v6PidFile = String.format("/run/dhclient6.bootstrap.%s.pid", hwDev); //$NON-NLS-1$
        String v6LeaseFile = String.format("/var/lib/dhcp/dhclient6.%s.leases", hwDev); //$NON-NLS-1$
        if ( Files.exists(Paths.get(v6PidFile)) ) {
            b.add(Exec.class).cmd(DHCLIENT).args(
                DHCLIENT_STOP,
                "-6", //$NON-NLS-1$
                DHCLIENT_CF,
                DHCLIENT_BOOTSTRAP_CONFIG,
                DHCLIENT_LF,
                v6LeaseFile,
                DHCLIENT_PF,
                v6PidFile,
                hwDev);
        }
    }


    /**
     * @param b
     * @param hwDev
     * @throws UnitInitializationFailedException
     */
    private static void killBootstrapDHCPV4 ( JobBuilder b, String hwDev ) throws UnitInitializationFailedException {
        String v4PidFile = String.format("/run/dhclient.bootstrap.%s.pid", hwDev); //$NON-NLS-1$
        String v4LeaseFile = String.format("/var/lib/dhcp/dhclient.%s.leases", hwDev); //$NON-NLS-1$
        if ( Files.exists(Paths.get(v4PidFile)) ) {
            b.add(Exec.class).cmd(DHCLIENT).args(
                DHCLIENT_STOP,
                "-4", //$NON-NLS-1$
                DHCLIENT_CF,
                DHCLIENT_BOOTSTRAP_CONFIG,
                DHCLIENT_LF,
                v4LeaseFile,
                DHCLIENT_PF,
                v4PidFile,
                hwDev);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param modifiedAliases
     * @param interfaceAliasMap
     * @throws MatcherException
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    private static boolean configureDHCP ( JobBuilder b, ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx,
            Map<String, Integer> modifiedAliases, Map<String, String> interfaceAliasMap )
                    throws MatcherException, InvalidParameterException, UnitInitializationFailedException, IOException {

        boolean changedDnsAutoConfig = ctx.changed(ctx.match().getResolverConfiguration().getAutoconfigureDns());
        boolean changedDhcpSettings = changedDnsAutoConfig
                || ctx.changed(ctx.match().getNetworkConfiguration().getRoutingConfiguration().getAutoconfigureV4Routes());

        b.add(Contents.class).file(DHCP_V4_CONF).content(ctx.tpl(DHCP_V4_CONF)).perms(HostConfigJobBuilder.WORLD_READABLE_CONFIG)
                .runIf(changedDhcpSettings);

        b.add(Contents.class).file(DHCP_V6_CONF).content(ctx.tpl(DHCP_V6_CONF)).perms(HostConfigJobBuilder.WORLD_READABLE_CONFIG)
                .runIf(changedDhcpSettings);

        if ( changedDhcpSettings ) {
            // all interfaces need to be reloaded so that their dhcp settings get reloaded
            for ( String intf : interfaceAliasMap.keySet() ) {
                if ( !modifiedAliases.containsKey(intf) ) {
                    modifiedAliases.put(intf, IF_DHCP_SETTINGS_CHANGED);
                }
                else {
                    modifiedAliases.put(intf, modifiedAliases.get(intf) | IF_DHCP_SETTINGS_CHANGED);
                }
            }
        }

        return changedDhcpSettings;
    }


    /**
     * @param b
     * @param ctx
     * @param modifiedAliases
     * @throws MatcherException
     * @throws IOException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    protected static void configureSysctl ( JobBuilder b, ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx,
            Map<String, Integer> modifiedAliases ) throws MatcherException, InvalidParameterException, UnitInitializationFailedException, IOException,
                    SystemInformationException, JobBuilderException {

        boolean changedSysctlNetDevSettings = ctx.changed(ctx.match().getNetworkConfiguration().getIpv6Enabled())
                || ctx.changed(ctx.match().getNetworkConfiguration().getRoutingConfiguration().getAutoconfigureV6Routes())
                || interfaceChanged(modifiedAliases, CHANGE_ADD | CHANGE_REMOVE | IF_CONFTYPE_V6_CHANGED);

        b.add(Contents.class).file(ETC_SYSCTL_NET_CONF).content(ctx.tpl(ETC_SYSCTL_NET_CONF, makeSysctlExtraContext(ctx)))
                .perms(HostConfigJobBuilder.WORLD_READABLE_CONFIG).runIf(changedSysctlNetDevSettings);

        b.add(Exec.class).cmd("/sbin/sysctl") //$NON-NLS-1$
                .args("-p", ETC_SYSCTL_NET_CONF).stdout(new DebugOutputHandler(log)).runIf(changedSysctlNetDevSettings); //$NON-NLS-1$
    }


    /**
     * @param modifiedAliases
     * @param changeMask
     * @return
     */
    private static boolean interfaceChanged ( Map<String, Integer> modifiedAliases, int changeMask ) {
        for ( Entry<String, Integer> e : modifiedAliases.entrySet() ) {
            if ( ( e.getValue() & changeMask ) != 0 ) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param ctx
     * @return
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    private static Map<String, Serializable> makeSysctlExtraContext ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx )
            throws SystemInformationException, JobBuilderException {
        Map<String, Serializable> res = new HashMap<>();
        res.put("ifAliases", (Serializable) makeInterfaceAliasMap(ctx)); //$NON-NLS-1$
        return res;
    }


    /**
     * @param ctx
     * @return
     * @throws JobBuilderException
     * @throws SystemInformationException
     */
    private static Map<String, Serializable> makeInterfaceConfigExtraContext ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx )
            throws SystemInformationException, JobBuilderException {
        Map<String, Serializable> res = new HashMap<>();
        res.put("ifAliases", (Serializable) makeInterfaceAliasMap(ctx)); //$NON-NLS-1$

        res.put(
            "dhcpExtraOptions", //$NON-NLS-1$
            ctx.cfg().getResolverConfiguration().getAutoconfigureDns() ? StringUtils.EMPTY : "-e DISABLE_RESOLVCONF=true"); //$NON-NLS-1$
        return res;
    }


    /**
     * @param ctx
     * @return a map of interface alias to real interface name
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    private static Map<String, String> makeInterfaceAliasMap ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx )
            throws SystemInformationException, JobBuilderException {
        return NetworkConfigUtil.makeInterfaceAliasMap(ctx, ctx.cfg());
    }


    /**
     * @return
     * @throws JobBuilderException
     */
    private static Map<String, Integer> getInterfacesOrInterfaceRoutesChanges (
            ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) throws JobBuilderException {
        Map<String, Integer> changes = new HashMap<>();
        Set<InterfaceEntry> newEntries = ctx.cfg().getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
        Map<String, InterfaceEntry> newEntryMap = makeInterfaceMap(newEntries);

        if ( !ctx.cur().isPresent() ) {
            for ( String alias : newEntryMap.keySet() ) {
                changes.put(alias, CHANGE_ADD);
            }
            return changes;
        }

        Set<InterfaceEntry> oldEntries = ctx.cur().get().getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
        Map<String, InterfaceEntry> oldEntryMap = makeInterfaceMap(oldEntries);

        addNew(changes, newEntryMap, oldEntryMap);
        addRemoved(changes, newEntryMap, oldEntryMap);
        addModified(ctx, changes, newEntryMap, oldEntryMap);
        return changes;
    }


    /**
     * @param ctx
     * @param newEntryMap
     * @param oldEntryMap
     * @throws JobBuilderException
     */
    private static void addModified ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, Map<String, Integer> changes,
            Map<String, InterfaceEntry> newEntryMap, Map<String, InterfaceEntry> oldEntryMap ) throws JobBuilderException {
        MultiValuedMap<String, StaticRouteEntry> newRouteEntries = new HashSetValuedHashMap<>();
        makeRouteMap(newRouteEntries, ctx.cfg().getNetworkConfiguration().getRoutingConfiguration());
        MultiValuedMap<String, StaticRouteEntry> oldRouteEntries = new HashSetValuedHashMap<>();
        if ( ctx.cur().isPresent() ) {
            makeRouteMap(oldRouteEntries, ctx.cur().get().getNetworkConfiguration().getRoutingConfiguration());
        }
        Set<String> commonAliases = new HashSet<>(newEntryMap.keySet());
        commonAliases.retainAll(oldEntryMap.keySet());

        for ( String alias : commonAliases ) {
            int changed = getInterfaceOrInterfaceRoutesChange(
                ctx,
                oldEntryMap.get(alias),
                newEntryMap.get(alias),
                oldRouteEntries.get(alias),
                newRouteEntries.get(alias));
            if ( changed != 0 ) {
                changes.put(alias, changed);
            }
        }
    }


    /**
     * @param newRouteEntries
     * @param routingConfiguration
     */
    private static void makeRouteMap ( MultiValuedMap<String, StaticRouteEntry> newRouteEntries, RoutingConfiguration routingConfiguration ) {
        if ( !routingConfiguration.getAutoconfigureV4Routes() && routingConfiguration.getDefaultRouteV4() != null
                && routingConfiguration.getDefaultRouteV4().getGateway() != null ) {
            newRouteEntries.put(routingConfiguration.getDefaultRouteV4().getDevice(), routingConfiguration.getDefaultRouteV4());
        }
        if ( !routingConfiguration.getAutoconfigureV6Routes() && routingConfiguration.getDefaultRouteV6() != null
                && routingConfiguration.getDefaultRouteV6().getGateway() != null ) {
            newRouteEntries.put(routingConfiguration.getDefaultRouteV6().getDevice(), routingConfiguration.getDefaultRouteV6());
        }
        for ( StaticRouteEntry e : routingConfiguration.getStaticRoutes() ) {
            newRouteEntries.put(e.getDevice(), e);
        }
    }


    /**
     * @param ctx
     * @param alias
     * @param interfaceEntry
     * @param interfaceEntry2
     * @param collection2
     * @param collection
     * @return
     * @throws JobBuilderException
     */
    private static int getInterfaceOrInterfaceRoutesChange ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx,
            InterfaceEntry oldIf, InterfaceEntry newIf, Collection<StaticRouteEntry> oldRoutes, Collection<StaticRouteEntry> newRoutes )
                    throws JobBuilderException {
        return getInterfaceChange(ctx, oldIf, newIf) | getRouteChanges(ctx, oldRoutes, newRoutes);
    }


    /**
     * @param ctx
     * @param alias
     * @param oldRoutes
     * @param newRoutes
     * @return
     * @throws JobBuilderException
     */
    private static int getRouteChanges ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, Collection<StaticRouteEntry> oldRoutes,
            Collection<StaticRouteEntry> newRoutes ) throws JobBuilderException {
        int changed = 0;
        Map<RouteIdentifier, StaticRouteEntry> oldRouteMap = makeRouteMap(oldRoutes);
        Map<RouteIdentifier, StaticRouteEntry> newRouteMap = makeRouteMap(newRoutes);
        Set<RouteIdentifier> addedRoutes = new HashSet<>(newRouteMap.keySet());
        addedRoutes.removeAll(oldRouteMap.keySet());
        if ( !addedRoutes.isEmpty() ) {
            changed |= ROUTE_ADDED;
        }

        Set<RouteIdentifier> removedRoutes = new HashSet<>(oldRouteMap.keySet());
        removedRoutes.removeAll(newRouteMap.keySet());
        if ( !removedRoutes.isEmpty() ) {
            changed |= ROUTE_REMOVED;
        }

        Set<RouteIdentifier> commonRoutes = new HashSet<>(newRouteMap.keySet());
        commonRoutes.retainAll(oldRouteMap.keySet());
        for ( RouteIdentifier rt : commonRoutes ) {
            int rtChanged = getRouteChange(ctx, oldRouteMap.get(rt), newRouteMap.get(rt));
            if ( rtChanged != 0 ) {
                changed |= rtChanged;
            }
        }

        return changed;
    }


    /**
     * @param ctx
     * @param alias
     * @param staticRouteEntry
     * @param staticRouteEntry2
     * @return
     */
    private static int getRouteChange ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, StaticRouteEntry oldRt,
            StaticRouteEntry newRt ) {

        if ( !Objects.equals(oldRt.getGateway(), newRt.getGateway()) ) {
            return ROUTE_MODIFIED;
        }

        if ( !Objects.equals(oldRt.getAdvmss(), newRt.getAdvmss()) || !Objects.equals(oldRt.getMtu(), newRt.getMtu()) ) {
            return ROUTE_MODIFIED;
        }

        if ( !Objects.equals(oldRt.getSourceAddress(), newRt.getSourceAddress()) ) {
            return ROUTE_MODIFIED;
        }

        return 0;
    }


    /**
     * @param oldRoutes
     * @return
     * @throws JobBuilderException
     */
    private static Map<RouteIdentifier, StaticRouteEntry> makeRouteMap ( Collection<StaticRouteEntry> routes ) throws JobBuilderException {
        if ( routes == null ) {
            return Collections.EMPTY_MAP;
        }
        Map<RouteIdentifier, StaticRouteEntry> res = new HashMap<>();
        for ( StaticRouteEntry e : routes ) {
            RouteIdentifier ri = new RouteIdentifier(e);
            if ( res.put(ri, e) != null ) {
                throw new JobBuilderException("Duplicate route entry " + ri); //$NON-NLS-1$
            }
        }
        return res;
    }


    /**
     * @param ctx
     * @param alias
     * @param oldIf
     * @param newIf
     * @return
     */
    private static int getInterfaceChange ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, InterfaceEntry oldIf,
            InterfaceEntry newIf ) {
        int changes = 0;
        if ( !Objects.equals(oldIf.getInterfaceIndex(), newIf.getInterfaceIndex())
                || !Objects.equals(oldIf.getHardwareAddress(), newIf.getHardwareAddress()) ) {
            changes |= IF_MATCHER_CHANGED;
        }
        if ( !Objects.equals(oldIf.getMediaType(), newIf.getMediaType()) || !Objects.equals(oldIf.getMtu(), newIf.getMtu())
                || !Objects.equals(oldIf.getOverrideHardwareAddress(), newIf.getOverrideHardwareAddress()) ) {
            changes |= IF_SETTINGS_CHANGED;
        }
        if ( !Objects.equals(oldIf.getV4AddressConfigurationType(), newIf.getV4AddressConfigurationType()) ) {
            changes |= IF_CONFTYPE_V4_CHANGED;
        }
        if ( !Objects.equals(oldIf.getV6AddressConfigurationType(), newIf.getV6AddressConfigurationType()) ) {
            changes |= IF_CONFTYPE_V6_CHANGED;
        }
        if ( !Objects.equals(oldIf.getStaticAddresses(), newIf.getStaticAddresses()) ) {
            changes |= IF_STATICADDR_CHANGED;
        }
        return changes;
    }


    /**
     * @param changes
     * @param newEntryMap
     * @param oldEntryMap
     */
    private static void addNew ( Map<String, Integer> changes, Map<String, InterfaceEntry> newEntryMap, Map<String, InterfaceEntry> oldEntryMap ) {
        Set<String> newAliases = new HashSet<>(newEntryMap.keySet());
        newAliases.removeAll(oldEntryMap.keySet());

        for ( String alias : newAliases ) {
            changes.put(alias, CHANGE_ADD);
        }
    }


    /**
     * @param changes
     * @param newEntryMap
     * @param oldEntryMap
     */
    private static void addRemoved ( Map<String, Integer> changes, Map<String, InterfaceEntry> newEntryMap,
            Map<String, InterfaceEntry> oldEntryMap ) {
        Set<String> removedAliases = new HashSet<>(oldEntryMap.keySet());
        removedAliases.removeAll(newEntryMap.keySet());

        for ( String alias : removedAliases ) {
            changes.put(alias, CHANGE_REMOVE);
        }
    }


    /**
     * @param oldEntries
     * @return
     */
    private static Map<String, InterfaceEntry> makeInterfaceMap ( Set<InterfaceEntry> entries ) {
        Map<String, InterfaceEntry> entryMap = new HashMap<>();
        for ( InterfaceEntry e : entries ) {
            if ( entryMap.put(e.getAlias(), e) != null ) {
                throw new IllegalArgumentException("Duplicate definition for interface alias " + e.getAlias()); //$NON-NLS-1$
            }
        }
        return entryMap;
    }


    /**
     * @param ctx
     * @param res
     */
    public static void addNetworkSysContext ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx, Map<String, Serializable> res ) {
        // unused right now
    }


    /**
     * @param ctx
     * @return whether the interface
     * @throws MatcherException
     */
    public static boolean haveSysctlSettingsChanged ( ConfigurationJobContext<HostConfiguration, HostConfigurationJob> ctx ) throws MatcherException {
        if ( ctx.changed(ctx.match().getNetworkConfiguration().getRoutingConfiguration().getAutoconfigureV6Routes()) ) {
            return true;
        }

        return true;
    }

}

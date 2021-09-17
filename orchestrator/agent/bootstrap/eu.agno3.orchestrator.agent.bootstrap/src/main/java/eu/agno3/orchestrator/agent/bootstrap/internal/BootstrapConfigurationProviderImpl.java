/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigException;
import eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider;
import eu.agno3.orchestrator.config.auth.PasswordPolicyConfigMutable;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfigMutable;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigMutable;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigMutable;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.HostIdentificationMutable;
import eu.agno3.orchestrator.config.hostconfig.HostIdentificationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV4;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV6;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.RouteType;
import eu.agno3.orchestrator.config.hostconfig.network.RoutingConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.RoutingConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryImpl;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntryImpl;
import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntryMutable;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorAdvancedConfigurationMutable;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorAuthenticationConfigurationMutable;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationMutable;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorEventLogConfigurationMutable;
import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.MACAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component (
    service = BootstrapConfigurationProvider.class,
    configurationPid = BootstrapConfigurationProviderImpl.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class BootstrapConfigurationProviderImpl implements BootstrapConfigurationProvider {

    /**
     * 
     */
    private static final String DATA_DISK_ALIAS = "data"; //$NON-NLS-1$

    /**
     * Configuration PID
     */
    public static final String PID = "bootstrap"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BootstrapConfigurationProviderImpl.class);

    private static final String DEFAULT_SERVER_CONFIG_DIR = "/etc/server"; //$NON-NLS-1$
    private static final String BOOTSTRAP_DOMAIN = "boot"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String IP = "IP"; //$NON-NLS-1$
    private static final String NETDEV = "NETDEV"; //$NON-NLS-1$
    private static final String DEFGW = "DEFGW"; //$NON-NLS-1$
    private static final String DNS = "DNS"; //$NON-NLS-1$
    private static final String SSHENABLE = "SSHENABLE"; //$NON-NLS-1$
    private static final String AGENT_ID = "AGENT_ID"; //$NON-NLS-1$
    private static final String LOCAL_SERVER = "LOCAL_SERVER"; //$NON-NLS-1$
    private static final String ADMINPASS = "ADMINPASS"; //$NON-NLS-1$
    private static final String HOSTNAME = "HOST_NAME"; //$NON-NLS-1$
    private static final String DEVELOPER = "DEVELOPER"; //$NON-NLS-1$
    private static final String TIMEZONE = "TIMEZONE"; //$NON-NLS-1$
    private static final String LOCAL_SERVER_CONF_DIR = "LOCAL_SERVER_CONF_DIR"; //$NON-NLS-1$
    private static final String IMAGE_TYPE = "IMAGE_TYPE"; //$NON-NLS-1$
    private static final String DHCPV6 = "dhcpv6"; //$NON-NLS-1$
    private static final String DHCP = "dhcp"; //$NON-NLS-1$
    private static final String DEPLOY_SIZE = "DEPLOYSIZE"; //$NON-NLS-1$
    private static final String DATA_DISK_LABEL = "DATA_DISK_LABEL"; //$NON-NLS-1$
    private static final String DATA_DISK_ID = "DATA_DISK_ID"; //$NON-NLS-1$

    private static final String DEBUG_PKGS = "DEBUG_PKGS"; //$NON-NLS-1$
    private static final String TRACE_PKGS = "TRACE_PKGS"; //$NON-NLS-1$

    private static final String AUTORUN = "AUTORUN"; //$NON-NLS-1$

    private static final String[] REQUIRED_PARAMS = new String[] {
        IP, NETDEV, AGENT_ID
    };

    private HostConfiguration bootstrapHostConfig;
    private boolean localServer;
    private String adminPass;
    private String overrideHostName;
    private boolean developerMode;

    private ValidatorFactory validator;

    private File localServerConfigDirectory;

    private String imageType;

    private String deploySize;

    private OrchestratorConfiguration bootstrapServerConfig;

    private boolean autoRun;


    @Reference
    protected synchronized void setValidatorFactory ( ValidatorFactory vf ) {
        this.validator = vf;
    }


    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        if ( this.validator == vf ) {
            this.validator = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> properties = ctx.getProperties();
        log.info("Processing bootstrap configuration"); //$NON-NLS-1$
        parseOtherConfig(properties);
        this.bootstrapHostConfig = parseBootstrapHostConfig(properties);
        this.bootstrapServerConfig = parseBootstrapServerConfig(properties, this.bootstrapHostConfig);
    }


    /**
     * @param properties
     */
    private void parseOtherConfig ( Dictionary<String, Object> properties ) {
        String localServerSpec = (String) properties.get(LOCAL_SERVER);
        if ( StringUtils.isNotBlank(localServerSpec) && localServerSpec.trim().equals(Boolean.TRUE.toString()) ) {
            this.localServer = true;
        }
        else {
            this.localServer = false;
        }

        String adminPassSpec = (String) properties.get(ADMINPASS);
        if ( StringUtils.isNotBlank(adminPassSpec) ) {
            this.adminPass = adminPassSpec.trim();
        }
        else {
            this.adminPass = null;
        }

        String hostNameSpec = (String) properties.get(HOSTNAME);
        if ( !StringUtils.isBlank(hostNameSpec) ) {
            this.overrideHostName = hostNameSpec.trim();
        }

        String deploySizeSpec = (String) properties.get(DEPLOY_SIZE);
        if ( !StringUtils.isBlank(deploySizeSpec) ) {
            this.deploySize = deploySizeSpec;
        }

        String autoRunSpec = (String) properties.get(AUTORUN);
        if ( !StringUtils.isBlank(autoRunSpec) ) {
            this.autoRun = Boolean.parseBoolean(autoRunSpec);
        }

        parseDeveloperConfig(properties);
    }


    /**
     * @param properties
     */
    private void parseDeveloperConfig ( Dictionary<String, Object> properties ) {
        String developerSpec = (String) properties.get(DEVELOPER);
        if ( !StringUtils.isBlank(developerSpec) ) {
            this.developerMode = true;
        }

        if ( this.developerMode ) {
            String imageTypeSpec = (String) properties.get(IMAGE_TYPE);
            if ( !StringUtils.isBlank(imageTypeSpec) ) {
                this.imageType = imageTypeSpec;
            }
        }

        String localServerConfDirSpec = (String) properties.get(LOCAL_SERVER_CONF_DIR);
        if ( !StringUtils.isBlank(localServerConfDirSpec) ) {
            this.localServerConfigDirectory = new File(localServerConfDirSpec.trim());
        }
    }


    /**
     * @param properties
     * @return
     */
    @Nullable
    protected HostConfiguration parseBootstrapHostConfig ( Dictionary<String, Object> properties ) {
        try {
            this.validateProperties(properties);
        }
        catch ( BootstrapConfigException e ) {
            log.error("Bootstrap configuration invalid", e); //$NON-NLS-1$
            return null;
        }

        HostConfiguration hc;
        try {
            hc = this.parseHostConfiguration(properties);
        }
        catch ( BootstrapConfigException e ) {
            log.error("Failed to process bootstrap host configuration", e); //$NON-NLS-1$
            return null;
        }

        Set<ConstraintViolation<HostConfiguration>> violations = this.validator.getValidator().validate(hc);

        if ( !violations.isEmpty() ) {
            log.error("Boostrap host configuration validation failed:"); //$NON-NLS-1$
            for ( ConstraintViolation<HostConfiguration> violation : violations ) {
                log.error(violation.toString());
            }
            return null;
        }
        return hc;
    }


    /**
     * @param properties
     * @throws BootstrapConfigException
     */
    protected void validateProperties ( Dictionary<String, Object> properties ) throws BootstrapConfigException {
        for ( String param : REQUIRED_PARAMS ) {
            if ( properties.get(param) == null ) {
                throw new BootstrapConfigException(String.format("Parameter %s must not be empty", param)); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider#getBootstrapHostConfig()
     */
    @Override
    public @NonNull HostConfiguration getBootstrapHostConfig () {
        HostConfiguration hc = this.bootstrapHostConfig;
        if ( hc == null ) {
            throw new IllegalStateException();
        }

        return hc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider#getServerConfiguration()
     */
    @Override
    public @NonNull OrchestratorConfiguration getServerConfiguration () {
        OrchestratorConfiguration sc = this.bootstrapServerConfig;
        if ( sc == null ) {
            throw new IllegalStateException();
        }
        return sc;
    }


    /**
     * @return the developerMode
     */
    @Override
    public boolean isDeveloperMode () {
        return this.developerMode;
    }


    /**
     * @return the localServer
     */
    @Override
    public boolean isLocalServer () {
        return this.localServer;
    }


    /**
     * @return the overrideHostName
     */
    @Override
    public String getOverrideHostName () {
        return this.overrideHostName;
    }


    /**
     * @return the deploySize
     */
    public String getDeploySize () {
        return this.deploySize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider#getAdminPassword()
     */
    @Override
    public String getAdminPassword () {
        return this.adminPass;
    }


    /**
     * @return the autoRun
     */
    @Override
    public boolean isAutoRun () {
        return this.autoRun;
    }


    /**
     * @param properties
     * @param hc
     * @return parsed server configuration
     */
    protected OrchestratorConfiguration parseBootstrapServerConfig ( Dictionary<String, Object> properties, HostConfiguration hc ) {
        String dataStorage = findDataStorage(hc);
        OrchestratorConfigurationMutable oc = OrchestratorConfigurationObjectTypeDescriptor.emptyInstance();
        OrchestratorAuthenticationConfigurationMutable authConfig = (OrchestratorAuthenticationConfigurationMutable) oc.getAuthenticationConfig();
        OrchestratorAdvancedConfigurationMutable advancedConfig = (OrchestratorAdvancedConfigurationMutable) oc.getAdvancedConfig();
        OrchestratorEventLogConfigurationMutable eventLogConfig = (OrchestratorEventLogConfigurationMutable) oc.getEventLogConfig();
        advancedConfig.setDataStorage(dataStorage);
        advancedConfig.setTempStorage(dataStorage);
        eventLogConfig.setEventStorage(dataStorage);

        setupDebugging((RuntimeConfigurationMutable) advancedConfig.getRuntimeConfig(), properties);

        PasswordPolicyConfigMutable passwordPolicy = (PasswordPolicyConfigMutable) authConfig.getAuthenticatorsConfig().getPasswordPolicy();
        passwordPolicy.setEntropyLowerLimit(0);
        return oc;
    }


    /**
     * @param hc
     * @return
     */
    private static String findDataStorage ( HostConfiguration hc ) {
        String dataStorage = null;
        Set<MountEntry> mountEntries = hc.getStorageConfiguration().getMountEntries();
        if ( mountEntries != null && mountEntries.size() > 0 ) {
            MountEntry next = mountEntries.iterator().next();
            if ( next instanceof LocalMountEntry ) {
                dataStorage = next.getAlias();
            }
        }
        return dataStorage;
    }


    /**
     * @param props
     * @return
     * @throws BootstrapConfigException
     */
    private HostConfiguration parseHostConfiguration ( Dictionary<String, Object> props ) throws BootstrapConfigException {
        HostConfigurationImpl hc = HostConfigurationObjectTypeDescriptor.emptyInstance();
        hc.setHostIdentification(this.parseHostIdentification(props));
        hc.setDateTimeConfiguration(this.parseDateTimeConfiguration(props));
        hc.setResolverConfiguration(this.parseResolverConfiguration(props));
        hc.setSystemConfiguration(this.parseSystemConfiguration(props));
        hc.setStorageConfiguration(this.parseStorageConfiguration(props));
        hc.setNetworkConfiguration(this.parseNetworkConfiguration(props));
        hc.setTrustConfiguration(this.parseTrustConfiguration(props));
        hc.setKeystoreConfiguration(this.parseKeystoreConfiguration(props));
        return hc;
    }


    /**
     * @param props
     * @return
     */
    protected KeystoresConfigMutable parseKeystoreConfiguration ( Dictionary<String, Object> props ) {
        KeystoresConfigMutable ksConfig = KeystoresConfigObjectTypeDescriptor.emptyInstance();
        KeystoreConfigMutable webConfig = KeystoreConfigObjectTypeDescriptor.emptyInstance();
        webConfig.setAlias("web"); //$NON-NLS-1$
        ksConfig.setKeystores(new HashSet<>(Arrays.asList((KeystoreConfig) webConfig)));
        return ksConfig;
    }


    /**
     * @param props
     * @return
     */
    protected TruststoresConfigMutable parseTrustConfiguration ( Dictionary<String, Object> props ) {
        return TruststoresConfigObjectTypeDescriptor.emptyInstance();
    }


    /**
     * @param props
     * @return
     * @throws BootstrapConfigException
     */
    protected NetworkConfigurationMutable parseNetworkConfiguration ( Dictionary<String, Object> props ) throws BootstrapConfigException {
        NetworkConfigurationMutable nc = NetworkConfigurationObjectTypeDescriptor.emptyInstance();
        String ipSpec = ( (String) props.get(IP) ).trim();
        boolean v6 = checkIpAddress(ipSpec);
        nc.setIpv6Enabled(v6);
        nc.setInterfaceConfiguration(this.parseInterfaceConfiguration(props, v6));
        nc.setRoutingConfiguration(this.parseRoutingConfiguration(props, v6));
        return nc;
    }


    /**
     * @param ipSpec
     * @return
     * @throws BootstrapConfigException
     */
    private static boolean checkIpAddress ( String ipSpec ) throws BootstrapConfigException {
        boolean v6 = false;

        if ( ipSpec.equalsIgnoreCase(DHCP) ) {
            v6 = false;
        }
        else if ( ipSpec.equalsIgnoreCase(DHCPV6) ) {
            v6 = true;
        }
        else {
            NetworkSpecification netSpec = NetworkSpecification.fromString(ipSpec, false);

            if ( netSpec.getAddress() instanceof IPv6Address ) {
                v6 = true;
            }
            else if ( netSpec.getAddress() instanceof IPv4Address ) {
                v6 = false;
            }
            else {
                throw new BootstrapConfigException("IP is not a valid address"); //$NON-NLS-1$
            }
        }
        return v6;
    }


    /**
     * @param props
     * @param v6
     * @return
     * @throws BootstrapConfigException
     */
    protected InterfaceConfigurationMutable parseInterfaceConfiguration ( Dictionary<String, Object> props, boolean v6 )
            throws BootstrapConfigException {
        InterfaceConfigurationMutable ifc = InterfaceConfigurationObjectTypeDescriptor.emptyInstance();
        InterfaceEntryMutable ife = InterfaceEntryObjectTypeDescriptor.emptyInstance();

        String netDevSpec = (String) props.get(NETDEV);
        netDevSpec = netDevSpec.trim();
        ife.setHardwareAddress(obtainHwAddr(netDevSpec));
        ife.setAlias(netDevSpec);
        setupAddressConfig(props, v6, ife);
        ifc.getInterfaces().add(ife);
        return ifc;
    }


    /**
     * @param props
     * @param v6
     * @param ife
     */
    private static void setupAddressConfig ( Dictionary<String, Object> props, boolean v6, InterfaceEntryMutable ife ) {
        String ipSpec = ( (String) props.get(IP) ).trim();

        AddressConfigurationTypeV4 v4Type = AddressConfigurationTypeV4.NONE;
        AddressConfigurationTypeV6 v6Type = AddressConfigurationTypeV6.NONE;
        Set<NetworkSpecification> addrs = new HashSet<>();

        if ( ipSpec.equalsIgnoreCase(DHCP) ) {
            v4Type = AddressConfigurationTypeV4.DHCP;
        }
        else if ( ipSpec.equalsIgnoreCase(DHCPV6) ) {
            v6Type = AddressConfigurationTypeV6.DHCP;
        }
        else if ( v6 ) {
            v6Type = AddressConfigurationTypeV6.STATIC;
            addrs.add(NetworkSpecification.fromString(ipSpec, false));
        }
        else {
            v4Type = AddressConfigurationTypeV4.STATIC;
            addrs.add(NetworkSpecification.fromString(ipSpec, false));

        }

        ife.setV4AddressConfigurationType(v4Type);
        ife.setV6AddressConfigurationType(v6Type);
        ife.setStaticAddresses(addrs);
    }


    /**
     * @param netDevSpec
     * @return
     * @throws BootstrapConfigException
     */
    private static MACAddress obtainHwAddr ( String netDevSpec ) throws BootstrapConfigException {
        try {
            NetworkInterface netDev = NetworkInterface.getByName(netDevSpec);
            if ( netDev == null ) {
                throw new BootstrapConfigException("Failed to locate network interface " + netDevSpec); //$NON-NLS-1$
            }
            MACAddress hwaddr = new MACAddress();
            hwaddr.fromByteArray(netDev.getHardwareAddress());
            return hwaddr;
        }
        catch ( SocketException e ) {
            throw new BootstrapConfigException("Failed to enumerate network interfaces", e); //$NON-NLS-1$
        }
    }


    /**
     * @param props
     * @param v6
     * @return
     */
    protected RoutingConfigurationMutable parseRoutingConfiguration ( Dictionary<String, Object> props, boolean v6 ) {
        RoutingConfigurationMutable rtc = RoutingConfigurationObjectTypeDescriptor.emptyInstance();
        String defGwSpec = (String) props.get(DEFGW);
        StaticRouteEntryImpl nullRoute = new StaticRouteEntryImpl();
        nullRoute.setRouteType(RouteType.UNICAST);

        if ( StringUtils.isNotBlank(defGwSpec) ) {
            setupStaticDefaultRoute(props, v6, rtc, defGwSpec);
        }
        else if ( v6 ) {
            rtc.setAutoconfigureV4Routes(false);
            rtc.setAutoconfigureV6Routes(true);
        }
        else {
            rtc.setAutoconfigureV4Routes(true);
            rtc.setAutoconfigureV6Routes(false);
        }
        return rtc;
    }


    /**
     * @param props
     * @param v6
     * @param rtc
     * @param defGwSpec
     */
    private static void setupStaticDefaultRoute ( Dictionary<String, Object> props, boolean v6, RoutingConfigurationMutable rtc, String defGwSpec ) {
        rtc.setAutoconfigureV4Routes(false);
        rtc.setAutoconfigureV6Routes(false);
        StaticRouteEntryMutable sre = StaticRouteEntryObjectTypeDescriptor.emptyInstance();
        sre.setDevice( ( (String) props.get(NETDEV) ).trim());
        sre.setGateway(AbstractIPAddress.parse(defGwSpec));

        if ( v6 ) {
            rtc.setDefaultRouteV6(sre);
        }
        else {
            rtc.setDefaultRouteV4(sre);
        }
    }


    /**
     * @param props
     * @return
     */
    protected StorageConfigurationMutable parseStorageConfiguration ( Dictionary<String, Object> props ) {
        StorageConfigurationMutable sc = new StorageConfigurationImpl();

        String dataDiskId = (String) props.get(DATA_DISK_ID);
        String dataDiskLabel = (String) props.get(DATA_DISK_LABEL);

        if ( !StringUtils.isBlank(dataDiskId) || !StringUtils.isBlank(dataDiskLabel) ) {
            addDataDisk(sc, dataDiskId, dataDiskLabel);
            sc.setBackupStorage(DATA_DISK_ALIAS);
        }

        return sc;
    }


    /**
     * @param sc
     * @param dataDiskId
     * @param dataDiskLabel
     */
    private static void addDataDisk ( StorageConfigurationMutable sc, String dataDiskId, String dataDiskLabel ) {
        LocalMountEntryMutable dataMe = new LocalMountEntryImpl();
        dataMe.setAlias(DATA_DISK_ALIAS); // $NON-NLS-1$
        if ( !StringUtils.isBlank(dataDiskId) ) {
            dataMe.setMatchUuid(UUID.fromString(dataDiskId));
        }
        else if ( !StringUtils.isBlank(dataDiskLabel) ) {
            dataMe.setMatchLabel(dataDiskLabel);
        }
        sc.getMountEntries().add(dataMe);
    }


    /**
     * @param props
     * @return
     */
    protected SystemConfigurationMutable parseSystemConfiguration ( Dictionary<String, Object> props ) {
        SystemConfigurationMutable sysc = SystemConfigurationObjectTypeDescriptor.emptyInstance();

        String sshEnableSpec = (String) props.get(SSHENABLE);

        if ( StringUtils.isNotBlank(sshEnableSpec) && Boolean.TRUE.toString().equalsIgnoreCase(sshEnableSpec.trim()) ) {
            sysc.setEnableSshAccess(true);
        }
        else {
            sysc.setEnableSshAccess(false);
        }

        RuntimeConfiguration agentConfig = sysc.getAgentConfig();
        setupDebugging((RuntimeConfigurationMutable) agentConfig, props);
        return sysc;
    }


    /**
     * @param agentConfig
     * @param props
     */
    private static void setupDebugging ( RuntimeConfigurationMutable agentConfig, Dictionary<String, Object> props ) {
        String dbg = (String) props.get(DEBUG_PKGS);
        if ( !StringUtils.isBlank(dbg) ) {
            for ( String e : StringUtils.split(dbg, ',') ) {
                agentConfig.getDebugPackages().add(e.trim());
            }
        }

        String trc = (String) props.get(TRACE_PKGS);
        if ( !StringUtils.isBlank(trc) ) {
            for ( String e : StringUtils.split(trc, ',') ) {
                agentConfig.getTracePackages().add(e.trim());
            }
        }
    }


    /**
     * @param props
     * @return
     */
    protected ResolverConfigurationMutable parseResolverConfiguration ( Dictionary<String, Object> props ) {
        ResolverConfigurationImpl resc = ResolverConfigurationObjectTypeDescriptor.emptyInstance();

        String dnsSpec = (String) props.get(DNS);

        if ( StringUtils.isNotBlank(dnsSpec) ) {
            resc.getNameservers().add(AbstractIPAddress.parse(dnsSpec.trim()));
        }
        else {
            resc.setAutoconfigureDns(true);
        }

        return resc;
    }


    /**
     * @param props
     * @return
     */
    protected DateTimeConfigurationMutable parseDateTimeConfiguration ( Dictionary<String, Object> props ) {
        DateTimeConfigurationMutable dtConfig = DateTimeConfigurationObjectTypeDescriptor.emptyInstance();
        String timezoneSpec = (String) props.get(TIMEZONE);
        if ( !StringUtils.isBlank(timezoneSpec) ) {
            try {
                dtConfig.setTimezone(DateTimeZone.forID(timezoneSpec));
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Invalid timezone " + timezoneSpec, e); //$NON-NLS-1$
            }
        }
        return dtConfig;
    }


    /**
     * @param props
     * @return
     * @throws BootstrapConfigException
     */
    protected HostIdentificationMutable parseHostIdentification ( Dictionary<String, Object> props ) throws BootstrapConfigException {
        HostIdentificationMutable hic = HostIdentificationObjectTypeDescriptor.emptyInstance();
        if ( this.overrideHostName == null ) {
            String guessedHostName = LocalHostUtil.guessPrimaryHostName();
            int firstDot = guessedHostName.indexOf('.');
            if ( AbstractIPAddress.isIPAddress(guessedHostName) || firstDot < 0 ) {
                String agentId = (String) props.get(AGENT_ID);
                String initialHostname = buildInitialHostname(agentId.trim());
                hic.setHostName(initialHostname);
                hic.setDomainName(BOOTSTRAP_DOMAIN);
            }
            else {

                String hostPart = guessedHostName.substring(0, firstDot);
                String domainPart = guessedHostName.substring(firstDot + 1);
                hic.setHostName(hostPart);
                hic.setDomainName(domainPart);
            }
            log.info(String.format("Initial hostname is %s.%s", hic.getHostName(), hic.getDomainName())); //$NON-NLS-1$
        }
        else {
            int firstSep = this.overrideHostName.indexOf('.');
            if ( firstSep <= 0 ) {
                throw new BootstrapConfigException("Hostname is not a valid FQDN"); //$NON-NLS-1$
            }

            hic.setHostName(this.overrideHostName.substring(0, firstSep));
            hic.setDomainName(this.overrideHostName.substring(firstSep + 1));
        }
        return hic;
    }


    /**
     * @param agentId
     * @return
     */
    String buildInitialHostname ( String agentId ) {
        return "app-" + agentId.substring(agentId.lastIndexOf('-') + 1); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider#getLocalServerConfigDirectory()
     */
    @Override
    public File getLocalServerConfigDirectory () {
        if ( this.localServerConfigDirectory != null ) {
            return this.localServerConfigDirectory;
        }
        return new File(DEFAULT_SERVER_CONFIG_DIR);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.bootstrap.BootstrapConfigurationProvider#getImageType()
     */
    @Override
    public String getImageType () {
        if ( this.imageType != null ) {
            return this.imageType;
        }
        return SystemImageUtil.getLocalImageType();
    }
}

/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.web.SSLEndpointConfiguration;
import eu.agno3.orchestrator.config.web.WebEndpointConfig;
import eu.agno3.orchestrator.config.web.WebReverseProxyConfiguration;
import eu.agno3.orchestrator.config.web.WebReverseProxySSLType;
import eu.agno3.orchestrator.config.web.WebReverseProxyType;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.file.mkdir.MkDir;
import eu.agno3.orchestrator.system.base.units.file.touch.Touch;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
public class WebConfigUtil {

    private static final Logger log = Logger.getLogger(WebConfigUtil.class);


    /**
     * @param ctx
     * @param wec
     * @param defaultBindAddr
     * @param v6
     * @return the bind address to use, null if bind to any
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    public static List<String> findBindAddresses ( RuntimeConfigContext<?, ?> ctx, WebEndpointConfig wec, String defaultBindAddr, boolean v6 )
            throws SystemInformationException, JobBuilderException {

        List<String> bindAddresses = new ArrayList<>();

        if ( wec.getBindAddresses() != null && !wec.getBindAddresses().isEmpty() ) {
            log.debug("Binding to addresses " + wec.getBindAddresses()); //$NON-NLS-1$
            for ( NetworkAddress networkAddress : wec.getBindAddresses() ) {
                bindAddresses.add(networkAddress.toString());
            }
        }

        if ( !bindAddresses.isEmpty() && !StringUtils.isBlank(wec.getBindInterface()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Binding to interface " + wec.getBindInterface()); //$NON-NLS-1$
            }
            bindAddresses = new ArrayList<>();
            HostConfiguration hc = ctx.octx().sctx().getContextService(HostConfiguration.class);
            Map<String, String> ifs = NetworkConfigUtil.makeInterfaceAliasMap(ctx.octx(), hc);

            String ifName = ifs.get(wec.getBindInterface());
            if ( log.isDebugEnabled() ) {
                log.debug("Real interface is " + ifName); //$NON-NLS-1$
            }

            if ( ifName == null ) {
                throw new JobBuilderException("Interface not found for alias " + wec.getBindInterface()); //$NON-NLS-1$
            }

            try {
                NetworkInterface byName = NetworkInterface.getByName(ifName);
                List<InterfaceAddress> interfaceAddresses = byName.getInterfaceAddresses();

                if ( log.isDebugEnabled() ) {
                    log.debug("Interface addresses " + interfaceAddresses); //$NON-NLS-1$
                }

                List<InterfaceAddress> usable = new LinkedList<>();

                for ( InterfaceAddress addr : interfaceAddresses ) {
                    if ( addr.getAddress().isLoopbackAddress() ) {
                        continue;
                    }

                    if ( v6 && addr.getAddress().getAddress().length == 16 ) {
                        usable.add(addr);
                    }
                    if ( addr.getAddress().getAddress().length == 4 ) {
                        usable.add(addr);
                    }
                }

                if ( usable.isEmpty() ) {
                    throw new JobBuilderException(String.format(
                        "Cannot find any usable addresses on interface %s (%s)", //$NON-NLS-1$
                        wec.getBindInterface(),
                        ifName));
                }

                for ( InterfaceAddress u : usable ) {
                    bindAddresses.add(u.getAddress().getHostAddress());
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Using " + bindAddresses); //$NON-NLS-1$
                }

                return bindAddresses;
            }
            catch ( SocketException e ) {
                throw new JobBuilderException("Could not get interface information for " + ifName, e); //$NON-NLS-1$
            }
        }
        else if ( bindAddresses.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Binding to default " + defaultBindAddr); //$NON-NLS-1$
            }
            return Collections.singletonList(defaultBindAddr);
        }
        return bindAddresses;
    }


    /**
     * @param b
     * @param ctx
     * @param wec
     * @param connectorName
     * @param v6
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws JobBuilderException
     * @throws SystemInformationException
     */
    @SuppressWarnings ( "nls" )
    public static void makeWebEndpointConfig ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, WebEndpointConfig wec, String connectorName, boolean v6 )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, SystemInformationException,
            JobBuilderException {

        PropertyConfigBuilder connectorProps = PropertyConfigBuilder.get().p("name", connectorName)
                .p("bind", findBindAddresses(ctx, wec, "0.0.0.0", v6)).p("port", wec.getBindPort());

        if ( wec.getBehindReverseProxy() ) {
            configureReverseProxy(connectorProps, wec.getReverseProxyConfig());
        }

        if ( wec.getBindPort() < 1024 ) {
            String authBindFile = "/etc/authbind/byport/" + wec.getBindPort();
            b.add(Touch.class).file(authBindFile).group(ctx.getServiceManager().getGroupPrincipal())
                    .perms(PosixFilePermissions.fromString("rw---x---"));
        }

        if ( wec.getDisableSSL() ) {
            ctx.factory("httpservice.connector.http", connectorName, connectorProps);
        }
        else {
            String tlsContextName = connectorName + "-ssl";
            connectorProps.p(
                "TLSContext.target",
                FilterBuilder.get().and(FilterBuilder.get().eq("subsystem", "https"), FilterBuilder.get().eq("instanceId", tlsContextName))
                        .toString());
            ctx.factory("httpservice.connector.https", connectorName, connectorProps);
            SSLEndpointConfiguration sec = wec.getSslEndpointConfiguration();
            SSLConfigUtil.setupSSLEndpointMapping(b, ctx, tlsContextName, "https", sec);
        }
    }


    /**
     * @param b
     * @param ctx
     * @param wec
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    public static void makeSecurityHeaderConfig ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, WebEndpointConfig wec )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder headersCfg = PropertyConfigBuilder.get();
        if ( wec.getEnableHSTS() ) {
            headersCfg.p("hstsEnabled", true);
            headersCfg.p("hstsMaxAge", wec.getHstsTimeout());
            headersCfg.p("hstsIncludeSubdomains", wec.getHstsIncludeSubdomains());
            headersCfg.p("hstsPreload", wec.getHstsAcceptPreload());
        }
        else {
            headersCfg.p("hstsEnabled", false);
        }

        ctx.instance("secheaders", headersCfg);
    }


    /**
     * @param b
     * @param ctx
     * @param wec
     * @param overrideSessionStoragePath
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    public static void setupSessionManager ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, WebEndpointConfig wec, Path overrideSessionStoragePath )
            throws UnitInitializationFailedException, ServiceManagementException, InvalidParameterException {
        if ( overrideSessionStoragePath != null ) {
            // create and ensure directory permissions
            b.add(MkDir.class).file(overrideSessionStoragePath).owner(ctx.getServiceManager().getServicePrincipal())
                    .perms(FileSecurityUtils.getOwnerOnlyDirPermissions()).createTargetDir();
        }

        ctx.instance(
            "httpservice.session",
            PropertyConfigBuilder.get()
                    .p("sessionTimeoutSecs", wec.getSessionInactiveTimeout() != null ? wec.getSessionInactiveTimeout().getStandardSeconds() : null)
                    .p("secureCookie", wec.getDisableSSL() != null ? !wec.getDisableSSL() : true)
                    .p("sessionStoreBase", overrideSessionStoragePath != null ? overrideSessionStoragePath.toString() : null));
    }


    /**
     * @param connectorProps
     * @param rpc
     */
    @SuppressWarnings ( "nls" )
    private static void configureReverseProxy ( PropertyConfigBuilder connectorProps, WebReverseProxyConfiguration rpc ) {
        connectorProps.p("behindReverseProxy", true);
        if ( rpc.getProxyType() == WebReverseProxyType.NONE && rpc.getOverrideURI() != null ) {
            connectorProps.p("overrideUri", rpc.getOverrideURI().toASCIIString());
        }

        if ( rpc.getProxyType() != WebReverseProxyType.NONE ) {
            List<String> trusted = new ArrayList<>();
            for ( NetworkSpecification net : rpc.getTrustedProxies() ) {
                trusted.add(net.toString());
            }
            connectorProps.p("proxyTrustedAddrs", trusted);
        }

        if ( rpc.getProxyType() == WebReverseProxyType.RFC7239 ) {
            connectorProps.p("proxyRFC7239", true);
        }

        if ( rpc.getProxyType() == WebReverseProxyType.CUSTOM ) {
            connectorProps.p("proxyForwardedHostHeader", rpc.getForwardedHostHeader());
            if ( !StringUtils.isBlank(rpc.getForwardedPortHeader()) ) {
                connectorProps.p("proxyForwardedPortHeader", rpc.getForwardedPortHeader());
            }
            else {
                connectorProps.p("proxyUsePortFromHostHeader", true);
            }

            connectorProps.p("proxyForwardedRemoteAddrHeader", rpc.getForwardedRemoteAddrHeader());

            connectorProps.p("proxyForwardedSSLHeader", rpc.getForwardedSSLMatchHeader());
            connectorProps.p("proxyForwardedSSLHeaderValue", rpc.getForwardedSSLMatchValue());
        }

        if ( rpc.getForwardedSSLCiphersType() != WebReverseProxySSLType.NONE ) {
            connectorProps.p("proxyForwardedSSLCiphersType", rpc.getForwardedSSLCiphersType().name());
            connectorProps.p("proxyForwardedSSLCiphersHeader", rpc.getForwardedSSLCiphersHeader());
        }
    }


    /**
     * @param b
     * @param ctx
     * @param overrideTempDir
     * @param webappName
     * @param webappBundle
     * @param defaultContextPath
     * @param forceContextPath
     * @param wec
     * @param params
     * @param connectors
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    public static void makeWebappConfig ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, String webappName, String webappBundle,
            String defaultContextPath, boolean forceContextPath, WebEndpointConfig wec, Path overrideTempDir, Map<String, String> params,
            String... connectors ) throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        Set<String> virtualHosts = new HashSet<>();
        for ( String connector : connectors ) {
            virtualHosts.add("@" + connector);
        }

        String realContextPath = defaultContextPath;

        if ( !StringUtils.isBlank(wec.getContextPath()) ) {
            realContextPath = wec.getContextPath();
        }

        // strip trailing slash if not root context
        if ( realContextPath.length() > 1 && realContextPath.endsWith("/") ) {
            realContextPath = realContextPath.substring(0, realContextPath.length() - 1);
        }

        PropertyConfigBuilder props = PropertyConfigBuilder.get().p("bundle", webappBundle).p("virtualHosts", virtualHosts)
                .p("contextPath", realContextPath).p("tempDir", overrideTempDir != null ? overrideTempDir.toString() : null);

        for ( Entry<String, String> param : params.entrySet() ) {
            props.p(param.getKey(), param.getValue());
        }

        ctx.factory("httpservice.webapp", webappName, props);
    }

}

/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network.validation;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigTestParams;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.validation.SocketValidationUtils;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPlugin.class )
public class NetworkConfigurationTestPlugin implements ConfigTestPluginAsync<NetworkConfiguration> {

    private static final Logger log = Logger.getLogger(NetworkConfigurationTestPlugin.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<NetworkConfiguration> getTargetType () {
        return NetworkConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getRunOn()
     */
    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return EnumSet.of(ConfigTestPluginRunOn.AGENT, ConfigTestPluginRunOn.SERVER);
    }


    @Override
    public ConfigTestResult testAsync ( NetworkConfiguration config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {

        log.debug("Running network test"); //$NON-NLS-1$

        if ( ! ( params instanceof NetworkConfigTestParams ) ) {
            return r.state(ConfigTestState.FAILURE);
        }

        NetworkConfigTestParams p = (NetworkConfigTestParams) params;
        r.warn("NETWORK_ACTIVE_CONFIG"); //$NON-NLS-1$
        HostOrAddress tgt = HostOrAddress.fromString(p.getTarget());

        Inet4Address v4addr = null;
        Inet6Address v6addr = null;

        if ( tgt.isHostName() ) {
            InetAddress[] addrs = SocketValidationUtils.checkDNSLookup(r, tgt.getHostName());
            if ( addrs == null ) {
                return r.state(ConfigTestState.FAILURE);
            }

            for ( InetAddress ad : addrs ) {
                if ( v4addr == null && ad instanceof Inet4Address ) {
                    v4addr = (Inet4Address) ad;
                }
                else if ( v6addr == null && ad instanceof Inet6Address ) {
                    v6addr = (Inet6Address) ad;
                }
            }
        }
        else {
            r.info("NETWORK_TARGET_ADDRESS", tgt.getAddress().toString()); //$NON-NLS-1$

            try {
                if ( tgt.getAddress().getBitSize() == 32 ) {
                    v4addr = (Inet4Address) InetAddress.getByName(tgt.getAddress().toString());
                }
                else if ( tgt.getAddress().getBitSize() == 128 ) {
                    v6addr = (Inet6Address) InetAddress.getByName(tgt.getAddress().toString());
                }
                else {
                    log.debug("Unknown address type " + tgt.getAddress()); //$NON-NLS-1$
                }
            }
            catch ( UnknownHostException e ) {
                log.debug("Address lookup failed?", e); //$NON-NLS-1$
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("v4 " + v4addr); //$NON-NLS-1$
            log.debug("v6 " + v6addr); //$NON-NLS-1$
        }

        boolean fail = false;
        if ( v4addr != null ) {
            fail |= runV4Test(r, h, p, v4addr);
        }

        if ( v6addr != null ) {
            fail |= runV6Test(r, h, p, v6addr);
        }

        if ( fail ) {
            r.state(ConfigTestState.FAILURE);
        }
        return r.state(ConfigTestState.SUCCESS);
    }


    /**
     * @param r
     * @param h
     * @param p
     * @param v6addr
     * @return
     */
    private static boolean runV6Test ( ConfigTestResult r, ConfigTestAsyncHandler h, NetworkConfigTestParams p, Inet6Address v6addr ) {
        r.info("NET_IPV6_TEST", v6addr.toString()); //$NON-NLS-1$
        return runTests(r, h, p, v6addr, false);
    }


    /**
     * @param r
     * @param h
     * @param v4addr
     */
    private static boolean runV4Test ( ConfigTestResult r, ConfigTestAsyncHandler h, NetworkConfigTestParams p, Inet4Address v4addr ) {
        r.info("NET_IPV4_TEST", v4addr.toString()); //$NON-NLS-1$
        return runTests(r, h, p, v4addr, false);
    }


    /**
     * @param r
     * @param h
     * @param p
     * @param v4addr
     * @return
     */
    static boolean runTests ( ConfigTestResult r, ConfigTestAsyncHandler h, NetworkConfigTestParams p, InetAddress addr, boolean v6 ) {
        h.update(r);
        boolean anyFail = false;
        if ( p.getPort() != null && p.getPort() > 0 ) {
            anyFail |= !runConnectTest(r, h, addr, p.getPort(), v6);
        }
        if ( p.getRunPing() ) {
            anyFail |= !runPingTest(r, h, addr, v6);
        }
        if ( p.getRunTraceroute() ) {
            anyFail |= !runTracerouteTest(r, h, addr, v6);
        }
        return !anyFail;
    }


    /**
     * @param r
     * @param h
     * @param addr
     * @param v6
     * @return
     */
    private static boolean runConnectTest ( ConfigTestResult r, ConfigTestAsyncHandler h, InetAddress addr, int port, boolean v6 ) {
        r.info("NET_CONNECTING", addr.getHostAddress(), String.valueOf(port)); //$NON-NLS-1$

        int timeout = 4000;
        try ( Socket s = SocketFactory.getDefault().createSocket() ) {
            s.setSoTimeout(timeout);
            s.connect(new InetSocketAddress(addr, port), timeout);

            try ( InputStream is = s.getInputStream() ) {

                byte[] buffer = new byte[256];

                try {
                    int read = is.read(buffer);

                    if ( log.isDebugEnabled() ) {
                        log.debug("Received " + read); //$NON-NLS-1$
                    }

                    if ( read > 0 ) {
                        r.info("NET_CONNECT_BANNER", stringify(buffer, read)); //$NON-NLS-1$
                    }
                }
                catch ( SocketTimeoutException e ) {
                    log.trace("Ignoring socket timeout", e); //$NON-NLS-1$
                }

            }
            r.info("NET_CONNECTED", addr.getHostAddress(), String.valueOf(port)); //$NON-NLS-1$
            return true;
        }
        catch ( IOException e ) {
            SocketValidationUtils.handleIOException(e, r, String.format("%s:%d", addr.getHostAddress(), port)); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param buffer
     * @return
     */
    private static String stringify ( byte[] buffer, int read ) {
        char[] chars = new char[read];

        for ( int i = 0; i < read; i++ ) {
            byte b = buffer[ i ];
            if ( b <= 167 && b >= 32 ) {
                chars[ i ] = (char) b;
            }
            else {
                chars[ i ] = '.';
            }
        }
        return new String(chars);
    }


    /**
     * @param r
     * @param h
     * @param v6addr
     * @param b
     * @return
     */
    private static boolean runTracerouteTest ( ConfigTestResult r, ConfigTestAsyncHandler h, InetAddress addr, boolean v6 ) {
        String suffix = v6 ? "6" : StringUtils.EMPTY; //$NON-NLS-1$

        int rc = exec(
            r,
            h,
            "traceroute" + suffix, //$NON-NLS-1$
            "/usr/bin/traceroute" + suffix, //$NON-NLS-1$
            addr.getHostAddress());

        if ( rc == 0 ) {
            r.info("NETWORK_TRACE_OK", addr.getHostAddress()); //$NON-NLS-1$
            return true;
        }
        r.error("NETWORK_TRACE_ERROR", addr.getHostAddress()); //$NON-NLS-1$
        return false;
    }


    /**
     * @param r
     * @param h
     * @param addr
     */
    private static boolean runPingTest ( ConfigTestResult r, ConfigTestAsyncHandler h, InetAddress addr, boolean v6 ) {
        int cnt = 5;
        int timeout = 10;
        String suffix = v6 ? "6" : StringUtils.EMPTY; //$NON-NLS-1$
        int rc = exec(
            r,
            h,
            "ping" + suffix, //$NON-NLS-1$
            "/bin/ping" + suffix, //$NON-NLS-1$
            "-w", //$NON-NLS-1$
            String.valueOf(timeout),
            "-D", //$NON-NLS-1$
            "-O", //$NON-NLS-1$
            "-n", //$NON-NLS-1$
            "-c", //$NON-NLS-1$
            String.valueOf(cnt),
            addr.getHostAddress());

        if ( log.isDebugEnabled() ) {
            log.debug("Ping result is " + rc); //$NON-NLS-1$
        }

        if ( rc == 0 ) {
            r.info("NETWORK_PING_OK", addr.getHostAddress()); //$NON-NLS-1$
            return true;
        }
        else if ( rc == 1 ) {
            r.warn("NETWORK_PING_UNREACH", addr.getHostAddress()); //$NON-NLS-1$
            return false;
        }

        r.error("NETWORK_PING_ERROR", addr.getHostAddress()); //$NON-NLS-1$
        return false;
    }


    /**
     * @param args
     */
    private static int exec ( ConfigTestResult r, ConfigTestAsyncHandler h, String displayProg, String... args ) {
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(args));
        pb.environment().clear();
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            p.getOutputStream().close();
            try ( InputStream is = p.getInputStream();
                  InputStreamReader isr = new InputStreamReader(is);
                  BufferedReader br = new BufferedReader(isr) ) {
                String line;
                while ( ( line = br.readLine() ) != null ) {
                    log.debug(line);
                    r.info("NETWORK_TOOL_OUTPUT", displayProg, line); //$NON-NLS-1$
                    h.update(r);
                }
            }

            if ( !p.waitFor(20, TimeUnit.SECONDS) ) {
                log.warn("Process did not voluntariliy exit within timeout, waiting for it to die"); //$NON-NLS-1$
                p.destroyForcibly();
                p.waitFor();
            }
            return p.exitValue();
        }
        catch (
            IOException |
            InterruptedException e ) {
            log.debug("Process execution failed", e); //$NON-NLS-1$
            r.error("NETWORK_TOOL_EXEC_FAIL", displayProg, e.getMessage()); //$NON-NLS-1$
            return -1;
        }
    }

}

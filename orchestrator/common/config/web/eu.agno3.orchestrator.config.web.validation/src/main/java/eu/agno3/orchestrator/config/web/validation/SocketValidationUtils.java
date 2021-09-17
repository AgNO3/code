/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation;


import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.web.LDAPConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 *
 */
public final class SocketValidationUtils {

    private static final Logger log = Logger.getLogger(SocketValidationUtils.class);

    /**
     * Not really kerberos but binds to web package
     */
    private static final String USE_TYPE = LDAPConfigurationObjectTypeDescriptor.TYPE_NAME;


    /**
     * 
     */
    private SocketValidationUtils () {}


    /**
     * @param e
     * @param r
     * @param ep
     */
    public static void handleSocketException ( SocketException e, ConfigTestResult r, String ep ) {
        ConfigTestResult br = r.withType(USE_TYPE);
        if ( e instanceof NoRouteToHostException ) {
            log.debug(String.format("Host %s unreachable", ep), e); //$NON-NLS-1$
            br.error("FAIL_CONNECT_HOST_UNREACH", ep, e.getMessage()); //$NON-NLS-1$
        }
        else if ( e instanceof PortUnreachableException ) {
            log.debug(String.format("Port %s unreachable", ep), e); //$NON-NLS-1$
            br.error("FAIL_CONNECT_PORT_UNREACH", ep, e.getMessage()); //$NON-NLS-1$
        }
        else if ( e instanceof ConnectException ) {
            log.debug(String.format("Connection to %s failed", ep), e); //$NON-NLS-1$
            br.error("FAIL_CONNECT_REFUSED", ep, e.getMessage()); //$NON-NLS-1$
        }
        else {
            log.debug("Unknown socket exception", e); //$NON-NLS-1$
            br.error("FAIL_NET_UNKNOWN", ep, e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @param r
     * @param ep
     */
    public static void handleIOException ( IOException e, ConfigTestResult r, String ep ) {
        ConfigTestResult br = r.withType(USE_TYPE);
        if ( e instanceof SocketException ) {
            SocketValidationUtils.handleSocketException((SocketException) e, r, ep);
        }
        else if ( e instanceof SocketTimeoutException ) {
            log.debug(String.format("Socket timeout connecting to %s", ep)); //$NON-NLS-1$
            br.error("FAIL_CONNECT_TIMEOUT", e.getMessage()); //$NON-NLS-1$
        }
        else {
            log.debug("Unknown connection error", e); //$NON-NLS-1$
            br.error("FAIL_NET_UNKNOWN", ep, e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param names
     * @return ip addresses string
     */
    public static String toAddrs ( InetAddress[] names ) {
        String[] addrs = new String[names.length];
        int i = 0;
        for ( InetAddress name : names ) {
            addrs[ i++ ] = name.getHostAddress();
        }
        return Arrays.toString(addrs);
    }


    /**
     * @param r
     * @param host
     * @return whether lookup was successful
     */
    public static InetAddress[] checkDNSLookup ( ConfigTestResult r, String host ) {
        ConfigTestResult br = r.withType(USE_TYPE);
        try {
            InetAddress[] names = InetAddress.getAllByName(host);

            if ( names == null || names.length == 0 ) {
                br.error("HOST_LOOKUP_NOADDR", host); //$NON-NLS-1$
                return null;
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Resolved addresses for %s: %s", host, Arrays.toString(names))); //$NON-NLS-1$
            }
            br.info("HOST_LOOKUP_OK", host, toAddrs(names)); //$NON-NLS-1$
            return names;
        }
        catch ( UnknownHostException e ) {
            log.debug("Host lookup failed", e); //$NON-NLS-1$
            br.error("HOST_LOOKUP_FAIL", host, e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

}
